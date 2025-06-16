package com.github.walkandtag.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.MainActivity
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.UserSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

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

    fun onRegister(
        context: Context,
        authentication: Authentication,
        userRepo: FirestoreRepository<UserSchema>
    ) {
        val currState: RegisterState = _uiState.value
        if (currState.email.isBlank() || currState.password.isBlank() || currState.confirmPassword.isBlank()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT)
                .show()
        } else if (currState.password != currState.confirmPassword) {
            Toast.makeText(context, "Passwords don’t match", Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModelScope.launch {
                when (authentication.registerWithEmail(currState.email, currState.password)) {
                    is AuthResult.Success -> {
                        userRepo.create(
                            UserSchema(
                                id = authentication.getCurrentUserId(),
                                username = "Username"
                            )
                        )
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }

                    is AuthResult.Failure -> {
                        Toast.makeText(
                            context,
                            "Could not register your account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}