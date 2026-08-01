package ua.snakeai.app.view.main.mainmenu

import androidx.compose.runtime.Immutable
import ua.snakeai.app.core.mvi.UiEffect
import ua.snakeai.app.core.mvi.UiEvent
import ua.snakeai.app.core.mvi.UiState
import ua.snakeai.contract.TrainedAiModel

interface MainMenuContract {
    @Immutable
    data class State(
        val modelInfo: TrainedAiModel? = null,
        val isLoading: Boolean = false,
        val isSystemOnline: Boolean = true
    ) : UiState

    sealed interface Event : UiEvent {
        data object OnPlayManualClicked : Event
        data object OnTrainDqnClicked : Event
        data object OnPlayAiClicked : Event
        data object OnRefreshModelClicked : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
    }
}
