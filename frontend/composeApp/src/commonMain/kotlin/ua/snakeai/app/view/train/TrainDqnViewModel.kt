package ua.snakeai.app.view.train

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.snakeai.app.core.mvi.BaseViewModel
import ua.snakeai.app.data.repository.SnakeAiRepository
import ua.snakeai.app.ui.shared.formatDouble
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.contract.GameState
import ua.snakeai.contract.TrainingProgressMetrics

class TrainDqnViewModel(
    private val repository: SnakeAiRepository
) : BaseViewModel<TrainDqnContract.State, TrainDqnContract.Event, TrainDqnContract.Effect>(
    TrainDqnContract.State()
) {
    private var trainJob: Job? = null

    override val state: StateFlow<TrainDqnContract.State> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    override fun onEvent(event: TrainDqnContract.Event) {
        when (event) {
            TrainDqnContract.Event.OnStartTrainingClicked -> startTraining()
            TrainDqnContract.Event.OnStopTrainingClicked -> stopTraining()
            is TrainDqnContract.Event.OnModelNameChanged -> {
                updateState { it.copy(modelName = event.name) }
            }
            is TrainDqnContract.Event.OnHyperparametersChanged -> {
                updateState { it.copy(hyperparameters = event.params) }
            }
            is TrainDqnContract.Event.OnLogReceived -> {
                updateState { it.copy(logs = it.logs + event.logLine) }
            }
            TrainDqnContract.Event.OnClearLogsClicked -> {
                updateState { it.copy(logs = emptyList()) }
            }
        }
    }

    private fun startTraining() {
        trainJob?.cancel()
        updateState {
            it.copy(
                isTraining = true,
                error = null,
                logs = it.logs + ">> Initializing Deep Q-Network Agent on backend..."
            )
        }
        trainJob = viewModelScope.launch {
            try {
                repository.trainAi(
                    modelName = currentState.modelName,
                    fieldSize = currentState.fieldSize,
                    hyperparameters = currentState.hyperparameters
                ).catch { e ->
                    val errorMsg = e.message ?: "Unknown error"
                    updateState {
                        it.copy(
                            error = "Training stream failed: $errorMsg",
                            isTraining = false,
                            logs = it.logs + ">> ERROR: $errorMsg"
                        )
                    }
                    emitEffect(TrainDqnContract.Effect.ShowToast("Training stream failed: $errorMsg"))
                }.collect { frame ->
                    val metrics = frame.metrics
                    val logLine = formatLogLine(metrics)
                    updateState {
                        val newLogs = if (it.metrics?.episode != metrics.episode) {
                            (it.logs + logLine).takeLast(200)
                        } else {
                            it.logs
                        }
                        it.copy(
                            metrics = metrics,
                            gameState = frame.gameState?.toGameContractState(metrics.topScore) ?: it.gameState,
                            logs = newLogs
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                updateState {
                    it.copy(
                        error = errorMsg,
                        isTraining = false,
                        logs = it.logs + ">> ERROR: $errorMsg"
                    )
                }
                emitEffect(TrainDqnContract.Effect.ShowToast(errorMsg))
            }
        }
    }

    private fun stopTraining() {
        trainJob?.cancel()
        updateState {
            it.copy(
                isTraining = false,
                logs = it.logs + ">> Training stopped by user."
            )
        }
    }

    private fun formatLogLine(metrics: TrainingProgressMetrics): String {
        val totalSecs = metrics.elapsedTimeMs / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val timeStr = "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
        val lossStr = if (metrics.loss == 0.0) "N/A" else formatDouble(metrics.loss, 5)
        val rewardStr = formatDouble(metrics.averageReward, 2)
        val epsStr = formatDouble(metrics.epsilon, 2)
        val spsStr = formatDouble(metrics.stepsPerSecond, 1)

        return "[$timeStr] Episode #${metrics.episode.toString().padStart(4, '0')}: " +
                "Score = ${metrics.recentScore.toString().padStart(2, '0')}, " +
                "Top = ${metrics.topScore.toString().padStart(2, '0')}, " +
                "Loss = $lossStr, " +
                "Avg Reward = $rewardStr, " +
                "Epsilon = $epsStr, " +
                "Steps/s = $spsStr"
    }



    private fun GameState.toGameContractState(topScore: Int): GameContract.State {
        return GameContract.State(
            score = this.score,
            topScore = topScore,
            steps = this.steps,
            status = this.status,
            direction = this.direction,
            fieldSize = this.fieldSize,
            selectedFieldSize = this.fieldSize,
            snake = this.snake,
            food = this.food
        )
    }

    override fun onCleared() {
        super.onCleared()
        trainJob?.cancel()
    }
}
