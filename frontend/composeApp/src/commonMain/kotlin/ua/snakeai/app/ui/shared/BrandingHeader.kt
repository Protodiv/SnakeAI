package ua.snakeai.app.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.ui.theme.cyberColors

@Composable
fun BrandingHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge.copy(
                brush = cyberColors.highlightGradient,
                letterSpacing = 4.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            text = subtitle,
            color = cyberColors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
