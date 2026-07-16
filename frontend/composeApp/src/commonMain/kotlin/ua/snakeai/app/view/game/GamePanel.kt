package ua.snakeai.app.view.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import ua.snakeai.app.ui.shared.GameOverlay
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.contract.Direction
import ua.snakeai.contract.FieldSize
import ua.snakeai.contract.GameStatus

@Composable
fun GamePanel(
    state: GameContract.State,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    // Pulsing animation for overlays
    val infiniteTransition = rememberInfiniteTransition(label = "game_panel_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val gridWidth = state.fieldSize.width
        val gridHeight = state.fieldSize.height

        Canvas(modifier = Modifier.fillMaxSize().padding(spacing.xs)) {
            val canvasW = size.width
            val canvasH = size.height

            val cellSizeW = canvasW / gridWidth
            val cellSizeH = canvasH / gridHeight
            val cellSize = minOf(cellSizeW, cellSizeH)

            // Center the grid within the canvas
            val offsetX = (canvasW - (cellSize * gridWidth)) / 2f
            val offsetY = (canvasH - (cellSize * gridHeight)) / 2f

            val cellSpacing = when (state.fieldSize) {
                FieldSize.SMALL -> 2.dp.toPx()
                FieldSize.MEDIUM -> 1.2.dp.toPx()
                FieldSize.LARGE -> 0.6.dp.toPx()
                FieldSize.RANDOM -> 1.2.dp.toPx()
            }

            val innerCellSize = cellSize - cellSpacing

            // 1. Draw Grid Cells
            for (col in 0 until gridWidth) {
                for (row in 0 until gridHeight) {
                    val x = offsetX + col * cellSize + cellSpacing / 2f
                    val y = offsetY + row * cellSize + cellSpacing / 2f
                    drawRoundRect(
                        color = cyberColors.gridCell.copy(alpha = 0.4f),
                        topLeft = Offset(x, y),
                        size = Size(innerCellSize, innerCellSize),
                        cornerRadius = CornerRadius(innerCellSize * 0.15f, innerCellSize * 0.15f)
                    )
                }
            }

            // 2. Draw Food (Apple) with pulse glow
            val food = state.food
            if (food.x in 0 until gridWidth && food.y in 0 until gridHeight) {
                val x = offsetX + food.x * cellSize + cellSize / 2f
                val y = offsetY + food.y * cellSize + cellSize / 2f
                val baseRadius = innerCellSize / 2f

                // Outer glow
                drawCircle(
                    color = cyberColors.apple.copy(alpha = 0.25f * pulseAlpha),
                    radius = baseRadius * 1.5f,
                    center = Offset(x, y)
                )

                // Main body
                drawCircle(
                    color = cyberColors.apple,
                    radius = baseRadius * 0.75f,
                    center = Offset(x, y)
                )

                // Highlight gloss
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = baseRadius * 0.2f,
                    center = Offset(x - baseRadius * 0.2f, y - baseRadius * 0.2f)
                )
            }

            // 3. Draw Snake
            val snake = state.snake
            if (snake.isNotEmpty()) {
                // Draw Body Segments (gradient and connections)
                val bodySize = snake.size - 1
                for (i in 1 until snake.size) {
                    val segment = snake[i]
                    if (segment.x in 0 until gridWidth && segment.y in 0 until gridHeight) {
                        val x = offsetX + segment.x * cellSize + cellSpacing / 2f
                        val y = offsetY + segment.y * cellSize + cellSpacing / 2f

                        // Color interpolation for gradient effect
                        val fraction = if (bodySize > 1) (i - 1).toFloat() / (bodySize - 1) else 0f
                        val bodyColor = lerp(cyberColors.snakeBodyStart, cyberColors.snakeBodyEnd, fraction)

                        drawRoundRect(
                            color = bodyColor,
                            topLeft = Offset(x, y),
                            size = Size(innerCellSize, innerCellSize),
                            cornerRadius = CornerRadius(innerCellSize * 0.2f, innerCellSize * 0.2f)
                        )
                    }
                }

                // Draw Head
                val head = snake.first()
                if (head.x in 0 until gridWidth && head.y in 0 until gridHeight) {
                    val hx = offsetX + head.x * cellSize + cellSpacing / 2f
                    val hy = offsetY + head.y * cellSize + cellSpacing / 2f

                    // Draw main head rect
                    drawRoundRect(
                        color = cyberColors.snakeHead,
                        topLeft = Offset(hx, hy),
                        size = Size(innerCellSize, innerCellSize),
                        cornerRadius = CornerRadius(innerCellSize * 0.35f, innerCellSize * 0.35f)
                    )

                    // Draw eyes representing the current direction
                    val eyeSize = innerCellSize * 0.15f
                    val eyeColor = Color.White
                    val centerH = hx + innerCellSize / 2f
                    val centerV = hy + innerCellSize / 2f

                    val offset = innerCellSize * 0.22f

                    when (state.direction) {
                        Direction.UP -> {
                            drawCircle(eyeColor, eyeSize, Offset(centerH - offset, hy + offset))
                            drawCircle(eyeColor, eyeSize, Offset(centerH + offset, hy + offset))
                        }
                        Direction.DOWN -> {
                            drawCircle(eyeColor, eyeSize, Offset(centerH - offset, hy + innerCellSize - offset))
                            drawCircle(eyeColor, eyeSize, Offset(centerH + offset, hy + innerCellSize - offset))
                        }
                        Direction.LEFT -> {
                            drawCircle(eyeColor, eyeSize, Offset(hx + offset, centerV - offset))
                            drawCircle(eyeColor, eyeSize, Offset(hx + offset, centerV + offset))
                        }
                        Direction.RIGHT -> {
                            drawCircle(eyeColor, eyeSize, Offset(hx + innerCellSize - offset, centerV - offset))
                            drawCircle(eyeColor, eyeSize, Offset(hx + innerCellSize - offset, centerV + offset))
                        }
                    }
                }
            }
        }

        // 4. Overlays based on game status
        when (state.status) {
            GameStatus.IDLE -> {
                GameOverlay(
                    title = "READY PLAYER ONE",
                    titleColor = cyberColors.highlightStart,
                    subtitle = "PRESS ANY MOVEMENT KEY TO INITIATE",
                    backgroundColor = Color.Black.copy(alpha = 0.55f),
                    instructionText = "[ W / A / S / D  or  ARROWS ]",
                    instructionColor = cyberColors.textSecondary,
                    pulseAlpha = pulseAlpha
                )
            }
            GameStatus.GAME_OVER -> {
                GameOverlay(
                    title = "GAME OVER",
                    titleColor = cyberColors.snakeHead,
                    subtitle = "CRITICAL COLLISION ENCOUNTERED",
                    backgroundColor = cyberColors.gameOverOverlay,
                    instructionText = "CLICK RESTART TO RETRY",
                    instructionColor = cyberColors.highlightStart,
                    pulseAlpha = pulseAlpha
                )
            }
            GameStatus.VICTORY -> {
                GameOverlay(
                    title = "VICTORY",
                    titleColor = cyberColors.apple,
                    subtitle = "GRID SYSTEM FULLY RESTORED",
                    backgroundColor = cyberColors.victoryOverlay,
                    instructionText = "Score: ${state.score} | Steps: ${state.steps}",
                    instructionColor = cyberColors.textSecondary,
                    pulseAlpha = 1.0f
                )
            }
            GameStatus.PLAYING -> {
                // No overlay while playing
            }
        }
    }
}
