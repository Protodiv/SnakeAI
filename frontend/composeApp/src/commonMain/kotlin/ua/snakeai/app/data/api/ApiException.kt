package ua.snakeai.app.data.api

import ua.snakeai.contract.ErrorResponse

interface DomainError {
    val message: String
}

sealed interface CommonError : DomainError {
    data object NoInternet : CommonError {
        override val message: String = "No internet connection. Please check your network."
    }
    data object ServerUnavailable : CommonError {
        override val message: String = "Server is currently unavailable. Please try again later."
    }
    data object SessionExpired : CommonError {
        override val message: String = "Your session has expired. Please log in again."
    }
    data object AccessDenied : CommonError {
        override val message: String = "Access denied. You do not have permission to view this resource."
    }
    data class NotFound(override val message: String = "The requested resource was not found.") : CommonError
    data class Unknown(override val message: String = "An unknown error occurred.") : CommonError
    data class ApiError(val statusCode: Int, override val message: String) : CommonError
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
    val backendMsg = response?.message ?: response?.error ?: "Server error ($statusCode)"
    return when (statusCode) {
        401 -> CommonError.SessionExpired
        403 -> CommonError.AccessDenied
        404 -> CommonError.NotFound(backendMsg)
        in 500..599 -> CommonError.ServerUnavailable // Or we can return ApiError
        else -> CommonError.ApiError(statusCode, backendMsg)
    }
}
