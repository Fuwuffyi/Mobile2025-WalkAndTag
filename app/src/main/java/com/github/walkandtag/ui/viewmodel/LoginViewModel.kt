package com.github.walkandtag.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.MainActivity
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class LoginState(
    val email: String = "",
    val password: String = ""
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { current -> current.copy(email = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current -> current.copy(password = value) }
    }

    fun onLogin(context: Context, authentication: Authentication) {
        val currState: LoginState = _uiState.value

        if (currState.email.isBlank() || currState.password.isBlank()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModelScope.launch {
                when (authentication.loginWithEmail(currState.email, currState.password)) {
                    is AuthResult.Success -> {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }

                    is AuthResult.Failure -> {
                        Toast.makeText(
                            context,
                            "Could not login",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}