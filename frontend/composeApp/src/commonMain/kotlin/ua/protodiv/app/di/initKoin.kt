package ua.protodiv.app.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import ua.protodiv.app.di.network.apiModule

fun initKoin(config: KoinAppDeclaration? = null){
    startKoin {
        config?.invoke(this)
        modules(
            apiModule,
            applicationModule
        )
    }
}
