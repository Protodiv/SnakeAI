package ua.snakeai.backend.exception

import org.springframework.http.HttpStatus

open class BaseServiceException(
    val statusCode: Int,
    override val message: String
) : RuntimeException(message)

class ResourceNotFoundException(message: String) : BaseServiceException(HttpStatus.NOT_FOUND.value(), message)
