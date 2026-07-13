package ua.protodiv.app

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import moe.tlaster.precompose.PreComposeApp
import ua.cryptoflow.app.ui.theme.SnakeAITheme
import ua.protodiv.app.navigation.NavigationRoot

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