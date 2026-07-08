package ua.cryptoflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import ui.compose.theme.Typography

private val DarkColorPalette = darkColorScheme(
    primary = CoralRed,
    onPrimary = Gunmetal,
//    primaryContainer = TODO(),
//    onPrimaryContainer = TODO(),
//    inversePrimary = TODO(),
    secondary = WarmGray,
//    onSecondary = TODO(),
//    secondaryContainer = TODO(),
//    onSecondaryContainer = TODO(),
//    tertiary = TODO(),
//    onTertiary = TODO(),
//    tertiaryContainer = TODO(),
//    onTertiaryContainer = TODO(),
//    background = TODO(),
//    onBackground = TODO(),
//    surface = TODO(),
//    onSurface = TODO(),
//    surfaceVariant = TODO(),
//    onSurfaceVariant = TODO(),
//    surfaceTint = TODO(),
//    inverseSurface = TODO(),
//    inverseOnSurface = TODO(),
//    error = TODO(),
//    onError = TODO(),
//    errorContainer = TODO(),
//    onErrorContainer = TODO(),
//    outline = TODO(),
//    outlineVariant = TODO(),
//    scrim = TODO(),
//    surfaceBright = TODO(),
//    surfaceContainer = TODO(),
//    surfaceContainerHigh = TODO(),
//    surfaceContainerHighest = TODO(),
//    surfaceContainerLow = TODO(),
//    surfaceContainerLowest = TODO(),
//    surfaceDim = TODO()
)

private val LightColorPalette = lightColorScheme(
    primary = SoftBeige,
    onPrimary = Gunmetal,
//    primaryContainer = TODO(),
//    onPrimaryContainer = TODO(),
//    inversePrimary = TODO(),
    secondary = WarmGray,
//    onSecondary = TODO(),
//    secondaryContainer = TODO(),
//    onSecondaryContainer = TODO(),
//    tertiary = TODO(),
//    onTertiary = TODO(),
//    tertiaryContainer = TODO(),
//    onTertiaryContainer = TODO(),
//    background = TODO(),
//    onBackground = TODO(),
//    surface = TODO(),
//    onSurface = TODO(),
//    surfaceVariant = TODO(),
//    onSurfaceVariant = TODO(),
//    surfaceTint = TODO(),
//    inverseSurface = TODO(),
//    inverseOnSurface = TODO(),
//    error = TODO(),
//    onError = TODO(),
//    errorContainer = TODO(),
//    onErrorContainer = TODO(),
//    outline = TODO(),
//    outlineVariant = TODO(),
//    scrim = TODO(),
//    surfaceBright = TODO(),
//    surfaceContainer = TODO(),
//    surfaceContainerHigh = TODO(),
//    surfaceContainerHighest = TODO(),
//    surfaceContainerLow = TODO(),
//    surfaceContainerLowest = TODO(),
//    surfaceDim = TODO()
)

@Composable
fun SnakeAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}