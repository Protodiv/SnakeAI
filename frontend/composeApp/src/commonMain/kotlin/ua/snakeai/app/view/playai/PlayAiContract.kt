package ua.snakeai.app.view.playai

import androidx.compose.runtime.Immutable
import ua.snakeai.app.core.mvi.*
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.contract.TrainedAiModel

interface PlayAiContract {
    @Immutable
    data class State(
        val isPlaying: Boolean = false,
        val agentNameText: String = "",
        val availableAgents: List<TrainedAiModel> = emptyList(),
        val filteredAgents: List<TrainedAiModel> = emptyList(),
        val selectedAgent: TrainedAiModel? = null,
        val gameState: GameContract.State? = null,
        val logs: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    ) : UiState

    sealed interface Event : UiEvent {
        data object OnStartClicked : Event
        data object OnStopClicked : Event
        data class OnAgentNameChanged(val name: String) : Event
        data class OnAgentSelected(val agent: TrainedAiModel) : Event
        data object OnClearLogsClicked : Event
        data object OnLoadAgents : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackBar(val message: String) : Effect
    }
}
