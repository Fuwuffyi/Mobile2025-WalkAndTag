package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.ui.navigation.Navigation
import com.github.walkandtag.util.Navigator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NavbarState(
    val currentPage: Navigation
)

sealed class NavbarEvent {
    data class NavigateTo(val route: Navigation) : NavbarEvent()
}

class NavbarViewModel(
    startPage: Navigation, private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(NavbarState(currentPage = startPage))
    val uiState: StateFlow<NavbarState> = _uiState.asStateFlow()
    private val _events = Channel<NavbarEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            navigator.currentRoute.collect { route ->
                route?.let { currentRoute ->
                    _uiState.update { it.copy(currentPage = currentRoute) }
                }
            }
        }
    }

    fun onChangePage(value: Navigation) {
        if (value == _uiState.value.currentPage) return
        viewModelScope.launch {
            _events.send(NavbarEvent.NavigateTo(value))
        }
    }
}
