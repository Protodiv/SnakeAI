package ua.snakeai.contract

import kotlinx.serialization.Serializable

@Serializable
data class TrainedAiModel(
    val name: String,
    val episodesRun: Long,
    val efficiency: Double,
    val topScore: Int
)
