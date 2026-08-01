package ua.snakeai.app.data.repository

import kotlinx.coroutines.flow.Flow
import ua.snakeai.app.data.api.AppResult
import ua.snakeai.app.data.api.SnakeAiApi
import ua.snakeai.app.data.api.safeApiCall
import ua.snakeai.app.data.api.mapError
import ua.snakeai.contract.*

class SnakeAiRepositoryImp(
    private val api: SnakeAiApi
) : SnakeAiRepository {
    override suspend fun getAiModel(name: String): AppResult<TrainedAiModel> = safeApiCall {
        api.getAiModel(name)
    }.mapError { it }

    override suspend fun getAiModels(): AppResult<List<TrainedAiModel>> = safeApiCall {
        api.getAiModels()
    }.mapError { it }

    override fun playAi(modelName: String, fieldSize: FieldSize, tickRateMs: Long): Flow<GameFrame> {
        return api.playAi(modelName, fieldSize, tickRateMs)
    }

    override fun trainAi(modelName: String, fieldSize: FieldSize, hyperparameters: TrainHyperparameters): Flow<TrainingMetricsFrame> {
        return api.trainAi(modelName, fieldSize, hyperparameters)
    }
}
