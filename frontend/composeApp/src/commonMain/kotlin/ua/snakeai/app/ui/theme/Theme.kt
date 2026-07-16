package ua.snakeai.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DeepSpaceBlue,
    secondary = NeonBlue,
    onSecondary = DeepSpaceBlue,
    background = DeepSpaceBlue,
    onBackground = TextPrimary,
    surface = SlateNavy,
    onSurface = TextPrimary,
    surfaceVariant = GlassFill,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = CrimsonRed,
    onError = TextPrimary
)

@Composable
fun SnakeAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Parameter kept for signature compatibility
    content: @Composable () -> Unit
) {
    val cyberColors = CyberColors()

    CompositionLocalProvider(
        LocalCyberColors provides cyberColors
    ) {
        MaterialTheme(
            colorScheme = CyberColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}