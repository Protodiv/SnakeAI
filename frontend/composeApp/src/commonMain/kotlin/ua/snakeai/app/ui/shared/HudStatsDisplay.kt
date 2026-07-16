package ua.snakeai.app.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun HudStatsDisplay(
    score: Int,
    topScore: Int,
    steps: Int,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        HudStatItem(
            label = "SCORE",
            value = score.toString().padStart(3, '0'),
            color = cyberColors.highlightStart
        )

        HudStatItem(
            label = "TOP SCORE",
            value = topScore.toString().padStart(3, '0'),
            color = cyberColors.snakeHead
        )

        HudStatItem(
            label = "STEPS",
            value = steps.toString().padStart(4, '0'),
            color = cyberColors.highlightStart
        )
    }
}

@Composable
private fun HudStatItem(
    label: String,
    value: String,
    color: Color
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Text(
            text = label,
            color = cyberColors.textSecondary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}
