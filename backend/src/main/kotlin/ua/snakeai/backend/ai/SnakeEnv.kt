package ua.snakeai.backend.ai

import ua.snakeai.contract.*
import kotlin.math.abs

object SnakeEnv {

    fun getObservation(state: GameState): DoubleArray {
        val head = state.snake.firstOrNull() ?: Coordinate(0, 0)
        val dir = state.direction
        val body = state.snake.drop(1).toSet()
        val width = state.fieldSize.width
        val height = state.fieldSize.height

        // 1. Danger sensors (Straight, Left, Right)
        val straightCoord = getNextCoordinate(head, dir)
        val leftCoord = getNextCoordinate(head, leftOf(dir))
        val rightCoord = getNextCoordinate(head, rightOf(dir))

        val dangerStraight = if (isDangerous(straightCoord, body, width, height)) 1.0 else 0.0
        val dangerLeft = if (isDangerous(leftCoord, body, width, height)) 1.0 else 0.0
        val dangerRight = if (isDangerous(rightCoord, body, width, height)) 1.0 else 0.0

        // 2. Heading directions (UP, DOWN, LEFT, RIGHT)
        val headingUp = if (dir == Direction.UP) 1.0 else 0.0
        val headingDown = if (dir == Direction.DOWN) 1.0 else 0.0
        val headingLeft = if (dir == Direction.LEFT) 1.0 else 0.0
        val headingRight = if (dir == Direction.RIGHT) 1.0 else 0.0

        // 3. Food relative directions (North, East, South, West)
        val food = state.food
        val foodNorth = if (food.y < head.y) 1.0 else 0.0
        val foodSouth = if (food.y > head.y) 1.0 else 0.0
        val foodWest = if (food.x < head.x) 1.0 else 0.0
        val foodEast = if (food.x > head.x) 1.0 else 0.0

        return doubleArrayOf(
            dangerStraight,
            dangerLeft,
            dangerRight,
            headingUp,
            headingDown,
            headingLeft,
            headingRight,
            foodNorth,
            foodEast,
            foodSouth,
            foodWest
        )
    }

    fun getAbsoluteDirection(currentDir: Direction, relativeAction: Int): Direction {
        return when (relativeAction) {
            0 -> currentDir // STRAIGHT
            1 -> leftOf(currentDir) // LEFT
            2 -> rightOf(currentDir) // RIGHT
            else -> currentDir
        }
    }

    fun getReward(prevState: GameState, nextState: GameState): Double {
        if (nextState.status == GameStatus.GAME_OVER) return -10.0
        if (nextState.status == GameStatus.VICTORY) return 10.0
        if (nextState.score > prevState.score) return 10.0

        // Reward shaping based on Manhattan distance to food
        val prevHead = prevState.snake.firstOrNull() ?: return 0.0
        val newHead = nextState.snake.firstOrNull() ?: return 0.0
        val food = prevState.food

        val prevDist = abs(prevHead.x - food.x) + abs(prevHead.y - food.y)
        val newDist = abs(newHead.x - food.x) + abs(newHead.y - food.y)

        return if (newDist < prevDist) 1.0 else -1.5
    }

    private fun getNextCoordinate(head: Coordinate, dir: Direction): Coordinate {
        return when (dir) {
            Direction.UP -> Coordinate(head.x, head.y - 1)
            Direction.DOWN -> Coordinate(head.x, head.y + 1)
            Direction.LEFT -> Coordinate(head.x - 1, head.y)
            Direction.RIGHT -> Coordinate(head.x + 1, head.y)
        }
    }

    private fun isDangerous(coord: Coordinate, body: Set<Coordinate>, width: Int, height: Int): Boolean {
        // Wall boundary danger
        if (coord.x < 0 || coord.x >= width || coord.y < 0 || coord.y >= height) return true
        // Self-collision danger
        if (body.contains(coord)) return true
        return false
    }

    private fun leftOf(dir: Direction): Direction {
        return when (dir) {
            Direction.UP -> Direction.LEFT
            Direction.DOWN -> Direction.RIGHT
            Direction.LEFT -> Direction.DOWN
            Direction.RIGHT -> Direction.UP
        }
    }

    private fun rightOf(dir: Direction): Direction {
        return when (dir) {
            Direction.UP -> Direction.RIGHT
            Direction.DOWN -> Direction.LEFT
            Direction.LEFT -> Direction.UP
            Direction.RIGHT -> Direction.DOWN
        }
    }
}
