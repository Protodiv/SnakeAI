package ua.snakeai.app.view.main.playmenu

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ua.snakeai.app.core.mvi.BaseViewModel

class PlayMenuViewModel : BaseViewModel<PlayMenuContract.State, PlayMenuContract.Event, PlayMenuContract.Effect>(
    PlayMenuContract.State()
) {
    override val state: StateFlow<PlayMenuContract.State> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    override fun onEvent(event: PlayMenuContract.Event) {
        // No-op for now as navigation is handled directly in UI
    }
}
