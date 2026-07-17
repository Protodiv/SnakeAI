package ua.snakeai.backend.handler

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
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
import kotlin.math.abs
import kotlin.random.Random

@Component
class AiTrainWebSocketHandler(
    private val repository: TrainedModelRepository,
    @Value("\${model.storage.path:models}") private val modelStoragePath: String
) : WebSocketHandler {

    private val json = Json { ignoreUnknownKeys = true }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionJob = Job()
        val scope = CoroutineScope(Dispatchers.Default + sessionJob)

        scope.launch {
            var isTraining = false
            var agent: DqnAgent? = null
            var fieldSize = FieldSize.MEDIUM
            var hyperparameters = TrainHyperparameters()
            
            // Stats tracker
            val stats = AtomicReference<TrainingProgressMetrics?>(null)
            var currentEpisode = 0
            var topScore = 0
            val recentScores = mutableListOf<Int>()
            val rewardHistory = mutableListOf<Double>()
            val lossHistory = mutableListOf<Double>()
            val startTime = System.currentTimeMillis()
            var totalStepsPlayed = 0

            session.receive()
                .doOnTerminate {
                    sessionJob.cancel()
                }
                .subscribe { webSocketMessage ->
                    try {
                        val payload = webSocketMessage.payloadAsText
                        val cmd = json.decodeFromString<TrainCommand>(payload)
                        when (cmd.action) {
                            "START_TRAINING" -> {
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
                                isTraining = true
                            }
                            "STOP" -> {
                                isTraining = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            // Ticker thread to send metrics at 5Hz (every 200ms)
            scope.launch {
                while (isActive) {
                    val currentStats = stats.get()
                    if (currentStats != null) {
                        val frame = TrainingMetricsFrame(metrics = currentStats)
                        val jsonStr = json.encodeToString(frame)
                        session.send(Mono.just(session.textMessage(jsonStr))).subscribe()
                    }
                    delay(200L) // 5Hz
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

                    // Update metrics atomic reference (to be consumed by the 5Hz ticker)
                    stats.set(
                        TrainingProgressMetrics(
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
                    )

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
            .doFinally {
                sessionJob.cancel()
            }
    }
}
