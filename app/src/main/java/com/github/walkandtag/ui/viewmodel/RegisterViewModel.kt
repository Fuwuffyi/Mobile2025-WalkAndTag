package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.UserSchema
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null
)

sealed class RegisterEvent {
    object RegisterSuccess : RegisterEvent()
    data class ShowError(val message: String) : RegisterEvent()
}

class RegisterViewModel(
    private val auth: Authentication, private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()
    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onUsernameChanged(value: String) {
        _uiState.update { current -> current.copy(username = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { current -> current.copy(email = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current -> current.copy(password = value) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { current -> current.copy(confirmPassword = value) }
    }

    fun onRegister() {
        val (username, email, password, confirmPassword) = _uiState.value
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            viewModelScope.launch { _events.send(RegisterEvent.ShowError("All fields are required")) }
            return
        }
        if (password != confirmPassword) {
            viewModelScope.launch { _events.send(RegisterEvent.ShowError("Passwords do not match")) }
            return
        }
        viewModelScope.launch {
            when (auth.registerWithEmail(email, password)) {
                is AuthResult.Success -> {
                    userRepo.create(UserSchema(username), auth.getCurrentUserId().orEmpty())
                    _events.send(RegisterEvent.RegisterSuccess)
                }

                is AuthResult.Failure -> _events.send(RegisterEvent.ShowError("Could not register your account"))
            }
        }
    }
}
