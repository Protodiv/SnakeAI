package ua.snakeai.app.core.mvi

import androidx.compose.runtime.Immutable
import moe.tlaster.precompose.navigation.Navigator

@Immutable
interface UiState

interface UiEvent

interface UiEffect

sealed interface NavigationEffect : UiEffect {
    data object Back : NavigationEffect
    data class ToRoute(val route: String) : NavigationEffect
}

fun Navigator.handle(effect: NavigationEffect) {
    when (effect) {
        NavigationEffect.Back -> popBackStack()
        is NavigationEffect.ToRoute -> navigate(effect.route)
    }
}
