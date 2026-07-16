package ua.snakeai.app.navigation

import androidx.compose.runtime.Immutable

@Immutable
interface TopLevelRoute {
    val route: String
    val title: String
}

sealed class NavigationRoute : TopLevelRoute {
    @Immutable
    sealed class MainRoute : NavigationRoute() {
        internal data object MainScreen : MainRoute() {
            override val route: String = "/main-home"
            override val title: String = "Home"
        }
        internal data object PlayManualScreen : MainRoute() {
            override val route: String = "/play-manual"
            override val title: String = "Play Manual"
        }
        internal data object TrainDqnScreen : MainRoute() {
            override val route: String = "/train-dqn"
            override val title: String = "Train DQN"
        }
        internal data object PlayAiScreen : MainRoute() {
            override val route: String = "/play-ai"
            override val title: String = "Play AI"
        }
    }
}
