package ua.snakeai.backend.exception

import org.springframework.http.HttpStatus

open class BaseServiceException(
    val statusCode: Int,
    val errorCode: String = "SERVICE_ERROR",
    override val message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ResourceNotFoundException(message: String) : BaseServiceException(
    statusCode = HttpStatus.NOT_FOUND.value(),
    errorCode = "NOT_FOUND",
    message = message
)

class ModelSaveException(message: String, cause: Throwable? = null) : BaseServiceException(
    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
    errorCode = "MODEL_SAVE_ERROR",
    message = message,
    cause = cause
)

class ModelDeleteException(message: String, cause: Throwable? = null) : BaseServiceException(
    statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
    errorCode = "MODEL_DELETE_ERROR",
    message = message,
    cause = cause
)
