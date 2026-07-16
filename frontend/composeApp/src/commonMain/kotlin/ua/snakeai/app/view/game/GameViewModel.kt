package ua.snakeai.app.view.game

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.snakeai.app.core.mvi.BaseViewModel
import ua.snakeai.app.data.repository.SnakeAiRepository

class GameViewModel(
    private val repository: SnakeAiRepository
) : BaseViewModel<GameContract.State, GameContract.Event, GameContract.Effect>(
    GameContract.State()
) {
    override val state: StateFlow<GameContract.State> = _state
        .onStart {
            loadTopScore()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    override fun onEvent(event: GameContract.Event) {
        when (event) {
            is GameContract.Event.OnDirectionChanged -> {
                val currentDir = currentState.direction
                val newDir = event.direction
                val isOpposite = when (newDir) {
                    GameContract.Direction.UP -> currentDir == GameContract.Direction.DOWN
                    GameContract.Direction.DOWN -> currentDir == GameContract.Direction.UP
                    GameContract.Direction.LEFT -> currentDir == GameContract.Direction.RIGHT
                    GameContract.Direction.RIGHT -> currentDir == GameContract.Direction.LEFT
                }
                if (!isOpposite && !currentState.isGameOver) {
                    updateState { it.copy(direction = newDir) }
                }
            }
            GameContract.Event.OnRestartClicked -> {
                updateState {
                    it.copy(
                        score = 0,
                        steps = 0,
                        isGameOver = false,
                        direction = GameContract.Direction.RIGHT
                    )
                }
            }
        }
    }

    private fun loadTopScore() {
        viewModelScope.launch {
            try {
                val model = repository.getAiModel()
                updateState { it.copy(topScore = model.topScore) }
            } catch (e: Exception) {
                // Keep default top score or 0
            }
        }
    }
}
