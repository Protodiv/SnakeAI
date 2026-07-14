package ua.snakeai.app.data.repository

import ua.snakeai.app.data.api.SnakeAiApi
import ua.snakeai.contract.TrainedAiModel

class SnakeAiRepositoryImp(
    private val api: SnakeAiApi
) : SnakeAiRepository {
    override suspend fun getAiModel(): TrainedAiModel {
        return api.getAiModel()
    }
}
