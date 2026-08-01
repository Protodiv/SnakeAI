package ua.snakeai.app.view.game

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import ua.snakeai.app.core.mvi.BaseViewModel
import ua.snakeai.app.data.repository.SnakeAiRepository
import ua.snakeai.contract.*
import kotlin.random.Random

import ua.snakeai.app.data.api.AppResult

class GameViewModel(
    private val repository: SnakeAiRepository
) : BaseViewModel<GameContract.State, GameContract.Event, GameContract.Effect>(
    GameContract.State()
) {
    private var gameLoopJob: Job? = null

    override val state: StateFlow<GameContract.State> = _state
        .onStart {
            resetGame(currentState.selectedFieldSize)
            loadTopScore()
            startGameLoop()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = _state.value
        )

    override fun onEvent(event: GameContract.Event) {
        when (event) {
            is GameContract.Event.OnDirectionChanged -> {
                handleDirectionChange(event.direction)
            }
            GameContract.Event.OnRestartClicked -> {
                resetGame(currentState.selectedFieldSize)
            }
            is GameContract.Event.OnFieldSizeConfigChanged -> {
                updateState { it.copy(selectedFieldSize = event.size) }
                resetGame(event.size)
            }
        }
    }

    private fun handleDirectionChange(newDir: Direction) {
        val currentStatus = currentState.status
        if (currentStatus == GameStatus.IDLE) {
            // First click on any direction starts the game!
            // Ensure the starting direction doesn't instantly cause a self-collision (e.g. 180 turn)
            val isOppositeOfInitialHeading = isOppositeDirection(currentState.direction, newDir)
            val finalDir = if (isOppositeOfInitialHeading) currentState.direction else newDir
            updateState {
                it.copy(
                    direction = finalDir,
                    status = GameStatus.PLAYING
                )
            }
        } else if (currentState.isPlaying) {
            val currentDir = currentState.direction
            if (!isOppositeDirection(currentDir, newDir)) {
                updateState { it.copy(direction = newDir) }
            }
        }
    }

    private fun isOppositeDirection(d1: Direction, d2: Direction): Boolean {
        return when (d1) {
            Direction.UP -> d2 == Direction.DOWN
            Direction.DOWN -> d2 == Direction.UP
            Direction.LEFT -> d2 == Direction.RIGHT
            Direction.RIGHT -> d2 == Direction.LEFT
        }
    }

    private fun resetGame(selectedSize: FieldSize) {
        val actualSize = when (selectedSize) {
            FieldSize.SMALL -> FieldSize.SMALL
            FieldSize.MEDIUM -> FieldSize.MEDIUM
            FieldSize.LARGE -> FieldSize.LARGE
            FieldSize.RANDOM -> {
                val sizes = listOf(FieldSize.SMALL, FieldSize.MEDIUM, FieldSize.LARGE)
                sizes.random()
            }
        }

        val initialLength = Random.nextInt(3, 7)
        val initialHeading = Direction.entries.random()

        val engineState = GameEngine.initGame(actualSize, initialLength, initialHeading, Random.Default)

        updateState {
            it.copy(
                score = engineState.score,
                steps = engineState.steps,
                status = engineState.status,
                direction = engineState.direction,
                fieldSize = engineState.fieldSize,
                snake = engineState.snake,
                food = engineState.food
            )
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                if (currentState.status == GameStatus.PLAYING) {
                    val delayMs = when (currentState.fieldSize) {
                        FieldSize.SMALL -> 180L
                        FieldSize.MEDIUM -> 120L
                        FieldSize.LARGE -> 85L
                        FieldSize.RANDOM -> 120L
                    }
                    delay(delayMs)
                    tickGame()
                } else {
                    delay(100L)
                }
            }
        }
    }

    private fun tickGame() {
        if (currentState.status != GameStatus.PLAYING) return
        val currentEngineState = GameState(
            score = currentState.score,
            steps = currentState.steps,
            status = currentState.status,
            direction = currentState.direction,
            fieldSize = currentState.fieldSize,
            snake = currentState.snake,
            food = currentState.food
        )
        val nextEngineState = GameEngine.step(currentEngineState, currentState.direction)
        val newTopScore = maxOf(currentState.topScore, nextEngineState.score)
        updateState {
            it.copy(
                score = nextEngineState.score,
                topScore = newTopScore,
                steps = nextEngineState.steps,
                status = nextEngineState.status,
                direction = nextEngineState.direction,
                fieldSize = nextEngineState.fieldSize,
                snake = nextEngineState.snake,
                food = nextEngineState.food
            )
        }
    }


    private fun loadTopScore() {
        viewModelScope.launch {
            when (val result = repository.getAiModels()) {
                is AppResult.Success -> {
                    val model = result.data.firstOrNull()
                    if (model != null) {
                        updateState { it.copy(topScore = model.topScore) }
                    }
                }
                is AppResult.Error -> {
                    emitEffect(GameContract.Effect.ShowSnackBar("Failed to load top score: ${result.error}"))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
