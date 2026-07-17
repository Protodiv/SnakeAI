package ua.snakeai.app.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import ua.snakeai.contract.*

class SnakeAiApi(
    private val httpClient: HttpClient,
    private val json: Json,
    private val serverHost: String,
    private val serverPort: String
) {
    companion object {
        private const val BASE_PATH = "/api/models"
        const val MODELS = BASE_PATH
        fun modelPath(name: String) = "$BASE_PATH/$name"
    }

    suspend fun getAiModels(): List<TrainedAiModel> =
        httpClient
            .get(MODELS)
            .body()

    suspend fun getAiModel(name: String): TrainedAiModel =
        httpClient
            .get(modelPath(name))
            .body()

    fun playAi(
        modelName: String,
        fieldSize: FieldSize,
        tickRateMs: Long
    ): Flow<GameFrame> = flow {
        httpClient.webSocket(host = serverHost, port = serverPort.toInt(), path = "/ws/ai/play") {
            val startCmd = PlayCommand(
                action = "START",
                modelName = modelName,
                fieldSize = fieldSize,
                tickRateMs = tickRateMs
            )
            send(Frame.Text(json.encodeToString(startCmd)))

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val frameText = frame.readText()
                    val gameFrame = json.decodeFromString<GameFrame>(frameText)
                    emit(gameFrame)
                }
            }
        }
    }

    fun trainAi(
        modelName: String,
        fieldSize: FieldSize,
        hyperparameters: TrainHyperparameters
    ): Flow<TrainingMetricsFrame> = flow {
        httpClient.webSocket(host = serverHost, port = serverPort.toInt(), path = "/ws/ai/train") {
            val startCmd = TrainCommand(
                action = "START_TRAINING",
                modelName = modelName,
                fieldSize = fieldSize,
                hyperparameters = hyperparameters
            )
            send(Frame.Text(json.encodeToString(startCmd)))

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val frameText = frame.readText()
                    val metricsFrame = json.decodeFromString<TrainingMetricsFrame>(frameText)
                    emit(metricsFrame)
                }
            }
        }
    }
}
