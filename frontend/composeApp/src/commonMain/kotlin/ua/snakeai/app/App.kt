package ua.snakeai.app

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.PreComposeApp
import ua.snakeai.app.ui.theme.SnakeAITheme
import ua.snakeai.app.navigation.NavigationRoot

@Composable
fun App() {
    PreComposeApp {
        SnakeAITheme {
            Scaffold { paddingValues ->
                NavigationRoot(paddingValues = paddingValues)
            }
        }
    }
}