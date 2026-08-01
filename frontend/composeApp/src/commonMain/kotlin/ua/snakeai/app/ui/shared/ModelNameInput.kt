package ua.snakeai.app.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun ModelNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.xxxs)
    ) {
        Text("Model Name:", color = cyberColors.textSecondary, fontSize = 10.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White.copy(alpha = 0.5f),
                focusedBorderColor = cyberColors.highlightStart,
                unfocusedBorderColor = cyberColors.glassBorder,
                disabledBorderColor = cyberColors.glassBorder.copy(alpha = 0.5f),
                focusedContainerColor = cyberColors.backgroundStart,
                unfocusedContainerColor = cyberColors.backgroundStart,
                disabledContainerColor = cyberColors.backgroundStart.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(spacing.xxs),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )
    }
}

