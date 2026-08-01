package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.CyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun HyperparameterChoiceChip(
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    cyberColors: CyberColors,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) cyberColors.highlightStart.copy(alpha = 0.2f)
                else cyberColors.backgroundStart
            )
            .border(
                1.dp,
                if (isSelected) cyberColors.highlightStart
                else cyberColors.glassBorder.copy(alpha = 0.5f),
                MaterialTheme.shapes.small
            )
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) cyberColors.highlightStart else if (isEnabled) Color.White else cyberColors.textSecondary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
