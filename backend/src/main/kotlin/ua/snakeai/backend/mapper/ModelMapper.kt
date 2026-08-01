package ua.snakeai.backend.mapper

import ua.snakeai.backend.repository.TrainedModelEntity
import ua.snakeai.contract.TrainedAiModel

fun TrainedModelEntity.toDto(): TrainedAiModel {
    return TrainedAiModel(
        name = this.name,
        episodesRun = this.episodesRun,
        efficiency = this.efficiency,
        topScore = this.topScore
    )
}
