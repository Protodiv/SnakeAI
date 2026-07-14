package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ua.snakeai.app.ui.theme.cyberColors

data class StatItem(
    val value: String,
    val label: String,
    val valueColor: Color
)

@Composable
fun FooterStatsPanel(
    stats: ImmutableList<StatItem>,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors

    Row(
        modifier = modifier
            .widthIn(max = 500.dp)
            .fillMaxWidth(0.9f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stats.forEachIndexed { index, stat ->
            FooterStatItem(
                value = stat.value,
                label = stat.label,
                valueColor = stat.valueColor
            )

            if (index < stats.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(cyberColors.glassBorder.copy(alpha = 0.3f))
                )
            }
        }
    }
}
