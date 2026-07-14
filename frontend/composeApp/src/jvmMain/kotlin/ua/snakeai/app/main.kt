package ua.snakeai.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ua.snakeai.app.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Snake AI",
        ) {
            App()
        }
    }
}