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
    val email: String = "", val password: String = "", val errorMessage: String? = null
)

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}

class LoginViewModel(
    private val auth: Authentication
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { current -> current.copy(email = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current -> current.copy(password = value) }
    }

    fun onLogin() {
        val (email, password) = _uiState.value
        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch { _events.send(LoginEvent.ShowError("All fields are required")) }
            return
        }

        viewModelScope.launch {
            when (auth.loginWithEmail(email, password)) {
                is AuthResult.Success -> _events.send(LoginEvent.LoginSuccess)
                is AuthResult.Failure -> _events.send(LoginEvent.ShowError("Invalid credentials"))
            }
        }
    }
}
