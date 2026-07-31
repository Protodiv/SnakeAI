package ua.snakeai.app.screens.main

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.ui.shared.*
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.app.view.playai.PlayAiContract
import ua.snakeai.app.view.playai.PlayAiViewModel

@Composable
fun PlayAiScene(
    navigator: NavHostController
) {
    val viewModel: PlayAiViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    PlayAiScreen(
        state = state,
        effect = { viewModel.effect },
        onEvent = viewModel::onEvent,
        onBackClicked = navigator::popBackStack
    )
}

@Composable
fun PlayAiScreen(
    state: PlayAiContract.State,
    effect: () -> Flow<PlayAiContract.Effect>,
    onEvent: (PlayAiContract.Event) -> Unit,
    onBackClicked: () -> Unit
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effect) {
        effect().collectLatest { item ->
            when (item) {
                is PlayAiContract.Effect.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(item.message)
                }
            }
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
        ScanlineOverlay(infiniteTransition = rememberInfiniteTransition(label = "scanline_play_ai"))

        Column(modifier = Modifier.fillMaxSize()) {
            // Top HUD Bar with statistics display
            CyberHeader(onBackClicked = onBackClicked) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudStatsDisplay(
                        score = state.gameState?.score ?: 0,
                        topScore = state.gameState?.topScore ?: 0,
                        steps = state.gameState?.steps ?: 0
                    )
                    Spacer(modifier = Modifier.width(spacing.md))
                    Text(
                        text = "AGENT: ${state.selectedAgent?.name ?: "NONE"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Main Content Area: Splits into Left (Game Arena) and Right (Agent Configuration & Console logs)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(spacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(spacing.cardPadding)
            ) {
                // Left Column: Square Game Arena Container (taking max height)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    GameArenaContainer(
                        state = state.gameState ?: GameContract.State(),
                        protocolText = "Protocol: Autonomous_Execution",
                        agentNameText = "Agent: ${state.selectedAgent?.name ?: "None Selected"}",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )

                    // Sleek neural link loading overlay
                    if (state.isLoading && state.isPlaying) {
                        NeuralLinkOverlay(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )
                    }
                }

                // Right Column: Agent Configuration and Console logs underneath
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Agent Config Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.md))
                            .background(cyberColors.glassFill)
                            .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.md))
                            .padding(spacing.cardPadding)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            Text(
                                text = "AGENT CONFIGURATION",
                                color = cyberColors.highlightStart,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Agent/Model Name input
                            ModelNameInput(
                                value = state.agentNameText,
                                onValueChange = { onEvent(PlayAiContract.Event.OnAgentNameChanged(it)) },
                                enabled = !state.isPlaying
                            )

                            // Autocomplete helper list
                            if (!state.isPlaying && state.filteredAgents.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 140.dp)
                                        .background(cyberColors.backgroundStart.copy(alpha = 0.95f))
                                        .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(4.dp))
                                        .padding(vertical = 4.dp)
                                ) {
                                    LazyColumn {
                                        items(state.filteredAgents) { agent ->
                                            AgentModelRow(
                                                agent = agent,
                                                onClick = { onEvent(PlayAiContract.Event.OnAgentSelected(agent)) }
                                            )
                                            Divider(color = cyberColors.glassBorder.copy(alpha = 0.2f), thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }

                            // Play initialization button
                            AgentPlayButton(
                                isPlaying = state.isPlaying,
                                isLoading = state.isLoading,
                                onStartClicked = { onEvent(PlayAiContract.Event.OnStartClicked) },
                                onStopClicked = { onEvent(PlayAiContract.Event.OnStopClicked) }
                            )
                        }
                    }

                    // Console Log Panel underneath naming input and search helpers
                    ConsoleLogPanel(
                        logs = state.logs,
                        placeholderText = ">> Console idle. Awaiting neural link...",
                        title = "AGENT PLAY LOGS",
                        onClearLogsClicked = { onEvent(PlayAiContract.Event.OnClearLogsClicked) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
