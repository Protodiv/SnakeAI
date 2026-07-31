package ua.snakeai.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun ConsoleLogPanel(
    logs: List<String>,
    placeholderText: String,
    title: String,
    onClearLogsClicked: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            lazyListState.animateScrollToItem(logs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.md))
            .background(cyberColors.backgroundStart.copy(alpha = 0.95f))
            .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.md))
            .padding(spacing.cardPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = cyberColors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear logs",
                    tint = cyberColors.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onClearLogsClicked() }
                )
            }

            Divider(color = cyberColors.glassBorder.copy(alpha = 0.3f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(spacing.xs))

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = placeholderText,
                            color = cyberColors.textSecondary.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = if (log.contains("ERROR")) cyberColors.snakeHead else cyberColors.apple,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
