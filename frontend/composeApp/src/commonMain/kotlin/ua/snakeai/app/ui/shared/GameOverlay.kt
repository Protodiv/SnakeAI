package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.spacing

@Composable
fun GameOverlay(
    title: String,
    titleColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = 0.6f),
    instructionText: String? = null,
    instructionColor: Color = Color.White,
    pulseAlpha: Float = 1f
) {
    val spacing = MaterialTheme.spacing

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(spacing.md)
        ) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            )
            if (instructionText != null) {
                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    text = instructionText,
                    color = instructionColor.copy(alpha = pulseAlpha),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
