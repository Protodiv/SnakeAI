package ua.protodiv.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

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
            // Render placeholder or main content for default startup screen
        }
    }
}
