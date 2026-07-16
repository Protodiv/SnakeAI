package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun KeyInstructions(
    keys: ImmutableList<String>,
    instructionText: String,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        keys.forEach { key ->
            Box(
                modifier = Modifier
                    .background(cyberColors.glassFill, MaterialTheme.shapes.small)
                    .border(1.dp, cyberColors.glassBorder, MaterialTheme.shapes.small)
                    .padding(horizontal = spacing.xs, vertical = spacing.xxs)
            ) {
                Text(
                    text = key,
                    color = cyberColors.highlightStart,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(spacing.xxs))
        Text(
            text = instructionText,
            color = cyberColors.textSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
