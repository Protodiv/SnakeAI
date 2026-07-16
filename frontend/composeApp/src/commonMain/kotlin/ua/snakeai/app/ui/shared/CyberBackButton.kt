package ua.snakeai.app.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun CyberBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "BACK TO MENU"
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.xs, vertical = spacing.xxs)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = text,
            tint = cyberColors.highlightStart,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(spacing.xs))
        Text(
            text = text,
            color = cyberColors.highlightStart,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
    }
}
