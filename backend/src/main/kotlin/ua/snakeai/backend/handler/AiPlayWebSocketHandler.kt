package ua.snakeai.backend.handler

import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.serialization.encodeToString
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ua.snakeai.backend.ai.DqnAgent
import ua.snakeai.backend.ai.SnakeEnv
import ua.snakeai.backend.service.TrainModelService
import ua.snakeai.contract.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Component
class AiPlayWebSocketHandler(
    aiModelService: TrainModelService,
    @Value("\${model.storage.path:models}") modelStoragePath: String
) : BaseAiWebSocketHandler(modelStoragePath, aiModelService, LoggerFactory.getLogger(AiPlayWebSocketHandler::class.java)) {

    override suspend fun handleSession(session: WebSocketSession, scope: CoroutineScope) {
        var isRunning = false
        var isPaused = false
        var gameState: GameState? = null
        var agent: DqnAgent? = null
        var tickRateMs = 120L

        scope.launch {
            try {
                session.receive()
                    .map { it.payloadAsText }
                    .asFlow()
                    .collect { payload ->
                        log.info("AI Play WS [ID: ${session.id}] received: $payload")
                        when (val cmd = json.decodeFromString<PlayCommand>(payload)) {
                            is PlayCommand.Start -> {
                                val modelName = cmd.modelName ?: "default_agent"
                                val actualSize = resolveFieldSize(cmd.fieldSize)
                                tickRateMs = cmd.tickRateMs ?: 120L

                                agent = loadAgent(modelName)
                                gameState = GameEngine.initGame(actualSize, 4, Direction.RIGHT, Random.Default)
                                    .copy(status = GameStatus.PLAYING)

                                isRunning = true
                                isPaused = false
                            }
                            is PlayCommand.Pause -> {
                                isPaused = true
                            }
                            is PlayCommand.Resume -> {
                                isPaused = false
                            }
                            is PlayCommand.Restart -> {
                                val size = gameState?.fieldSize ?: FieldSize.RANDOM
                                gameState = GameEngine.initGame(size, 4, Direction.RIGHT, Random.Default)
                                    .copy(status = GameStatus.PLAYING)
                                isPaused = false
                            }
                            is PlayCommand.Stop -> {
                                isRunning = false
                            }
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error("Error consuming client commands [ID: ${session.id}]", e)
            }
        }

        // Play Game Loop
        while (scope.isActive) {
            if (isRunning && !isPaused && gameState != null && agent != null) {
                val currentGameState = gameState!!
                if (currentGameState.status == GameStatus.PLAYING) {
                    val obs = SnakeEnv.getObservation(currentGameState)
                    val (action, isExploration) = agent!!.selectAction(obs, explore = false)
                    val nextDir = SnakeEnv.getAbsoluteDirection(currentGameState.direction, action)
                    val nextGameState = GameEngine.step(currentGameState, nextDir)
                    gameState = nextGameState

                    val qValues = agent!!.getQValues(obs)
                    val metrics = createDecisionMetrics(
                        obs = obs,
                        action = action,
                        isExploration = isExploration,
                        epsilon = agent!!.epsilon,
                        qValues = qValues
                    )
                    val frame = GameFrame(state = nextGameState, decisionMetrics = metrics)
                    val jsonStr = json.encodeToString(frame)

                    sendSafe(session, jsonStr)
                }
                delay(tickRateMs.milliseconds)
            } else {
                delay(100L.milliseconds)
            }
        }
    }
}
