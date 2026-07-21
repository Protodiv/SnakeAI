package ua.snakeai.app.view.train

import androidx.compose.runtime.Immutable
import ua.snakeai.app.core.mvi.*
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.contract.*

interface TrainDqnContract {
    @Immutable
    data class State(
        val isTraining: Boolean = false,
        val modelName: String = "Agent_default",
        val fieldSize: FieldSize = FieldSize.RANDOM,
        val hyperparameters: TrainHyperparameters = TrainHyperparameters(),
        val metrics: TrainingProgressMetrics? = null,
        val gameState: GameContract.State? = null,
        val availableLearningRates: List<Double> = listOf(0.001, 0.0005, 0.0001),
        val availableBatchSizes: List<Int> = listOf(32, 64, 128),
        val logs: List<String> = emptyList(),
        val error: String? = null
    ) : UiState

    sealed interface Event : UiEvent {
        data object OnStartTrainingClicked : Event
        data object OnStopTrainingClicked : Event
        data class OnModelNameChanged(val name: String) : Event
        data class OnHyperparametersChanged(val params: TrainHyperparameters) : Event
        data class OnLogReceived(val logLine: String) : Event
        data object OnClearLogsClicked : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
    }
}
