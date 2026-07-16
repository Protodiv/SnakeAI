package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.contract.Direction

@Composable
fun ActiveDirBadge(
    direction: Direction,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(cyberColors.glassBorder.copy(alpha = 0.5f))
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        val icon = when (direction) {
            Direction.UP -> Icons.Default.ArrowUpward
            Direction.DOWN -> Icons.Default.ArrowDownward
            Direction.LEFT -> Icons.Default.ArrowBack
            Direction.RIGHT -> Icons.Default.ArrowForward
        }

        Icon(
            imageVector = icon,
            contentDescription = "Direction",
            tint = cyberColors.highlightStart,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(spacing.xs))

        Text(
            text = "DIR: $direction",
            color = cyberColors.textPrimary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}
