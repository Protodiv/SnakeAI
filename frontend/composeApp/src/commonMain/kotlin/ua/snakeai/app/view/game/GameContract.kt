package ua.snakeai.app.view.game

import androidx.compose.runtime.Immutable
import ua.snakeai.app.core.mvi.UiEffect
import ua.snakeai.app.core.mvi.UiEvent
import ua.snakeai.app.core.mvi.UiState

interface GameContract {
    @Immutable
    data class State(
        val score: Int = 0,
        val topScore: Int = 0,
        val steps: Int = 0,
        val isGameOver: Boolean = false,
        val direction: Direction = Direction.RIGHT
    ) : UiState

    sealed interface Event : UiEvent {
        data class OnDirectionChanged(val direction: Direction) : Event
        data object OnRestartClicked : Event
    }

    sealed interface Effect : UiEffect

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
