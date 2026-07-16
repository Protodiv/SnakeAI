package ua.snakeai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Cold Cyber Palette
val DeepSpaceBlue = Color(0xFF0B132B)
val SlateNavy = Color(0xFF1C2541)
val GlassFill = Color(0xB31C2541) // #1C2541B3 (RGBA) -> ARGB: 0xB31C2541
val GlassBorder = Color(0xFF3A506B)
val GridCell = Color(0xFF1F2D3D)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8E9AAF)
val CyberCyan = Color(0xFF00F2FE)
val NeonBlue = Color(0xFF4FACFE)
val CrimsonRed = Color(0xFFE63946)
val NeonGreen = Color(0xFF00F5D4)

@Immutable
data class CyberColors(
    val backgroundStart: Color = DeepSpaceBlue,
    val backgroundEnd: Color = SlateNavy,
    val glassFill: Color = GlassFill,
    val glassBorder: Color = GlassBorder,
    val gridCell: Color = GridCell,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val highlightStart: Color = CyberCyan,
    val highlightEnd: Color = NeonBlue,
    val snakeHead: Color = CrimsonRed,
    val snakeBodyStart: Color = CyberCyan,
    val snakeBodyEnd: Color = NeonBlue,
    val apple: Color = NeonGreen
) {
    val backgroundGradient: Brush
        get() = Brush.linearGradient(listOf(backgroundStart, backgroundEnd))

    val highlightGradient: Brush
        get() = Brush.linearGradient(listOf(highlightStart, highlightEnd))

    val snakeBodyGradient: Brush
        get() = Brush.linearGradient(listOf(snakeBodyStart, snakeBodyEnd))
}

val LocalCyberColors = staticCompositionLocalOf { CyberColors() }

val MaterialTheme.cyberColors: CyberColors
    @Composable
    get() = LocalCyberColors.current