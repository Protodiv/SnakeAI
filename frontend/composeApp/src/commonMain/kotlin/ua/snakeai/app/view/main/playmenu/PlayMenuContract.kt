package ua.snakeai.app.view.main.playmenu

import androidx.compose.runtime.Immutable
import ua.snakeai.app.core.mvi.UiEffect
import ua.snakeai.app.core.mvi.UiEvent
import ua.snakeai.app.core.mvi.UiState

interface PlayMenuContract {
    @Immutable
    data class State(
        val protocol: String = "Manual_Override",
        val agent: String = "Human_H01"
    ) : UiState

    sealed interface Event : UiEvent

    sealed interface Effect : UiEffect
}
