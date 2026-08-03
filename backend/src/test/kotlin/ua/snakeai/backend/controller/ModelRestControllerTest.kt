package ua.snakeai.backend.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import ua.snakeai.backend.service.TrainModelService
import ua.snakeai.contract.TrainedAiModel

// SIMPLE TEST FOR TEST COVERAGE TODO(REWORK)
@WebFluxTest(ModelRestController::class)
class ModelRestControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var trainModelService: TrainModelService

    @Test
    fun testListModels() {
        val mockModels = listOf(
            TrainedAiModel(
                name = "test-agent",
                episodesRun = 100,
                efficiency = 0.85,
                topScore = 15,
            )
        )
        `when`(trainModelService.listModels()).thenReturn(mockModels)

        webTestClient.get()
            .uri("/api/models")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].name").isEqualTo("test-agent")
            .jsonPath("$[0].episodesRun").isEqualTo(100)
            .jsonPath("$[0].efficiency").isEqualTo(0.85)
            .jsonPath("$[0].topScore").isEqualTo(15)
    }

    @Test
    fun testNotFoundResource() {
        webTestClient.get()
            .uri("/api/non-existent-url-that-does-not-exist")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.code").isEqualTo("NOT_FOUND")
            .jsonPath("$.message").isNotEmpty
            .jsonPath("$.path").isEqualTo("/api/non-existent-url-that-does-not-exist")
    }
}
