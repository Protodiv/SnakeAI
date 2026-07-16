package ua.snakeai.app.view.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing

@Composable
fun GamePanel(
    initialDirection: GameContract.Direction,
    onStateChanged: (GameContract.State) -> Unit,
    onArenaClicked: (GameContract.Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GameViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Sync initialDirection parameter with the ViewModel's state
    LaunchedEffect(initialDirection) {
        viewModel.onEvent(GameContract.Event.OnDirectionChanged(initialDirection))
    }

    // Propagate state updates back to the parent screen
    LaunchedEffect(state) {
        onStateChanged(state)
    }

    GamePanelContent(
        state = state,
        onDirectionChanged = { viewModel.onEvent(GameContract.Event.OnDirectionChanged(it)) },
        onArenaClicked = { onArenaClicked(state.direction) },
        modifier = modifier
    )
}

@Composable
fun GamePanelContent(
    state: GameContract.State,
    onDirectionChanged: (GameContract.Direction) -> Unit,
    onArenaClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val direction = when (keyEvent.key) {
                        Key.W, Key.DirectionUp -> GameContract.Direction.UP
                        Key.S, Key.DirectionDown -> GameContract.Direction.DOWN
                        Key.A, Key.DirectionLeft -> GameContract.Direction.LEFT
                        Key.D, Key.DirectionRight -> GameContract.Direction.RIGHT
                        else -> null
                    }
                    if (direction != null) {
                        onDirectionChanged(direction)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            .clip(RoundedCornerShape(spacing.xs))
            .background(cyberColors.glassFill)
            .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.xs))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
                onArenaClicked()
            },
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GAME ARENA",
                color = cyberColors.highlightStart,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.height(spacing.st))
            Text(
                text = "[ PLACEHOLDER ]",
                color = cyberColors.textSecondary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
