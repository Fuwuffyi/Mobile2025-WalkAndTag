package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
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
    val confirmPassword: String = ""
)

enum class RegisterError {
    ALL_FIELDS_REQUIRED, REPEAT_PASSWORD_INCORRECT, GENERIC_ERROR
}

sealed class RegisterEvent {
    object RegisterSuccess : RegisterEvent()
    data class ShowError(val err: RegisterError) : RegisterEvent()
}

class RegisterViewModel(
    private val auth: Authentication, private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<RegisterEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onUsernameChanged(value: String) = updateState { copy(username = value) }

    fun onEmailChanged(value: String) = updateState { copy(email = value) }

    fun onPasswordChanged(value: String) = updateState { copy(password = value) }

    fun onConfirmPasswordChanged(value: String) = updateState { copy(confirmPassword = value) }

    fun onRegister() {
        val (username, email, password, confirmPassword) = _uiState.value

        when {
            username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> sendEvent(
                RegisterEvent.ShowError(RegisterError.ALL_FIELDS_REQUIRED)
            )

            password != confirmPassword -> sendEvent(RegisterEvent.ShowError(RegisterError.REPEAT_PASSWORD_INCORRECT))

            else -> {
                viewModelScope.launch {
                    when (auth.registerWithEmail(email, password)) {
                        is AuthResult.Success -> {
                            val uid = auth.getCurrentUserId().orEmpty()
                            userRepo.create(UserSchema(username), uid)
                            sendEvent(RegisterEvent.RegisterSuccess)
                        }

                        is AuthResult.Failure -> sendEvent(RegisterEvent.ShowError(RegisterError.GENERIC_ERROR))
                    }
                }
            }
        }
    }

    private fun updateState(reducer: RegisterState.() -> RegisterState) {
        _uiState.update(reducer)
    }

    private fun sendEvent(event: RegisterEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
