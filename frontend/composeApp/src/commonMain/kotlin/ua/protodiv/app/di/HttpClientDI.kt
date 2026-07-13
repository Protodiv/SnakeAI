package ua.protodiv.app.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlinx.serialization.json.Json

expect fun httpClient(json:Json, config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
