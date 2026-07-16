package ua.snakeai.app.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.core.mvi.NavigationEffect
import ua.snakeai.app.core.mvi.handle
import kotlinx.collections.immutable.persistentListOf
import ua.snakeai.app.ui.shared.*
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.view.main.mainmenu.MainMenuContract
import ua.snakeai.app.view.main.mainmenu.MainMenuViewModel

@Composable
fun MainMenuScene(
    navigator: () -> Navigator
) {
    val viewModel: MainMenuViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nav = navigator()

    LaunchedEffect(viewModel) {
        viewModel.navigation.collectLatest(nav::handle)
    }

    MainMenuScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigationRequested = nav::handle
    )
}

@Composable
fun MainMenuScreen(
    state: MainMenuContract.State,
    onEvent: (MainMenuContract.Event) -> Unit,
    onNavigationRequested: (NavigationEffect) -> Unit
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Shared top background circles
                drawBackgroundCircle(
                    backgroundStart = cyberColors.backgroundStart,
                    backgroundEnd = cyberColors.backgroundEnd
                )
                // Radial gradient 2 (Bottom Left Glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0D1515), Color.Transparent),
                        center = Offset(0f, size.height),
                        radius = size.height * 0.5f
                    ),
                    radius = size.height * 0.5f,
                    center = Offset(0f, size.height)
                )
            }
    ) {
        // Scanning line overlay (passes a new self-contained infinite transition)
        ScanlineOverlay(infiniteTransition = rememberInfiniteTransition(label = "scanline_pulse"))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Identity branding
            BrandingHeader(
                title = "SNAKE AI",
                subtitle = "Reinforcement Learning & Deep Q-Network Showcase"
            )

            Spacer(modifier = Modifier.height(spacing.md))

            // Status row
            StatusRow(statusText = "System Online")

            Spacer(modifier = Modifier.height(spacing.xl))

            // Menu items
            Column(
                modifier = Modifier.widthIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(spacing.elementGap)
            ) {
                MenuCardButton(
                    title = "Play",
                    description = "Manual control mode. Test your reflexes on the 8px grid.",
                    icon = Icons.Default.SportsEsports,
                    accentColor = cyberColors.highlightStart,
                    containerColor = cyberColors.glassFill,
                    borderColor = cyberColors.glassBorder,
                    onClick = { onEvent(MainMenuContract.Event.OnPlayManualClicked) }
                )

                MenuCardButton(
                    title = "Train",
                    description = "DQN Training. Configure hyperparameters and evolve the agent.",
                    icon = Icons.Default.ModelTraining,
                    accentColor = cyberColors.highlightEnd,
                    containerColor = cyberColors.glassFill,
                    borderColor = cyberColors.glassBorder,
                    onClick = { onEvent(MainMenuContract.Event.OnTrainDqnClicked) }
                )

                // Filled Play AI Button (Vibrant Call to Action)
                MenuCardButton(
                    title = "Play AI",
                    description = "Autonomous showcase. Watch Agent Alpha navigate the arena.",
                    icon = Icons.Default.Psychology,
                    accentColor = cyberColors.backgroundStart,
                    containerColor = cyberColors.highlightStart,
                    borderColor = Color.Transparent,
                    titleColor = cyberColors.backgroundStart,
                    descColor = cyberColors.backgroundStart.copy(alpha = 0.8f),
                    isGlow = true,
                    onClick = { onEvent(MainMenuContract.Event.OnPlayAiClicked) }
                )
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            // Footer Stats
            val statList = remember(state.modelInfo) {
                persistentListOf(
                    StatItem(
                        value = state.modelInfo?.episodesRun?.let { formatEpisodes(it) } ?: "1.2M+",
                        label = "EPISODES RUN",
                        valueColor = cyberColors.highlightStart
                    ),
                    StatItem(
                        value = state.modelInfo?.efficiency?.let { "$it%" } ?: "98.4%",
                        label = "EFFICIENCY",
                        valueColor = cyberColors.apple
                    ),
                    StatItem(
                        value = state.modelInfo?.topScore?.toString() ?: "428",
                        label = "TOP SCORE",
                        valueColor = cyberColors.snakeHead
                    )
                )
            }
            FooterStatsPanel(stats = statList)
        }
    }
}
