package ua.snakeai.app.domain.feature.snakeai.error

import ua.snakeai.app.data.api.CommonError
import ua.snakeai.app.data.api.DomainError

sealed interface SnakeAIError : DomainError {
    data class AiModelsNotFound(override val message: String) : SnakeAIError
    data class ApiError(override val message: String) : SnakeAIError
}

fun DomainError.toSnakeAIError(): SnakeAIError =
    when (this) {
        is CommonError.NotFound -> SnakeAIError.AiModelsNotFound(this.message)
        else -> SnakeAIError.ApiError(this.message)
    }