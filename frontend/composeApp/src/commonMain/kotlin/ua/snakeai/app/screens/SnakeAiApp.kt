package ua.snakeai.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ua.snakeai.app.navigation.NavigationRoute.MainRoute
import ua.snakeai.app.screens.main.MainMenuScene
import ua.snakeai.app.screens.main.PlayManualScene
import ua.snakeai.app.screens.main.TrainDqnScene

@Composable
fun SnakeAiApp(
    navController: NavHostController = rememberNavController()
) {
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.MainScreen.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = MainRoute.MainScreen.route) {
                MainMenuScene(navigator = navController)
            }
            composable(route = MainRoute.PlayManualScreen.route) {
                PlayManualScene(navigator = navController)
            }
            composable(route = MainRoute.TrainDqnScreen.route) {
                TrainDqnScene(navigator = navController)
            }
            composable(route = MainRoute.PlayAiScreen.route) {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Play AI Showcase (Placeholder)", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}