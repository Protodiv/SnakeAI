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
