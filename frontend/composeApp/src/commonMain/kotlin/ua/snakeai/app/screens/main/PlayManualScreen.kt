package ua.snakeai.app.screens.main

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.core.mvi.handle
import ua.snakeai.app.ui.shared.*
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.main.playmenu.PlayMenuContract
import ua.snakeai.app.view.main.playmenu.PlayMenuViewModel
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.app.view.game.GamePanel

@Composable
fun PlayManualScene(
    navigator: () -> Navigator
) {
    val playMenuViewModel: PlayMenuViewModel = koinViewModel()
    val menuState by playMenuViewModel.state.collectAsStateWithLifecycle()
    val nav = navigator()

    LaunchedEffect(playMenuViewModel) {
        playMenuViewModel.navigation.collectLatest(nav::handle)
    }

    PlayManualScreen(
        menuState = menuState,
        onBackClicked = { nav.popBackStack() }
    )
}

@Composable
fun PlayManualScreen(
    menuState: PlayMenuContract.State,
    onBackClicked: () -> Unit
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    val keyList = remember { persistentListOf("W", "A", "S", "D") }

    // Local states updated exclusively via GamePanel's onStateChanged callback
    var score by rememberSaveable { mutableStateOf(0) }
    var topScore by rememberSaveable { mutableStateOf(0) }
    var steps by rememberSaveable { mutableStateOf(0) }
    var lastDirectionName by rememberSaveable { mutableStateOf(GameContract.Direction.RIGHT.name) }
    val lastDirection = remember(lastDirectionName) { GameContract.Direction.valueOf(lastDirectionName) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.xxl)
                    .background(cyberColors.backgroundStart.copy(alpha = 0.7f))
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = cyberColors.glassBorder.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(horizontal = spacing.cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                CyberBackButton(onClick = onBackClicked)

                // Stats / HUD Display
                HudStatsDisplay(
                    score = score,
                    topScore = maxOf(topScore, score),
                    steps = steps
                )
            }

            // Main Content Section (Row layout placing Game Arena and D-Pad side-by-side)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(spacing.cardPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.widthIn(max = 680.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: The square game arena placeholder container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    ) {
                        GamePanel(
                            initialDirection = lastDirection,
                            onStateChanged = { state ->
                                score = state.score
                                topScore = state.topScore
                                steps = state.steps
                                lastDirectionName = state.direction.name
                            },
                            onArenaClicked = { direction ->
                                lastDirectionName = direction.name
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Corner Badges (Using shared CornerBadge component)
                        CornerBadge(
                            text = "Protocol: ${menuState.protocol}",
                            textColor = cyberColors.highlightStart,
                            backgroundColor = cyberColors.glassBorder,
                            borderColor = cyberColors.highlightStart,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = (-8).dp, y = (-8).dp)
                        )

                        CornerBadge(
                            text = "Agent: ${menuState.agent}",
                            textColor = Color.White,
                            backgroundColor = cyberColors.snakeHead.copy(alpha = 0.9f),
                            borderColor = cyberColors.snakeHead,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 8.dp, y = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(spacing.md))

                    // Right: D-Pad & active direction info badge
                    Column(
                        modifier = Modifier.width(160.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Active direction badge (Using shared ActiveDirBadge component)
                        ActiveDirBadge(direction = lastDirection)

                        Spacer(modifier = Modifier.height(spacing.md))

                        // Direction D-Pad controller (purely visual display using lastDirection from shared package)
                        DirectionDpad(currentDirection = lastDirection)
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
                    StatusBadge(
                        text = "SYSTEM ACTIVE",
                        color = cyberColors.apple,
                        isPulsing = true
                    )

                    StatusBadge(
                        text = "MANUAL PROTOCOL",
                        color = cyberColors.snakeHead
                    )
                }
            }
        }
    }
}
