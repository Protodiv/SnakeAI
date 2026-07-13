package ua.protodiv.app.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun httpClient(json:Json, config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(CIO) {
        config(this)
        install(ContentNegotiation) {
            json(json)
        }
    }
