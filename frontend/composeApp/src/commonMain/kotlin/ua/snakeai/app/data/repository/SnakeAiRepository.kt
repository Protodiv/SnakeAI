package ua.snakeai.app.data.repository

import kotlinx.coroutines.flow.Flow
import ua.snakeai.app.data.api.AppResult
import ua.snakeai.contract.*

interface SnakeAiRepository {
    suspend fun getAiModel(name: String): AppResult<TrainedAiModel>
    suspend fun getAiModels(): AppResult<List<TrainedAiModel>>
    fun playAi(modelName: String, fieldSize: FieldSize, tickRateMs: Long): Flow<GameFrame>
    fun trainAi(modelName: String, fieldSize: FieldSize, hyperparameters: TrainHyperparameters): Flow<TrainingMetricsFrame>
}
