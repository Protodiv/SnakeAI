package ua.snakeai.backend.handler

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import ua.snakeai.backend.ai.DqnAgent
import ua.snakeai.backend.ai.SnakeEnv
import ua.snakeai.backend.ai.Transition
import ua.snakeai.backend.repository.TrainedModelEntity
import ua.snakeai.backend.repository.TrainedModelRepository
import ua.snakeai.contract.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

data class CompletedEpisode(
    val episodeNumber: Int,
    val metrics: TrainingProgressMetrics,
    val states: List<GameState>
)

@Component
class AiTrainWebSocketHandler(
    private val repository: TrainedModelRepository,
    @Value("\${model.storage.path:models}") private val modelStoragePath: String
) : WebSocketHandler {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val logger = LoggerFactory.getLogger(AiTrainWebSocketHandler::class.java)
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("New AI Training WebSocket session initiated. ID: ${session.id}, URI: ${session.handshakeInfo.uri}")
        val sessionJob = Job()
        val scope = CoroutineScope(Dispatchers.Default + sessionJob)

        scope.launch {
            var isTraining = false
            var agent: DqnAgent? = null
            var fieldSize = FieldSize.MEDIUM
            var hyperparameters = TrainHyperparameters()
            
            // Stats tracker
            val stats = AtomicReference<TrainingProgressMetrics?>(null)
            val latestCompletedEpisode = AtomicReference<CompletedEpisode?>(null)
            var currentEpisode = 0
            var topScore = 0
            val recentScores = mutableListOf<Int>()
            val rewardHistory = mutableListOf<Double>()
            val lossHistory = mutableListOf<Double>()
            val startTime = System.currentTimeMillis()
            var totalStepsPlayed = 0

            session.receive()
                .doOnTerminate {
                    logger.info("AI Training WebSocket receive flow terminated. ID: ${session.id}")
                    sessionJob.cancel()
                }
                .subscribe { webSocketMessage ->
                    try {
                        val payload = webSocketMessage.payloadAsText
                        logger.info("AI Training WS [ID: ${session.id}] received: $payload")
                        val cmd = json.decodeFromString<TrainCommand>(payload)
                        when (cmd.action) {
                            "START_TRAINING" -> {
                                logger.info("AI Training [ID: ${session.id}] START_TRAINING for modelName: ${cmd.modelName ?: "new_model"}")
                                fieldSize = cmd.fieldSize ?: FieldSize.MEDIUM
                                hyperparameters = cmd.hyperparameters ?: TrainHyperparameters()
                                
                                val modelName = cmd.modelName ?: ("Agent_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")))
                                val modelsDir = File(modelStoragePath)
                                if (!modelsDir.exists()) modelsDir.mkdirs()
                                val modelFile = File(modelsDir, "$modelName.zip")
                                agent = if (modelFile.exists()) {
                                    DqnAgent(modelName, modelFile)
                                } else {
                                    DqnAgent(
                                        name = modelName,
                                        learningRate = hyperparameters.learningRate,
                                        batchSize = hyperparameters.batchSize,
                                        memorySize = 50000
                                    )
                                }
                                currentEpisode = 0
                                topScore = 0
                                recentScores.clear()
                                rewardHistory.clear()
                                lossHistory.clear()
                                totalStepsPlayed = 0
                                latestCompletedEpisode.set(null)
                                isTraining = true
                            }
                            "STOP" -> {
                                logger.info("AI Training [ID: ${session.id}] STOP training")
                                isTraining = false
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Error processing training WS message [ID: ${session.id}]", e)
                    }
                }

            // Playback thread to stream completed episodes frame-by-frame
            scope.launch {
                var lastPlayedEpisodeNum = 0
                val playbackTickRateMs = 80L // Smooth frame rate (~12.5 FPS)

                while (isActive) {
                    val completed = latestCompletedEpisode.get()
                    if (completed != null) {
                        if (completed.episodeNumber < lastPlayedEpisodeNum) {
                            lastPlayedEpisodeNum = 0
                        }
                        
                        if (completed.episodeNumber > lastPlayedEpisodeNum) {
                            val epNum = completed.episodeNumber
                            val states = completed.states
                            val epMetrics = completed.metrics
                            
                            logger.info("Starting playback of episode #$epNum (${states.size} steps)")
                            for (state in states) {
                                if (!isActive || !isTraining) break
                                
                                val frame = TrainingMetricsFrame(
                                    metrics = epMetrics,
                                    gameState = state
                                )
                                val jsonStr = json.encodeToString(frame)
                                session.send(Mono.just(session.textMessage(jsonStr))).subscribe()
                                delay(playbackTickRateMs)
                            }
                            lastPlayedEpisodeNum = epNum
                        } else {
                            delay(100L)
                        }
                    } else {
                        delay(100L)
                    }
                }
            }

            // Training Loop
            while (isActive) {
                if (isTraining && agent != null && currentEpisode < hyperparameters.maxEpisodes) {
                    currentEpisode++

                    val actualSize = if (fieldSize == FieldSize.RANDOM) {
                        listOf(FieldSize.SMALL, FieldSize.MEDIUM, FieldSize.LARGE).random()
                    } else fieldSize

                    // 1. Reset Env
                    var state = GameEngine.initGame(actualSize, 4, Direction.RIGHT, Random.Default)
                    state = state.copy(status = GameStatus.PLAYING)

                    val recordedStates = mutableListOf<GameState>()
                    recordedStates.add(state)

                    var episodeReward = 0.0
                    var stepCount = 0
                    val episodeLosses = mutableListOf<Double>()

                    // 2. Play episode
                    while (state.status == GameStatus.PLAYING && isTraining) {
                        val obs = SnakeEnv.getObservation(state)
                        val (action, isExploration) = agent!!.selectAction(obs, explore = true)
                        val nextDir = SnakeEnv.getAbsoluteDirection(state.direction, action)
                        
                        var nextState = GameEngine.step(state, nextDir)
                        stepCount++
                        totalStepsPlayed++

                        // Epsilon-Greedy timeout trap to prevent infinite circling loops
                        val maxSteps = 200 + (nextState.snake.size * 10)
                        if (stepCount > maxSteps) {
                            nextState = nextState.copy(status = GameStatus.GAME_OVER)
                        }

                        val nextObs = SnakeEnv.getObservation(nextState)
                        val reward = SnakeEnv.getReward(state, nextState)
                        val done = (nextState.status == GameStatus.GAME_OVER || nextState.status == GameStatus.VICTORY)

                        episodeReward += reward

                        // Store transition in replay memory
                        agent!!.replayBuffer.add(Transition(obs, action, reward, nextObs, done))

                        // Train on batch
                        val loss = agent!!.trainStep()
                        if (loss > 0.0) {
                            episodeLosses.add(loss)
                        }

                        state = nextState
                        recordedStates.add(state)
                    }

                    // 3. Post episode calculations
                    agent!!.decayEpsilon()

                    val finalScore = state.score
                    recentScores.add(finalScore)
                    if (recentScores.size > 100) recentScores.removeAt(0)
                    
                    if (finalScore > topScore) {
                        topScore = finalScore
                    }

                    val avgLoss = if (episodeLosses.isEmpty()) 0.0 else episodeLosses.average()
                    lossHistory.add(avgLoss)
                    rewardHistory.add(episodeReward)

                    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                    val speed = if (elapsedSec > 0) totalStepsPlayed / elapsedSec else 0.0

                    // Create metrics for this episode
                    val episodeMetrics = TrainingProgressMetrics(
                        episode = currentEpisode,
                        epsilon = agent!!.epsilon,
                        loss = avgLoss,
                        averageReward = rewardHistory.takeLast(100).average(),
                        topScore = topScore,
                        recentScore = finalScore,
                        stepsPlayed = totalStepsPlayed,
                        stepsPerSecond = speed,
                        elapsedTimeMs = System.currentTimeMillis() - startTime
                    )
                    stats.set(episodeMetrics)

                    // Buffer this completed episode for playback
                    latestCompletedEpisode.set(CompletedEpisode(currentEpisode, episodeMetrics, recordedStates))

                    // 4. Save model upon completion
                    if (currentEpisode >= hyperparameters.maxEpisodes) {
                        isTraining = false

                        val modelsDir = File(modelStoragePath)
                        if (!modelsDir.exists()) modelsDir.mkdirs()
                        val modelFile = File(modelsDir, "${agent!!.name}.zip")
                        agent!!.save(modelFile)

                        // Save details to database
                        val dbDir = File("db")
                        if (!dbDir.exists()) dbDir.mkdirs()

                        // Calculate efficiency as average score of last 100 episodes
                        val efficiency = recentScores.average()

                        // Serialize history arrays
                        val historyMap = mapOf(
                            "rewards" to rewardHistory,
                            "losses" to lossHistory
                        )
                        val historyStr = json.encodeToString(historyMap)

                        val entity = TrainedModelEntity(
                            name = agent!!.name,
                            episodesRun = currentEpisode.toLong(),
                            efficiency = efficiency,
                            topScore = topScore,
                            filePath = modelFile.absolutePath,
                            createdAt = LocalDateTime.now(),
                            historyJson = historyStr
                        )
                        repository.save(entity)
                    }
                } else {
                    delay(100L)
                }
            }
        }

        return Mono.never<Void>()
            .doFinally { signalType ->
                logger.info("AI Training WebSocket connection closed. ID: ${session.id}, Signal: $signalType")
                sessionJob.cancel()
            }
    }
}
