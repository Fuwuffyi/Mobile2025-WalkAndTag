package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val email: String = "", val password: String = ""
)

enum class LoginError {
    ALL_FIELDS_REQUIRED, INVALID_CREDENTIALS
}

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
    data class ShowError(val err: LoginError) : LoginEvent()
}

class LoginViewModel(
    private val auth: Authentication
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChanged(value: String) = updateState { copy(email = value) }

    fun onPasswordChanged(value: String) = updateState { copy(password = value) }

    fun onLogin() {
        val (email, password) = _uiState.value

        if (email.isBlank() || password.isBlank()) {
            sendEvent(LoginEvent.ShowError(LoginError.ALL_FIELDS_REQUIRED))
            return
        }

        viewModelScope.launch {
            when (auth.loginWithEmail(email, password)) {
                is AuthResult.Success -> sendEvent(LoginEvent.LoginSuccess)
                is AuthResult.Failure -> sendEvent(LoginEvent.ShowError(LoginError.INVALID_CREDENTIALS))
            }
        }
    }

    private fun updateState(reducer: LoginState.() -> LoginState) {
        _uiState.update(reducer)
    }

    private fun sendEvent(event: LoginEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
}
