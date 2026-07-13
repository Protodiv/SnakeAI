package ua.protodiv.app.navigation

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
    }
}
