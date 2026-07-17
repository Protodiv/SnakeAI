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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel
import ua.snakeai.app.ui.shared.CyberBackButton
import ua.snakeai.app.ui.shared.ScanlineOverlay
import ua.snakeai.app.ui.shared.drawBackgroundCircle
import ua.snakeai.app.ui.theme.cyberColors
import ua.snakeai.app.ui.theme.spacing
import ua.snakeai.app.view.train.TrainDqnContract
import ua.snakeai.app.view.train.TrainDqnViewModel
import ua.snakeai.contract.FieldSize
import kotlin.math.pow

@Composable
fun TrainDqnScene(
    navigator: Navigator
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
                CyberBackButton(onClick = onBackClicked)

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

            // Main Content Area: Splits into Hyperparameters Form (left) and Logs Terminal (right)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(spacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(spacing.cardPadding)
            ) {
                // Left: Configuration & Real-Time Stats Panel
                Column(
                    modifier = Modifier
                        .weight(0.9f)
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Model Name Input
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Model Name:", color = cyberColors.textSecondary, fontSize = 11.sp)
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
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                )
                            }

                            // Learning Rate selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Learning Rate:", color = cyberColors.textSecondary, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                    listOf(0.001, 0.0005, 0.0001).forEach { lr ->
                                        HyperparameterChoiceChip(
                                            text = lr.toString(),
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
                                Text("Batch Size:", color = cyberColors.textSecondary, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                    listOf(32, 64, 128).forEach { size ->
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

                            // Grid Size selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Arena Grid Size:", color = cyberColors.textSecondary, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                    listOf(FieldSize.SMALL, FieldSize.MEDIUM, FieldSize.LARGE).forEach { size ->
                                        HyperparameterChoiceChip(
                                            text = size.name,
                                            isSelected = state.fieldSize == size,
                                            isEnabled = !state.isTraining,
                                            cyberColors = cyberColors
                                        ) {
                                            onEvent(TrainDqnContract.Event.OnFieldSizeChanged(size))
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
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isTraining) Icons.Default.Stop else Icons.Default.ModelTraining,
                                    contentDescription = "Train Control",
                                    tint = if (state.isTraining) Color.White else cyberColors.backgroundStart
                                )
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Text(
                                    text = if (state.isTraining) "STOP TRAINING" else "EVOLVE AGENT",
                                    color = if (state.isTraining) Color.White else cyberColors.backgroundStart,
                                    fontWeight = FontWeight.Bold
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
                                fontSize = 14.sp,
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

                // Right: Terminal Logging Console
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
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
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun HyperparameterChoiceChip(
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    cyberColors: ua.snakeai.app.ui.theme.CyberColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) cyberColors.highlightStart.copy(alpha = 0.2f)
                else cyberColors.backgroundStart
            )
            .border(
                1.dp,
                if (isSelected) cyberColors.highlightStart
                else cyberColors.glassBorder.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = MaterialTheme.spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) cyberColors.highlightStart else if (isEnabled) Color.White else cyberColors.textSecondary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetricBox(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    val cyberColors = MaterialTheme.cyberColors
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(cyberColors.backgroundStart)
            .border(1.dp, cyberColors.glassBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = label, color = cyberColors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatDouble(value: Double, decimals: Int): String {
    if (value.isNaN() || value.isInfinite()) return "0.0"
    val factor = 10.0.pow(decimals)
    val rounded = kotlin.math.round(value * factor) / factor
    return rounded.toString()
}
