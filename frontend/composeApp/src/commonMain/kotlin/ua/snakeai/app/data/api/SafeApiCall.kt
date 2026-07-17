package ua.snakeai.app.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val error: DomainError) : AppResult<Nothing>()
}

suspend fun <T> safeApiCall(
    call: suspend () -> T
): AppResult<T> {
    return withContext(Dispatchers.Default){
        try {
            AppResult.Success(call())
        } catch (e: ApiException.ServerError) {
            AppResult.Error(e.toCommonError())
        } catch (e: ApiException.NetworkError) {
            AppResult.Error(CommonError.NoInternet)
        } catch (e: ApiException.Timeout) {
            AppResult.Error(CommonError.ServerUnavailable)
        } catch (_: ApiException) {
            AppResult.Error(CommonError.Unknown)
        }
    }
}

inline fun <T> AppResult<T>.mapError(
    transform: (DomainError) -> DomainError
): AppResult<T> {
    return when (this) {
        is AppResult.Success -> this
        is AppResult.Error -> AppResult.Error(
            transform(error)
        )
    }
}
