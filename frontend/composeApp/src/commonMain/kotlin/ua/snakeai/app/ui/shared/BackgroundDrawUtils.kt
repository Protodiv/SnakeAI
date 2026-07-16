package ua.snakeai.app.ui.shared

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawBackgroundCircle(
    backgroundStart: Color,
    backgroundEnd: Color
) {
    drawRect(color = backgroundStart)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(backgroundEnd, Color.Transparent),
            center = Offset(size.width / 2f, -size.height * 0.2f),
            radius = size.height * 0.7f
        ),
        radius = size.height * 0.7f,
        center = Offset(size.width / 2f, -size.height * 0.2f)
    )
}
