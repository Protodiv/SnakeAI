package ua.snakeai.app.di.network

import org.koin.dsl.module
import org.koin.core.qualifier.named
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import ua.snakeai.app.di.httpClient
import ua.snakeai.app.data.api.ApiException
import ua.snakeai.contract.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.errors.IOException

val clientModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single(named("ServerHost")) { "localhost" }
    single(named("ServerPort")) { "8080" }

    single<HttpClient> {
        httpClient(get()) {
            expectSuccess = false

            install(WebSockets)

            install(Logging) {
                level = LogLevel.ALL
            }

            defaultRequest {
                val hostStr = get<String>(named("ServerHost"))
                val portStr = get<String>(named("ServerPort"))
                url("http://$hostStr:$portStr")
                contentType(ContentType.Application.Json)
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    throw when (cause) {
                        is ClientRequestException -> {
                            val response = cause.response
                            val errorBody = try {
                                response.body<ErrorResponse>()
                            } catch (_: Exception) {
                                null
                            }
                            ApiException.ServerError(
                                statusCode = response.status.value,
                                response = errorBody
                            )
                        }
                        is ServerResponseException -> {
                            val response = cause.response
                            val errorBody = try {
                                response.body<ErrorResponse>()
                            } catch (_: Exception) {
                                null
                            }
                            ApiException.ServerError(
                                statusCode = response.status.value,
                                response = errorBody
                            )
                        }
                        is IOException -> {
                            ApiException.NetworkError(cause.message)
                        }
                        is SerializationException -> {
                            ApiException.Serialization(cause.message)
                        }
                        else -> {
                            ApiException.UnknownError(cause.message)
                        }
                    }
                }
            }
        }
    }
}

val apiModule = module {
    includes(clientModule)
}
