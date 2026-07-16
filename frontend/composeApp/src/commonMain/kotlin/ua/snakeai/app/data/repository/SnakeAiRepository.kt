package ua.snakeai.app.data.repository

import ua.snakeai.contract.TrainedAiModel

interface SnakeAiRepository {
    suspend fun getAiModel(): TrainedAiModel
}
