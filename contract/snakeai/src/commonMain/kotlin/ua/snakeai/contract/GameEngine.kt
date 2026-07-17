package ua.snakeai.contract

import kotlin.random.Random

object GameEngine {

    fun step(state: GameState, nextDirection: Direction): GameState {
        if (state.status != GameStatus.PLAYING) return state

        val snakeBody = state.snake
        val head = snakeBody.firstOrNull() ?: return state

        // Compute new head coordinate
        val newHead = when (nextDirection) {
            Direction.UP -> Coordinate(head.x, head.y - 1)
            Direction.DOWN -> Coordinate(head.x, head.y + 1)
            Direction.LEFT -> Coordinate(head.x - 1, head.y)
            Direction.RIGHT -> Coordinate(head.x + 1, head.y)
        }

        val width = state.fieldSize.width
        val height = state.fieldSize.height

        // Strict wall collision check
        if (newHead.x < 0 || newHead.x >= width || newHead.y < 0 || newHead.y >= height) {
            return state.copy(status = GameStatus.GAME_OVER)
        }

        // Self-collision check (if we are eating, tail doesn't move, otherwise tail is removed)
        val isEating = (newHead == state.food)
        val bodyToCollide = if (isEating) snakeBody else snakeBody.dropLast(1)
        if (bodyToCollide.contains(newHead)) {
            return state.copy(status = GameStatus.GAME_OVER)
        }

        val newSnake = mutableListOf<Coordinate>()
        newSnake.add(newHead)

        return if (isEating) {
            newSnake.addAll(snakeBody)
            val newScore = state.score + 1
            val nextFood = generateSafeFood(newSnake, width, height)

            if (nextFood == null) {
                // Victory! Snake filled the entire board
                state.copy(
                    snake = newSnake,
                    score = newScore,
                    steps = state.steps + 1,
                    status = GameStatus.VICTORY
                )
            } else {
                state.copy(
                    snake = newSnake,
                    food = nextFood,
                    score = newScore,
                    steps = state.steps + 1,
                    direction = nextDirection
                )
            }
        } else {
            newSnake.addAll(snakeBody.dropLast(1))
            state.copy(
                snake = newSnake,
                steps = state.steps + 1,
                direction = nextDirection
            )
        }
    }

    fun generateSafeFood(snake: List<Coordinate>, width: Int, height: Int): Coordinate? {
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

    fun generateSafeFoodWithRandom(snake: List<Coordinate>, width: Int, height: Int, random: Random): Coordinate? {
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
        return emptyCells.random(random)
    }

    fun initGame(fieldSize: FieldSize, initialLength: Int, initialHeading: Direction, random: Random): GameState {
        val width = fieldSize.width
        val height = fieldSize.height

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

        val foodCoord = generateSafeFoodWithRandom(snakeBody, width, height, random) ?: Coordinate(0, 0)

        return GameState(
            score = 0,
            steps = 0,
            status = GameStatus.IDLE,
            direction = initialHeading,
            fieldSize = fieldSize,
            snake = snakeBody,
            food = foodCoord
        )
    }
}
