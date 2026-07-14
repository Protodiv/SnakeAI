package ua.snakeai.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import ua.snakeai.app.screens.main.MainMenuScene

@Composable
fun NavigationRoot(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberNavigator()

    NavHost(
        navigator = navigator,
        modifier = modifier,
        initialRoute = NavigationRoute.MainRoute.MainScreen.route,
    ) {
        scene(route = NavigationRoute.MainRoute.MainScreen.route) {
            MainMenuScene(navigator = { navigator })
        }
        scene(route = NavigationRoute.MainRoute.PlayManualScreen.route) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("Play Manual Screen (Placeholder)", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        scene(route = NavigationRoute.MainRoute.TrainDqnScreen.route) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("Train DQN Screen (Placeholder)", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        scene(route = NavigationRoute.MainRoute.PlayAiScreen.route) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("Play AI Showcase (Placeholder)", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
