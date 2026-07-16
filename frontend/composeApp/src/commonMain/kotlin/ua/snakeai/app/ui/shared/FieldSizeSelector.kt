package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.contract.FieldSize

@Composable
fun FieldSizeSelector(
    selectedConfig: FieldSize,
    onConfigChanged: (FieldSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Column(modifier = modifier) {
        Text(
            text = "GRID SYSTEM CONFIG",
            color = cyberColors.textSecondary,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(spacing.xs))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(spacing.xs))
                .background(cyberColors.glassFill)
                .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.xs))
                .padding(spacing.xxxs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxxs)
        ) {
            FieldSize.entries.forEach { config ->
                val isActive = selectedConfig == config
                val label = when (config) {
                    FieldSize.SMALL -> "16²"
                    FieldSize.MEDIUM -> "32²"
                    FieldSize.LARGE -> "64²"
                    FieldSize.RANDOM -> "RAND"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(spacing.xxs))
                        .background(if (isActive) cyberColors.highlightStart else Color.Transparent)
                        .clickable { onConfigChanged(config) }
                        .padding(vertical = spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isActive) cyberColors.backgroundStart else cyberColors.textPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}
