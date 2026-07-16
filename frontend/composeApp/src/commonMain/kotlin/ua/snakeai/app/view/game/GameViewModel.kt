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

        val width = actualSize.width
        val height = actualSize.height

        // Variable starting length: randomized between 3 and 6
        val initialLength = Random.nextInt(3, 7)
        val initialHeading = Direction.entries.random()

        val cx = width / 2
        val cy = height / 2

        val snakeBody = mutableListOf<Coordinate>()
        for (i in 0 until initialLength) {
            val segment = when (initialHeading) {
                Direction.RIGHT -> Coordinate(cx - i, cy)
                Direction.LEFT -> Coordinate(cx + i, cy)
                Direction.UP -> Coordinate(cx, cy + i)
                Direction.DOWN -> Coordinate(cx, cy - i)
            }
            // Ensure bounds safety during spawn
            val boundedX = segment.x.coerceIn(0, width - 1)
            val boundedY = segment.y.coerceIn(0, height - 1)
            snakeBody.add(Coordinate(boundedX, boundedY))
        }

        val foodCoord = generateSafeFood(snakeBody, width, height) ?: Coordinate(0, 0)

        updateState {
            it.copy(
                score = 0,
                steps = 0,
                status = GameStatus.IDLE,
                direction = initialHeading,
                fieldSize = actualSize,
                snake = snakeBody,
                food = foodCoord
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
        val state = currentState
        if (state.status != GameStatus.PLAYING) return

        val snakeBody = state.snake
        val head = snakeBody.firstOrNull() ?: return
        val dir = state.direction

        // Compute new head coordinate
        val newHead = when (dir) {
            Direction.UP -> Coordinate(head.x, head.y - 1)
            Direction.DOWN -> Coordinate(head.x, head.y + 1)
            Direction.LEFT -> Coordinate(head.x - 1, head.y)
            Direction.RIGHT -> Coordinate(head.x + 1, head.y)
        }

        val width = state.fieldSize.width
        val height = state.fieldSize.height

        // Strict wall collision check
        if (newHead.x < 0 || newHead.x >= width || newHead.y < 0 || newHead.y >= height) {
            updateState { it.copy(status = GameStatus.GAME_OVER) }
            return
        }

        // Self-collision check (if we are eating, tail doesn't move, otherwise tail is removed)
        val isEating = (newHead == state.food)
        val bodyToCollide = if (isEating) snakeBody else snakeBody.dropLast(1)
        if (bodyToCollide.contains(newHead)) {
            updateState { it.copy(status = GameStatus.GAME_OVER) }
            return
        }

        val newSnake = mutableListOf<Coordinate>()
        newSnake.add(newHead)

        if (isEating) {
            newSnake.addAll(snakeBody)
            val newScore = state.score + 1
            val newTopScore = maxOf(state.topScore, newScore)
            val nextFood = generateSafeFood(newSnake, width, height)

            if (nextFood == null) {
                // Victory! Snake filled the entire board
                updateState {
                    it.copy(
                        snake = newSnake,
                        score = newScore,
                        topScore = newTopScore,
                        steps = state.steps + 1,
                        status = GameStatus.VICTORY
                    )
                }
            } else {
                updateState {
                    it.copy(
                        snake = newSnake,
                        food = nextFood,
                        score = newScore,
                        topScore = newTopScore,
                        steps = state.steps + 1
                    )
                }
            }
        } else {
            newSnake.addAll(snakeBody.dropLast(1))
            updateState {
                it.copy(
                    snake = newSnake,
                    steps = state.steps + 1
                )
            }
        }
    }

    private fun generateSafeFood(snake: List<Coordinate>, width: Int, height: Int): Coordinate? {
        val occupied = snake.toSet()
        val emptyCells = mutableListOf<Coordinate>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                val coord = Coordinate(x, y)
                if (!occupied.contains(coord)) {
                    emptyCells.add(coord)
                }
            }
        }
        if (emptyCells.isEmpty()) return null
        return emptyCells.random()
    }

    private fun loadTopScore() {
        viewModelScope.launch {
            try {
                val model = repository.getAiModel()
                updateState { it.copy(topScore = model.topScore) }
            } catch (e: Exception) {
                // Ignore and use default
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
