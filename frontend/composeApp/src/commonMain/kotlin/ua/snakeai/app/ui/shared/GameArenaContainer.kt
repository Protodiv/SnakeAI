package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.app.view.game.GamePanel

@Composable
fun GameArenaContainer(
    state: GameContract.State,
    protocolText: String,
    agentNameText: String,
    modifier: Modifier = Modifier,
    innerModifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Box(
        modifier = modifier
    ) {
        // Inner container for clipping, background, border, and custom focus/events passed via innerModifier
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(innerModifier)
                .clip(RoundedCornerShape(spacing.xs))
                .background(cyberColors.glassFill)
                .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.xs))
        ) {
            GamePanel(
                state = state,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Corner Badges (placed on the unclipped parent Box)
        CornerBadge(
            text = protocolText,
            textColor = cyberColors.highlightStart,
            backgroundColor = cyberColors.glassBorder,
            borderColor = cyberColors.highlightStart,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-8).dp, y = (-8).dp)
        )

        CornerBadge(
            text = agentNameText,
            textColor = Color.White,
            backgroundColor = cyberColors.snakeHead.copy(alpha = 0.9f),
            borderColor = cyberColors.snakeHead,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
        )
    }
}
