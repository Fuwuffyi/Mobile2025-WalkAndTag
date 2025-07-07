package com.github.walkandtag.ui.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.AuthResult
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.util.BiometricPromptManager
import com.github.walkandtag.util.BiometricStatus
import com.github.walkandtag.util.checkBiometricAvailability
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val auth: Authentication,
    private val userRepo: FirestoreRepository<UserSchema>,
    private val globalViewModel: GlobalViewModel
) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<AuthUIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            when (auth.loginWithGoogle(context)) {
                is AuthResult.Success -> {
                    userRepo.create(
                        UserSchema(username = auth.getCurrentUserName().orEmpty()),
                        auth.getCurrentUserId().orEmpty()
                    )
                    _uiEvent.emit(AuthUIEvent.NavigateToMain)
                }

                is AuthResult.Failure -> globalViewModel.showSnackbar("Google login failed.")
            }
        }
    }

    fun checkBiometricAndNavigate(activity: FragmentActivity) {
        val status = checkBiometricAvailability(activity)
        val currentUser = FirebaseAuth.getInstance().currentUser
        when (status) {
            BiometricStatus.SUCCESS -> {
                BiometricPromptManager(activity).authenticate(
                    onSuccess = {
                    viewModelScope.launch {
                        if (currentUser != null) {
                            _uiEvent.emit(AuthUIEvent.NavigateToMain)
                        }
                    }
                },
                    onFail = { globalViewModel.showSnackbar("Biometric authentication failed.") },
                    onError = { globalViewModel.showSnackbar("Biometric authentication error.") })
            }

            BiometricStatus.NO_ENROLLED -> globalViewModel.showSnackbar("No biometric enrolled.")
            BiometricStatus.NO_HARDWARE -> globalViewModel.showSnackbar("No biometric hardware.")
            BiometricStatus.FAILURE -> globalViewModel.showSnackbar("Biometric error.")
        }
    }

    sealed class AuthUIEvent {
        object NavigateToMain : AuthUIEvent()
    }
}
