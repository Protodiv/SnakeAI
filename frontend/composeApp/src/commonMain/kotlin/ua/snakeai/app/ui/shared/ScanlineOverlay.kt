package ua.snakeai.app.ui.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import ua.snakeai.app.ui.theme.cyberColors

@Composable
fun ScanlineOverlay(
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val y = scanlineY * size.height
        drawLine(
            color = cyberColors.highlightStart.copy(alpha = 0.1f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.dp.toPx()
        )
    }
}
