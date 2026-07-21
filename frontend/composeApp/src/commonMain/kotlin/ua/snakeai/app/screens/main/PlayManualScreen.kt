package ua.snakeai.app.screens.main

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.ui.shared.*
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.app.view.game.GamePanel
import ua.snakeai.app.view.game.GameViewModel
import ua.snakeai.contract.Direction
import ua.snakeai.contract.GameStatus

@Composable
fun PlayManualScene(
    navigator: NavHostController
) {
    val gameViewModel: GameViewModel = koinViewModel()
    val state by gameViewModel.state.collectAsStateWithLifecycle()

    PlayManualScreen(
        state = state,
        effect = { gameViewModel.effect },
        onEvent = gameViewModel::onEvent,
        onBackClicked = navigator::popBackStack
    )
}

@Composable
fun PlayManualScreen(
    state: GameContract.State,
    effect: () -> Flow<GameContract.Effect>,
    onEvent: (GameContract.Event) -> Unit,
    onBackClicked: () -> Unit
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    val keyList = persistentListOf("W", "A", "S", "D")
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(effect) {
        effect().collectLatest { item ->
            when (item) {
                is GameContract.Effect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(item.message)
                }
            }
        }
    }

    // Automatically request keyboard focus when game launches or restarts
    LaunchedEffect(state.status) {
        if (state.status == GameStatus.PLAYING || state.status == GameStatus.IDLE) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawBackgroundCircle(
                    backgroundStart = cyberColors.backgroundStart,
                    backgroundEnd = cyberColors.backgroundEnd
                )
            }
    ) {
        ScanlineOverlay(infiniteTransition = rememberInfiniteTransition(label = "scanline_play_manual"))

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            CyberHeader(
                onBackClicked = onBackClicked
            ) {
                // Stats / HUD Display
                HudStatsDisplay(
                    score = state.score,
                    topScore = state.topScore,
                    steps = state.steps
                )
            }

            // Main Content Section (Row layout placing Game Arena and Side Control Panel side-by-side)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(spacing.cardPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.widthIn(max = 760.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: The square game arena container
                    GameArenaContainer(
                        state = state,
                        protocolText = "Protocol: Manual_Override",
                        agentNameText = "Agent: Human_H01",
                        modifier = Modifier
                            .weight(1.3f)
                            .aspectRatio(1f),
                        innerModifier = Modifier
                            .focusRequester(focusRequester)
                            .focusable()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    val direction = when (keyEvent.key) {
                                        Key.W, Key.DirectionUp -> Direction.UP
                                        Key.S, Key.DirectionDown -> Direction.DOWN
                                        Key.A, Key.DirectionLeft -> Direction.LEFT
                                        Key.D, Key.DirectionRight -> Direction.RIGHT
                                        else -> null
                                    }
                                    if (direction != null) {
                                        onEvent(GameContract.Event.OnDirectionChanged(direction))
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusRequester.requestFocus()
                            }
                    )

                    Spacer(modifier = Modifier.width(spacing.lg))

                    // Right: Controller Panel (D-Pad, Field Settings, Action Buttons)
                    Column(
                        modifier = Modifier
                            .weight(0.7f)
                            .widthIn(max = 240.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Active direction badge
                        ActiveDirBadge(direction = state.direction)

                        Spacer(modifier = Modifier.height(spacing.md))

                        // Direction D-Pad controller (purely visual display as instructed)
                        DirectionDpad(currentDirection = state.direction)

                        Spacer(modifier = Modifier.height(spacing.lg))

                        // Field Settings
                        FieldSizeSelector(
                            selectedConfig = state.selectedFieldSize,
                            onConfigChanged = { config ->
                                onEvent(GameContract.Event.OnFieldSizeConfigChanged(config))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.lg))

                        // Action / Restart Buttons
                        val isFinished = state.status == GameStatus.GAME_OVER || state.status == GameStatus.VICTORY
                        CyberRestartButton(
                            onClick = { onEvent(GameContract.Event.OnRestartClicked) },
                            isFinished = isFinished,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Footer Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cyberColors.backgroundEnd.copy(alpha = 0.8f))
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = cyberColors.glassBorder.copy(alpha = 0.3f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(horizontal = spacing.cardPadding, vertical = spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keyboard Instructions
                KeyInstructions(
                    keys = keyList,
                    instructionText = "to navigate snake direction"
                )

                // Status Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    val statusText = when (state.status) {
                        GameStatus.IDLE -> "SYSTEM READY"
                        GameStatus.PLAYING -> "SYSTEM ACTIVE"
                        GameStatus.GAME_OVER -> "COLLISION DETECTED"
                        GameStatus.VICTORY -> "GRID COMPLETED"
                    }
                    val statusColor = when (state.status) {
                        GameStatus.IDLE -> cyberColors.textSecondary
                        GameStatus.PLAYING -> cyberColors.apple
                        GameStatus.GAME_OVER -> cyberColors.snakeHead
                        GameStatus.VICTORY -> cyberColors.highlightStart
                    }

                    StatusBadge(
                        text = statusText,
                        color = statusColor,
                        isPulsing = state.status == GameStatus.PLAYING
                    )

                    StatusBadge(
                        text = "MANUAL PROTOCOL",
                        color = cyberColors.snakeHead
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
