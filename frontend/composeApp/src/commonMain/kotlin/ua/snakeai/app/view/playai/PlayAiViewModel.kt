package ua.snakeai.app.view.playai

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onStart
import ua.snakeai.app.core.mvi.BaseViewModel
import ua.snakeai.app.data.api.AppResult
import ua.snakeai.app.data.repository.SnakeAiRepository
import ua.snakeai.app.ui.shared.formatDouble
import ua.snakeai.app.domain.feature.snakeai.error.toSnakeAIError
import ua.snakeai.app.view.game.GameContract
import ua.snakeai.app.view.game.toGameContractState
import ua.snakeai.contract.*

class PlayAiViewModel(
    private val repository: SnakeAiRepository
) : BaseViewModel<PlayAiContract.State, PlayAiContract.Event, PlayAiContract.Effect>(
    PlayAiContract.State()
) {
    private var playJob: Job? = null

    override val state: StateFlow<PlayAiContract.State> = _state
        .onStart {
            loadAgents()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    override fun onEvent(event: PlayAiContract.Event) {
        when (event) {
            PlayAiContract.Event.OnStartClicked -> startPlay()
            PlayAiContract.Event.OnStopClicked -> stopPlay()
            is PlayAiContract.Event.OnAgentNameChanged -> {
                val query = event.name
                val filtered = if (query.isEmpty()) {
                    currentState.availableAgents
                } else {
                    currentState.availableAgents.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                }
                updateState {
                    it.copy(
                        agentNameText = query,
                        filteredAgents = filtered,
                        selectedAgent = currentState.availableAgents.find { agent -> agent.name == query }
                    )
                }
            }
            is PlayAiContract.Event.OnAgentSelected -> {
                updateState {
                    it.copy(
                        agentNameText = event.agent.name,
                        selectedAgent = event.agent,
                        filteredAgents = emptyList() // clear autocomplete suggestions after selection
                    )
                }
            }
            PlayAiContract.Event.OnClearLogsClicked -> {
                updateState { it.copy(logs = emptyList()) }
            }
            PlayAiContract.Event.OnLoadAgents -> {
                loadAgents()
            }
        }
    }

    private fun loadAgents() {
        updateState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.getAiModels()) {
                is AppResult.Success -> {
                    updateState {
                        it.copy(
                            availableAgents = result.data,
                            filteredAgents = result.data,
                            isLoading = false
                        )
                    }
                }
                is AppResult.Error -> {
                    val domainError = result.error.toSnakeAIError()
                    val errorMsg = domainError.message
                    updateState {
                        it.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                    }
                    emitEffect(PlayAiContract.Effect.ShowSnackBar(errorMsg))
                }
            }
        }
    }

    private fun startPlay() {
        if (currentState.agentNameText.isBlank()) {
            emitEffect(PlayAiContract.Effect.ShowSnackBar("Please enter or select a model name first."))
            return
        }

        playJob?.cancel()
        updateState {
            it.copy(
                isPlaying = true,
                isLoading = true,
                error = null,
                gameState = GameContract.State(), // Reset game state
                logs = it.logs + ">> Establishing neural link with agent '${it.agentNameText}'..."
            )
        }

        playJob = viewModelScope.launch {
            repository.playAi(
                modelName = currentState.agentNameText,
                fieldSize = FieldSize.MEDIUM,
                tickRateMs = 100L
            ).catch { e ->
                val errorMsg = e.message ?: "Play session connection failed"
                updateState {
                    it.copy(
                        error = errorMsg,
                        isPlaying = false,
                        isLoading = false,
                        logs = it.logs + ">> ERROR: $errorMsg"
                    )
                }
                emitEffect(PlayAiContract.Effect.ShowSnackBar(errorMsg))
            }.collect { frame ->
                val isFirstFrame = currentState.isLoading
                val logLine = formatPlayLogLine(frame)
                updateState {
                    val newLogs = (it.logs + logLine).takeLast(200)
                    val finalLogs = if (isFirstFrame) {
                        newLogs + ">> Neural link established. Autonomous execution initialized."
                    } else {
                        newLogs
                    }
                    it.copy(
                        isLoading = false,
                        gameState = frame.state.toGameContractState(it.selectedAgent?.topScore ?: 0),
                        logs = finalLogs
                    )
                }
            }
        }
    }

    private fun stopPlay() {
        playJob?.cancel()
        updateState {
            it.copy(
                isPlaying = false,
                isLoading = false,
                logs = it.logs + ">> Autonomous execution stopped by operator."
            )
        }
    }

    private fun formatPlayLogLine(frame: GameFrame): String {
        val state = frame.state
        val metrics = frame.decisionMetrics
        val actionStr = metrics.selectedAction
        val expStr = if (metrics.isExploration) "EXPLORE" else "EXPLOIT"
        val qMax = if (metrics.qValues.isNotEmpty()) formatDouble(metrics.qValues.maxOrNull() ?: 0.0, 3) else "N/A"
        val headPos = state.snake.firstOrNull()
        val posStr = if (headPos != null) "(${headPos.x}, ${headPos.y})" else "N/A"

        return "Step #${state.steps.toString().padStart(4, '0')}: " +
                "Score = ${state.score.toString().padStart(2, '0')}, " +
                "Action = $actionStr ($expStr), " +
                "Max Q = $qMax, " +
                "Pos = $posStr"
    }



    override fun onCleared() {
        super.onCleared()
        playJob?.cancel()
    }
}
