package ua.protodiv.app.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, E : UiEvent, Ef : UiEffect>(
    initialState: S
) : ViewModel() {

    protected val _state = MutableStateFlow(initialState)
    abstract val state: StateFlow<S>

    private val _effect = Channel<Ef>(Channel.BUFFERED)
    val effect: Flow<Ef> = _effect.receiveAsFlow()

    private val _navigation = Channel<NavigationEffect>(Channel.BUFFERED)
    val navigation: Flow<NavigationEffect> = _navigation.receiveAsFlow()

    protected val currentState: S
        get() = _state.value

    abstract fun onEvent(event: E)

    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun emitEffect(effect: Ef) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    protected fun navigateBack() {
        viewModelScope.launch {
            _navigation.send(NavigationEffect.Back)
        }
    }

    protected fun navigateTo(route: String) {
        viewModelScope.launch {
            _navigation.send(NavigationEffect.ToRoute(route))
        }
    }
}
