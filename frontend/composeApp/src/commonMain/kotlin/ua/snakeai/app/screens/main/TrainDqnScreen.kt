package ua.snakeai.app.screens.main

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.ui.shared.*
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.app.view.train.TrainDqnContract
import ua.snakeai.app.view.train.TrainDqnViewModel

@Composable
fun TrainDqnScene(
    navigator: NavHostController
) {
    val viewModel: TrainDqnViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    TrainDqnScreen(
        state = state,
        effect = { viewModel.effect },
        onEvent = viewModel::onEvent,
        onBackClicked = navigator::popBackStack
    )
}

@Composable
fun TrainDqnScreen(
    state: TrainDqnContract.State,
    effect: () -> kotlinx.coroutines.flow.Flow<TrainDqnContract.Effect>,
    onEvent: (TrainDqnContract.Event) -> Unit,
    onBackClicked: () -> Unit
) {
    val cyberColors = MaterialTheme.cyberColors
    val spacing = MaterialTheme.spacing
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effect) {
        effect().collectLatest { item ->
            when (item) {
                is TrainDqnContract.Effect.ShowToast -> {
                    snackbarHostState.showSnackbar(item.message)
                }
            }
        }
    }

    // Auto-scroll the terminal logs as they arrive
    LaunchedEffect(state.logs.size) {
        if (state.logs.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.logs.size - 1)
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
        ScanlineOverlay(infiniteTransition = rememberInfiniteTransition(label = "scanline_train_dqn"))

        Column(modifier = Modifier.fillMaxSize()) {
            // Top HUD Bar
            CyberHeader(onBackClicked = onBackClicked) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EPISODE: #${state.metrics?.episode ?: 0}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "EPSILON: ${formatDouble(state.metrics?.epsilon ?: 1.0, 2)}",
                        color = cyberColors.highlightStart,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Main Content Area: Splits into Left (Game Arena & Console logs) and Right (Agent Config & Metrics)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(spacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(spacing.cardPadding)
            ) {
                // Left Column: Game Arena (top) + Console Log (bottom)
                Column(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Game Arena
                    GameArenaContainer(
                        state = state.gameState ?: GameContract.State(),
                        protocolText = "Protocol: DQN_Training",
                        agentNameText = "Agent: ${state.modelName}",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    // Terminal Logging Console
                    Box(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxWidth()
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
                                    text = "TRAINING LOG CONSOLE",
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
                                        .clickable { onEvent(TrainDqnContract.Event.OnClearLogsClicked) }
                                )
                            }

                            Divider(color = cyberColors.glassBorder.copy(alpha = 0.3f), thickness = 1.dp)

                            Spacer(modifier = Modifier.height(spacing.xs))

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (state.logs.isEmpty()) {
                                    item {
                                        Text(
                                            text = ">> Console idle. Awaiting training initialization...",
                                            color = cyberColors.textSecondary.copy(alpha = 0.5f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    items(state.logs) { log ->
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

                // Right Column: Configuration & Real-Time Stats Panel
                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Hyperparameter Settings Form (Card)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                            .clip(RoundedCornerShape(spacing.md))
                            .background(cyberColors.glassFill)
                            .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.md))
                            .padding(spacing.cardPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "AGENT CONFIGURATION",
                                color = cyberColors.highlightStart,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Model Name Input
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Model Name:", color = cyberColors.textSecondary, fontSize = 10.sp)
                                OutlinedTextField(
                                    value = state.modelName,
                                    onValueChange = { onEvent(TrainDqnContract.Event.OnModelNameChanged(it)) },
                                    enabled = !state.isTraining,
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
                                    shape = RoundedCornerShape(4.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                )
                            }

                             // Learning Rate selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Learning Rate:", color = cyberColors.textSecondary, fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                                    state.availableLearningRates.forEach { lr ->
                                        HyperparameterChoiceChip(
                                            text = formatDouble(lr, 4),
                                            isSelected = state.hyperparameters.learningRate == lr,
                                            isEnabled = !state.isTraining,
                                            cyberColors = cyberColors
                                        ) {
                                            onEvent(TrainDqnContract.Event.OnHyperparametersChanged(state.hyperparameters.copy(learningRate = lr)))
                                        }
                                    }
                                }
                            }

                            // Batch Size selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Batch Size:", color = cyberColors.textSecondary, fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                                    state.availableBatchSizes.forEach { size ->
                                        HyperparameterChoiceChip(
                                            text = size.toString(),
                                            isSelected = state.hyperparameters.batchSize == size,
                                            isEnabled = !state.isTraining,
                                            cyberColors = cyberColors
                                        ) {
                                            onEvent(TrainDqnContract.Event.OnHyperparametersChanged(state.hyperparameters.copy(batchSize = size)))
                                        }
                                    }
                                }
                            }

                            // Training start/stop button
                            Button(
                                onClick = {
                                    if (state.isTraining) {
                                        onEvent(TrainDqnContract.Event.OnStopTrainingClicked)
                                    } else {
                                        onEvent(TrainDqnContract.Event.OnStartTrainingClicked)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isTraining) cyberColors.snakeHead else cyberColors.highlightStart
                                ),
                                shape = RoundedCornerShape(spacing.xs),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isTraining) Icons.Default.Stop else Icons.Default.ModelTraining,
                                    contentDescription = "Train Control",
                                    tint = if (state.isTraining) Color.White else cyberColors.backgroundStart,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Text(
                                    text = if (state.isTraining) "STOP TRAINING" else "EVOLVE AGENT",
                                    color = if (state.isTraining) Color.White else cyberColors.backgroundStart,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Simulated Metrics Display Panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(spacing.md))
                            .background(cyberColors.glassFill)
                            .border(1.dp, cyberColors.glassBorder, RoundedCornerShape(spacing.md))
                            .padding(spacing.cardPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "REAL-TIME METRICS",
                                color = cyberColors.highlightStart,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricBox("LOSS", formatDouble(state.metrics?.loss ?: 0.0, 5), cyberColors.snakeHead, Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(spacing.md))
                                MetricBox("AVG REWARD", formatDouble(state.metrics?.averageReward ?: 0.0, 2), cyberColors.apple, Modifier.weight(1f))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricBox("TOP SCORE", (state.metrics?.topScore ?: 0).toString(), Color.White, Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(spacing.md))
                                MetricBox("STEPS/SEC", formatDouble(state.metrics?.stepsPerSecond ?: 0.0, 1), cyberColors.highlightStart, Modifier.weight(1f))
                            }
                        }
                    }
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

