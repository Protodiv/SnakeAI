package ua.snakeai.app.view.main.mainmenu

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.snakeai.app.core.mvi.BaseViewModel
import ua.snakeai.app.data.repository.SnakeAiRepository
import ua.snakeai.app.navigation.NavigationRoute

class MainMenuViewModel(
    private val repository: SnakeAiRepository
) : BaseViewModel<MainMenuContract.State, MainMenuContract.Event, MainMenuContract.Effect>(
    MainMenuContract.State()
) {
    override val state: StateFlow<MainMenuContract.State> = _state
        .onStart {
            loadModelInfo()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    override fun onEvent(event: MainMenuContract.Event) {
        when (event) {
            MainMenuContract.Event.OnPlayManualClicked -> {
                navigateTo(NavigationRoute.MainRoute.PlayManualScreen.route)
            }
            MainMenuContract.Event.OnTrainDqnClicked -> {
                navigateTo(NavigationRoute.MainRoute.TrainDqnScreen.route)
            }
            MainMenuContract.Event.OnPlayAiClicked -> {
                navigateTo(NavigationRoute.MainRoute.PlayAiScreen.route)
            }
            MainMenuContract.Event.OnRefreshModelClicked -> {
                loadModelInfo()
            }
        }
    }

    private fun loadModelInfo() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            try {
                val model = repository.getAiModel()
                updateState {
                    it.copy(
                        isLoading = false,
                        modelInfo = model
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
}
