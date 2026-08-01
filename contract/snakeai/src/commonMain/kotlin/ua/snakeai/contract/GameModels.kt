package ua.snakeai.contract

import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(val x: Int, val y: Int)

@Serializable
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

@Serializable
enum class GameStatus {
    IDLE, PLAYING, GAME_OVER, VICTORY
}

@Serializable
enum class FieldSize(val width: Int, val height: Int) {
    SMALL(16, 16),
    MEDIUM(32, 32),
    LARGE(64, 64),
    RANDOM(0, 0)
}

@Serializable
data class GameState(
    val score: Int = 0,
    val steps: Int = 0,
    val status: GameStatus = GameStatus.IDLE,
    val direction: Direction = Direction.RIGHT,
    val fieldSize: FieldSize = FieldSize.MEDIUM,
    val snake: List<Coordinate> = emptyList(),
    val food: Coordinate = Coordinate(0, 0)
)

