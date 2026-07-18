package ua.snakeai.app

import androidx.compose.runtime.Composable
import ua.snakeai.app.di.initKoin
import ua.snakeai.app.screens.SnakeAiApp
import ua.snakeai.app.ui.theme.SnakeAITheme

@Composable
fun App() {
    initKoin()
    SnakeAITheme {
        SnakeAiApp()
    }
}