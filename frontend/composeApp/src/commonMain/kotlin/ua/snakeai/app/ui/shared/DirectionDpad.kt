package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.game.GameContract

@Composable
fun DirectionDpad(
    currentDirection: GameContract.Direction,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Up button
        DpadButton(
            icon = Icons.Default.ArrowUpward,
            isActive = currentDirection == GameContract.Direction.UP
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left button
            DpadButton(
                icon = Icons.Default.ArrowBack,
                isActive = currentDirection == GameContract.Direction.LEFT
            )

            Spacer(modifier = Modifier.width(spacing.md))

            // Right button
            DpadButton(
                icon = Icons.Default.ArrowForward,
                isActive = currentDirection == GameContract.Direction.RIGHT
            )
        }

        // Down button
        DpadButton(
            icon = Icons.Default.ArrowDownward,
            isActive = currentDirection == GameContract.Direction.DOWN
        )
    }
}

@Composable
private fun DpadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isActive) cyberColors.highlightStart else cyberColors.glassFill)
            .border(
                1.dp,
                if (isActive) cyberColors.highlightStart else cyberColors.glassBorder,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) cyberColors.backgroundStart else cyberColors.highlightStart,
            modifier = Modifier.size(20.dp)
        )
    }
}
