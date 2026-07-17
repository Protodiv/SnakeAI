package ua.snakeai.app.data.api

import ua.snakeai.contract.ErrorResponse

interface DomainError

enum class CommonError : DomainError {
    NoInternet,
    ServerUnavailable,
    SessionExpired,
    AccessDenied,
    NotFound,
    Unknown
}

sealed class ApiException : Exception() {
    data class ServerError(
        val statusCode: Int,
        val response: ErrorResponse?
    ) : ApiException()

    data class NetworkError(override val message: String?) : ApiException()
    data class Timeout(override val message: String?) : ApiException()
    data class Serialization(override val message: String?) : ApiException()
    data class UnknownError(override val message: String?) : ApiException()
}

fun ApiException.ServerError.toCommonError(): DomainError {
    return when (statusCode) {
        401 -> CommonError.SessionExpired
        403 -> CommonError.AccessDenied
        404 -> CommonError.NotFound
        in 500..599 -> CommonError.ServerUnavailable
        else -> CommonError.Unknown
    }
}
