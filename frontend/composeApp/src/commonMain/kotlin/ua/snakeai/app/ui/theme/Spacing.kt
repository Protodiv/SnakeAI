package ua.snakeai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

object Spacing {
    // Design System Spacing Tokens
    val grid = 8.dp
    val cardPadding = 24.dp
    val elementGap = 16.dp

    // Compatibility styles
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val st = 12.dp
    val sm = 16.dp
    val md = 24.dp
    val lg = 32.dp
    val xl = 48.dp
    val xxl = 64.dp
    val xxxl = 80.dp
}

val MaterialTheme.spacing: Spacing get() = Spacing