package ua.protodiv.app.di.network

import org.koin.dsl.module
import kotlinx.serialization.json.Json
import ua.protodiv.app.di.httpClient

val clientModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    single { httpClient(get()) }
}

val apiModule = module {
    includes(clientModule)
}
