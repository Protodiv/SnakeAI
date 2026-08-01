package ua.snakeai.app.ui.shared

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun AgentPlayButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    Button(
        onClick = {
            if (isPlaying) {
                onStopClicked()
            } else {
                onStartClicked()
            }
        },
        enabled = !isLoading || isPlaying,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPlaying) cyberColors.snakeHead else cyberColors.highlightStart
        ),
        shape = RoundedCornerShape(spacing.xs),
        modifier = modifier.fillMaxWidth().height(38.dp)
    ) {
        if (isLoading && isPlaying) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.sm),
                color = Color.White,
                strokeWidth = spacing.xxxs
            )
            Spacer(modifier = Modifier.width(spacing.xs))
            Text("LINKING...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        } else {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Play Control",
                tint = if (isPlaying) Color.White else cyberColors.backgroundStart,
                modifier = Modifier.size(spacing.sm)
            )
            Spacer(modifier = Modifier.width(spacing.xs))
            Text(
                text = if (isPlaying) "DISCONNECT AGENT" else "INITIALIZE AGENT",
                color = if (isPlaying) Color.White else cyberColors.backgroundStart,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
