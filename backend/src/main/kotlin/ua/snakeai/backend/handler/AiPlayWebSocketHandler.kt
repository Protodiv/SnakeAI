package ua.snakeai.backend.handler

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import ua.snakeai.backend.ai.DqnAgent
import ua.snakeai.backend.ai.SnakeEnv
import ua.snakeai.contract.*
import java.io.File
import kotlin.random.Random

@Component
class AiPlayWebSocketHandler(
    @Value("\${model.storage.path:models}") private val modelStoragePath: String
) : WebSocketHandler {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val logger = LoggerFactory.getLogger(AiPlayWebSocketHandler::class.java)
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("New AI Play WebSocket session initiated. ID: ${session.id}, URI: ${session.handshakeInfo.uri}")
        val sessionJob = Job()
        val scope = CoroutineScope(Dispatchers.Default + sessionJob)

        scope.launch {
            var isRunning = false
            var isPaused = false
            var gameState: GameState? = null
            var agent: DqnAgent? = null
            var tickRateMs = 120L

            session.receive()
                .doOnTerminate {
                    logger.info("AI Play WebSocket receive flow terminated. ID: ${session.id}")
                    sessionJob.cancel()
                }
                .subscribe { webSocketMessage ->
                    try {
                        val payload = webSocketMessage.payloadAsText
                        logger.info("AI Play WS [ID: ${session.id}] received: $payload")
                        val cmd = json.decodeFromString<PlayCommand>(payload)
                        when (cmd.action) {
                            "START" -> {
                                val modelName = cmd.modelName ?: "default_agent"
                                val size = cmd.fieldSize ?: FieldSize.MEDIUM
                                val actualSize = if (size == FieldSize.RANDOM) {
                                    listOf(FieldSize.SMALL, FieldSize.MEDIUM, FieldSize.LARGE).random()
                                } else size

                                tickRateMs = cmd.tickRateMs ?: 120L

                                val modelsDir = File(modelStoragePath)
                                if (!modelsDir.exists()) modelsDir.mkdirs()
                                val modelFile = File(modelsDir, "$modelName.zip")
                                agent = if (modelFile.exists()) {
                                    DqnAgent(modelName, modelFile)
                                } else {
                                    DqnAgent(modelName)
                                }

                                gameState = GameEngine.initGame(actualSize, 4, Direction.RIGHT, Random.Default)
                                gameState = gameState?.copy(status = GameStatus.PLAYING)
                                isRunning = true
                                isPaused = false
                            }
                            "PAUSE" -> {
                                isPaused = true
                            }
                            "RESUME" -> {
                                isPaused = false
                            }
                            "RESTART" -> {
                                val size = gameState?.fieldSize ?: FieldSize.MEDIUM
                                gameState = GameEngine.initGame(size, 4, Direction.RIGHT, Random.Default)
                                gameState = gameState?.copy(status = GameStatus.PLAYING)
                                isPaused = false
                            }
                            "STOP" -> {
                                isRunning = false
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Error processing play WS message [ID: ${session.id}]", e)
                    }
                }

            // Play Game Loop
            while (isActive) {
                if (isRunning && !isPaused && gameState != null && agent != null) {
                    val currentGameState = gameState!!
                    if (currentGameState.status == GameStatus.PLAYING) {
                        // 1. Generate observation features
                        val obs = SnakeEnv.getObservation(currentGameState)

                        // 2. Select action (no exploration during show play)
                        val (action, isExploration) = agent!!.selectAction(obs, explore = false)

                        // 3. Map action to absolute direction
                        val nextDir = SnakeEnv.getAbsoluteDirection(currentGameState.direction, action)

                        // 4. Tick the shared engine
                        val nextGameState = GameEngine.step(currentGameState, nextDir)
                        gameState = nextGameState

                        // 5. Send frame update
                        val qValues = agent!!.getQValues(obs).toList()
                        val metrics = DecisionMetrics(
                            dangerStraight = obs[0],
                            dangerLeft = obs[1],
                            dangerRight = obs[2],
                            foodNorth = obs[7],
                            foodEast = obs[8],
                            foodSouth = obs[9],
                            foodWest = obs[10],
                            qValues = qValues,
                            selectedAction = when (action) {
                                0 -> "STRAIGHT"
                                1 -> "TURN_LEFT"
                                2 -> "TURN_RIGHT"
                                else -> "STRAIGHT"
                            },
                            isExploration = isExploration,
                            epsilon = agent!!.epsilon
                        )
                        val frame = GameFrame(state = nextGameState, decisionMetrics = metrics)
                        val jsonStr = json.encodeToString(frame)

                        session.send(Mono.just(session.textMessage(jsonStr))).subscribe()
                    }
                    delay(tickRateMs)
                } else {
                    delay(100L)
                }
            }
        }

        return Mono.never<Void>()
            .doFinally { signalType ->
                logger.info("AI Play WebSocket connection closed. ID: ${session.id}, Signal: $signalType")
                sessionJob.cancel()
            }
    }
}
