package ua.snakeai.app.data.api

import io.ktor.client.HttpClient
import ua.snakeai.contract.TrainedAiModel

class SnakeAiApi(
    private val httpClient: HttpClient
) {
    suspend fun getAiModel(): TrainedAiModel {
        // Return mocked data directly for now
        return TrainedAiModel(
            name = "Agent Alpha",
            episodesRun = 1200000L,
            efficiency = 98.4,
            topScore = 428
        )
    }
}
