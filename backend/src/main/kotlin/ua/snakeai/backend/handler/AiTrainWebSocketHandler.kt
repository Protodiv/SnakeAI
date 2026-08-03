package ua.snakeai.backend.handler

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.reactive.asFlow
import kotlinx.serialization.encodeToString
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ua.snakeai.backend.ai.DqnAgent
import ua.snakeai.backend.ai.SnakeEnv
import ua.snakeai.backend.ai.Transition
import ua.snakeai.backend.exception.ModelSaveException
import ua.snakeai.backend.service.TrainModelService
import ua.snakeai.contract.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Component
class AiTrainWebSocketHandler(
    aiModelService: TrainModelService,
    @Value("\${model.storage.path:models}") modelStoragePath: String,
    @Value("\${model.training.default-max-episodes:100}") private val defaultMaxEpisodes: Int
) : BaseAiWebSocketHandler(modelStoragePath, aiModelService, LoggerFactory.getLogger(AiTrainWebSocketHandler::class.java)) {

    override suspend fun handleSession(session: WebSocketSession, scope: CoroutineScope) {
        var isTraining = false
        var agent: DqnAgent? = null
        var fieldSize = FieldSize.MEDIUM
        var hyperparameters = TrainHyperparameters()

        // Stats tracker
        val stats = AtomicReference<TrainingProgressMetrics?>(null)
        val completedEpisodes = Channel<CompletedEpisode>(Channel.CONFLATED)
        var currentEpisode = 0
        var topScore = 0
        val recentScores = mutableListOf<Int>()
        val rewardHistory = mutableListOf<Double>()
        val lossHistory = mutableListOf<Double>()
        val startTime = System.currentTimeMillis()
        var totalStepsPlayed = 0

        // Launch incoming message consumer
        scope.launch {
            try {
                session.receive()
                    .map { it.payloadAsText }
                    .asFlow()
                    .collect { payload ->
                        log.info("AI Training WS [ID: ${session.id}] received: $payload")
                        when (val cmd = json.decodeFromString<TrainCommand>(payload)) {
                            is TrainCommand.Start -> {
                                log.info("AI Training [ID: ${session.id}] START_TRAINING for modelName: ${cmd.modelName ?: "new_model"}")
                                fieldSize = cmd.fieldSize ?: FieldSize.RANDOM
                                hyperparameters = cmd.hyperparameters ?: TrainHyperparameters()

                                val modelName = cmd.modelName ?: ("Agent_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")))
                                agent = loadOrCreateAgent(modelName, hyperparameters)

                                currentEpisode = 0
                                topScore = 0
                                recentScores.clear()
                                rewardHistory.clear()
                                lossHistory.clear()
                                totalStepsPlayed = 0
                                
                                // Drain the channel to ensure we don't play stale episodes
                                while (completedEpisodes.tryReceive().isSuccess) { /* empty */ }
                                
                                isTraining = true
                            }
                            is TrainCommand.Stop -> {
                                log.info("AI Training [ID: ${session.id}] STOP training")
                                isTraining = false
                            }
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error("Error consuming training commands [ID: ${session.id}]", e)
            }
        }

        // Playback thread to stream completed episodes frame-by-frame
        scope.launch {
            var lastPlayedEpisodeNum = 0
            val playbackTickRateMs = 80L // Smooth frame rate (~12.5 FPS)

            try {
                for (completed in completedEpisodes) {
                    if (completed.episodeNumber < lastPlayedEpisodeNum) {
                        lastPlayedEpisodeNum = 0
                    }

                    if (completed.episodeNumber > lastPlayedEpisodeNum) {
                        val epNum = completed.episodeNumber
                        val states = completed.states
                        val epMetrics = completed.metrics

                        log.info("Starting playback of episode #$epNum (${states.size} steps)")
                        for (state in states) {
                            if (!isActive || !isTraining) break

                            val frame = TrainingMetricsFrame(
                                metrics = epMetrics,
                                gameState = state
                            )
                            val jsonStr = json.encodeToString(frame)
                            sendSafe(session, jsonStr)
                            delay(playbackTickRateMs.milliseconds)
                        }
                        lastPlayedEpisodeNum = epNum
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error("Error during playback loop [ID: ${session.id}]", e)
            }
        }

        // Training Loop
        while (scope.isActive) {
            val currentAgent = agent
            val resolvedMaxEpisodes = hyperparameters.maxEpisodes ?: defaultMaxEpisodes
            if (isTraining && currentAgent != null && currentEpisode < resolvedMaxEpisodes) {
                currentEpisode++

                val actualSize = resolveFieldSize(fieldSize)

                // 1. Play episode using extracted helper
                val result = runTrainingEpisode(
                    agent = currentAgent,
                    actualSize = actualSize,
                    isTrainingActive = { isTraining }
                )

                totalStepsPlayed += result.totalSteps

                // 2. Post episode calculations using extracted helper
                val calcResult = handlePostEpisodeCalculations(
                    agent = currentAgent,
                    episode = currentEpisode,
                    result = result,
                    recentScores = recentScores,
                    rewardHistory = rewardHistory,
                    lossHistory = lossHistory,
                    startTime = startTime,
                    totalStepsPlayed = totalStepsPlayed,
                    currentTopScore = topScore
                )
                topScore = calcResult.topScore
                stats.set(calcResult.metrics)

                // Buffer this completed episode for playback
                completedEpisodes.trySend(CompletedEpisode(currentEpisode, calcResult.metrics, result.recordedStates))

                // 3. Save model upon completion using extracted helper
                if (currentEpisode >= resolvedMaxEpisodes) {
                    isTraining = false
                    saveTrainedModel(
                        agent = currentAgent,
                        episodesRun = currentEpisode,
                        topScore = topScore,
                        recentScores = recentScores,
                        rewardHistory = rewardHistory,
                        lossHistory = lossHistory,
                        session = session
                    )
                }
            } else {
                delay(100L.milliseconds)
            }
        }
    }

    private suspend fun runTrainingEpisode(
        agent: DqnAgent,
        actualSize: FieldSize,
        isTrainingActive: () -> Boolean
    ): EpisodeResult {
        var state = GameEngine.initGame(actualSize, 4, Direction.RIGHT, Random.Default)
        state = state.copy(status = GameStatus.PLAYING)

        val recordedStates = mutableListOf<GameState>()
        recordedStates.add(state)

        var episodeReward = 0.0
        var stepCount = 0
        val episodeLosses = mutableListOf<Double>()

        while (state.status == GameStatus.PLAYING && isTrainingActive()) {
            val obs = SnakeEnv.getObservation(state)
            val (action, _) = agent.selectAction(obs, explore = true)
            val nextDir = SnakeEnv.getAbsoluteDirection(state.direction, action)

            var nextState = GameEngine.step(state, nextDir)
            stepCount++

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
            agent.replayBuffer.add(Transition(obs, action, reward, nextObs, done))

            // Train on batch
            val loss = agent.trainStep()
            if (loss > 0.0) {
                episodeLosses.add(loss)
            }

            state = nextState
            recordedStates.add(state)
        }

        return EpisodeResult(
            finalState = state,
            recordedStates = recordedStates,
            reward = episodeReward,
            totalSteps = stepCount,
            losses = episodeLosses
        )
    }

    private fun handlePostEpisodeCalculations(
        agent: DqnAgent,
        episode: Int,
        result: EpisodeResult,
        recentScores: MutableList<Int>,
        rewardHistory: MutableList<Double>,
        lossHistory: MutableList<Double>,
        startTime: Long,
        totalStepsPlayed: Int,
        currentTopScore: Int
    ): PostEpisodeCalculationResult {
        agent.decayEpsilon()

        val finalScore = result.finalState.score
        recentScores.add(finalScore)
        if (recentScores.size > 100) recentScores.removeAt(0)

        val newTopScore = if (finalScore > currentTopScore) finalScore else currentTopScore

        val avgLoss = if (result.losses.isEmpty()) 0.0 else result.losses.average()
        lossHistory.add(avgLoss)
        rewardHistory.add(result.reward)

        val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
        val speed = if (elapsedSec > 0) totalStepsPlayed / elapsedSec else 0.0

        val episodeMetrics = TrainingProgressMetrics(
            episode = episode,
            epsilon = agent.epsilon,
            loss = avgLoss,
            averageReward = rewardHistory.takeLast(100).average(),
            topScore = newTopScore,
            recentScore = finalScore,
            stepsPlayed = totalStepsPlayed,
            stepsPerSecond = speed,
            elapsedTimeMs = System.currentTimeMillis() - startTime
        )

        return PostEpisodeCalculationResult(newTopScore, episodeMetrics)
    }

    private fun saveTrainedModel(
        agent: DqnAgent,
        episodesRun: Int,
        topScore: Int,
        recentScores: List<Int>,
        rewardHistory: List<Double>,
        lossHistory: List<Double>,
        session: WebSocketSession
    ) {
        var tempFile: File? = null
        try {
            tempFile = File.createTempFile("agent-save-${agent.name}-", ".zip")
            tempFile.deleteOnExit()
            agent.save(tempFile)

            // Calculate efficiency as average score of last 100 episodes
            val efficiency = recentScores.average()

            // Downsample histories to a maximum of 1000 points to keep database size bounded
            val sampledRewards = rewardHistory.downsample(1000)
            val sampledLosses = lossHistory.downsample(1000)

            // Serialize history arrays
            val historyMap = mapOf(
                "rewards" to sampledRewards,
                "losses" to sampledLosses
            )
            val historyStr = json.encodeToString(historyMap)

            trainModelService.saveModel(
                agentName = agent.name,
                episodesRun = episodesRun.toLong(),
                efficiency = efficiency,
                topScore = topScore,
                tempModelFile = tempFile,
                historyJson = historyStr
            )
        } catch (e: ModelSaveException) {
            log.error("Custom error saving model: ${e.message}", e)
            val errorResponse = ErrorResponse(
                message = e.message,
                code = e.errorCode,
                timestamp = LocalDateTime.now().toString()
            )
            sendSafe(session, json.encodeToString(errorResponse))
        } catch (e: Exception) {
            log.error("Unexpected error saving model: ${e.message}", e)
            val errorResponse = ErrorResponse(
                message = "An unexpected error occurred while saving the model: ${e.message}",
                code = "UNEXPECTED_SAVE_ERROR",
                timestamp = LocalDateTime.now().toString()
            )
            sendSafe(session, json.encodeToString(errorResponse))
        } finally {
            tempFile?.let {
                if (it.exists()) {
                    it.delete()
                }
            }
        }
    }

    private fun List<Double>.downsample(maxSize: Int): List<Double> {
        if (this.size <= maxSize) return this
        val step = this.size.toDouble() / maxSize
        return List(maxSize) { i ->
            val start = (i * step).toInt().coerceIn(0, this.size - 1)
            val end = ((i + 1) * step).toInt().coerceIn(start + 1, this.size)
            this.subList(start, end).average()
        }
    }

    data class CompletedEpisode(
        val episodeNumber: Int,
        val metrics: TrainingProgressMetrics,
        val states: List<GameState>
    )

    private data class EpisodeResult(
        val finalState: GameState,
        val recordedStates: List<GameState>,
        val reward: Double,
        val totalSteps: Int,
        val losses: List<Double>
    )

    private data class PostEpisodeCalculationResult(
        val topScore: Int,
        val metrics: TrainingProgressMetrics
    )
}
