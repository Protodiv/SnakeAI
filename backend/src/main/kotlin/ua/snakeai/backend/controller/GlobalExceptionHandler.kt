package ua.snakeai.backend.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import ua.snakeai.backend.exception.BaseServiceException
import ua.snakeai.contract.ErrorResponse
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        val status = ex.statusCode.value()
        if (ex.statusCode.is5xxServerError) {
            logger.error("Response status exception: {} {}", ex.statusCode, ex.reason ?: ex.message, ex)
        } else {
            logger.warn("Response status exception: {} {}", ex.statusCode, ex.reason ?: ex.message)
        }

        val code = when (status) {
            HttpStatus.NOT_FOUND.value() -> "NOT_FOUND"
            HttpStatus.METHOD_NOT_ALLOWED.value() -> "METHOD_NOT_ALLOWED"
            HttpStatus.BAD_REQUEST.value() -> "BAD_REQUEST"
            else -> "ERROR_$status"
        }

        return buildResponse(
            statusCode = status,
            errorCode = code,
            message = ex.reason ?: ex.message ?: "Response status exception",
            exchange = exchange
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception occurred: {}", ex.message, ex)
        return buildResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            errorCode = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred. Please try again later.",
            exchange = exchange
        )
    }

    @ExceptionHandler(BaseServiceException::class)
    fun handleBaseServiceException(ex: BaseServiceException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        return buildResponse(
            statusCode = ex.statusCode,
            errorCode = ex.errorCode,
            message = ex.message,
            exchange = exchange
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException, exchange: ServerWebExchange): ResponseEntity<ErrorResponse> {
        return buildResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            errorCode = "NOT_FOUND",
            message = ex.message ?: "Resource not found",
            exchange = exchange
        )
    }

    private fun buildResponse(
        statusCode: Int,
        errorCode: String,
        message: String,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        val httpStatus = HttpStatus.resolve(statusCode) ?: HttpStatus.INTERNAL_SERVER_ERROR
        val response = ErrorResponse(
            message = message,
            code = errorCode,
            timestamp = Instant.now().toString(),
            status = statusCode,
            error = httpStatus.reasonPhrase,
            path = exchange.request.path.value()
        )
        return ResponseEntity(response, httpStatus)
    }
}
