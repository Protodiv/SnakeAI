package ua.snakeai.backend.exception

import org.springframework.http.HttpStatus

open class BaseServiceException(
    val statusCode: Int,
    val errorCode: String = "SERVICE_ERROR",
    override val message: String
) : RuntimeException(message)

class ResourceNotFoundException(message: String) : BaseServiceException(
    statusCode = HttpStatus.NOT_FOUND.value(),
    errorCode = "NOT_FOUND",
    message = message
)
