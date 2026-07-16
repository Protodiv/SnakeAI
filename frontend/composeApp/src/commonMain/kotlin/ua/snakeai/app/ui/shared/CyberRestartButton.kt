package ua.snakeai.app.ui.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun CyberRestartButton(
    onClick: () -> Unit,
    isFinished: Boolean,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    // Pulse scale animation when the game is finished
    val infiniteTransition = rememberInfiniteTransition(label = "restart_button_pulse")
    val buttonScale by if (isFinished) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "button_scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val btnColor = if (isFinished) cyberColors.highlightStart else cyberColors.glassBorder
    val contentColor = if (isFinished) cyberColors.backgroundStart else Color.White

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = btnColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(spacing.xs),
        modifier = modifier
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .fillMaxWidth()
            .height(42.dp)
            .border(
                1.dp,
                if (isFinished) cyberColors.highlightStart else cyberColors.glassBorder,
                RoundedCornerShape(spacing.xs)
            )
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Restart",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(spacing.xs))
        Text(
            text = "RESTART PROTOCOL",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        )
    }
}
