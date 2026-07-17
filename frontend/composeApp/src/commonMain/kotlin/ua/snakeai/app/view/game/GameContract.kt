package ua.snakeai.app.view.game

import androidx.compose.runtime.Immutable
import ua.snakeai.app.core.mvi.UiEffect
import ua.snakeai.app.core.mvi.UiEvent
import ua.snakeai.app.core.mvi.UiState
import ua.snakeai.contract.Coordinate
import ua.snakeai.contract.Direction
import ua.snakeai.contract.FieldSize
import ua.snakeai.contract.GameStatus

interface GameContract {
    @Immutable
    data class State(
        val score: Int = 0,
        val topScore: Int = 0,
        val steps: Int = 0,
        val status: GameStatus = GameStatus.IDLE,
        val direction: Direction = Direction.RIGHT,
        val fieldSize: FieldSize = FieldSize.MEDIUM,
        val selectedFieldSize: FieldSize = FieldSize.MEDIUM,
        val snake: List<Coordinate> = emptyList(),
        val food: Coordinate = Coordinate(0, 0)
    ) : UiState {
        val isGameOver: Boolean get() = status == GameStatus.GAME_OVER
        val isVictory: Boolean get() = status == GameStatus.VICTORY
        val isPlaying: Boolean get() = status == GameStatus.PLAYING
    }

    sealed interface Event : UiEvent {
        data class OnDirectionChanged(val direction: Direction) : Event
        data object OnRestartClicked : Event
        data class OnFieldSizeConfigChanged(val size: FieldSize) : Event
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackBar(val message: String) : Effect
    }
}
