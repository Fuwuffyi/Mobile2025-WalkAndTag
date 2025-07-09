package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val username: String = "", val showLanguageDialog: Boolean = false
)

sealed interface SettingsEvent {
    object NavigateToAuth : SettingsEvent
    data class ShowError(val message: String) : SettingsEvent
}

class SettingsViewModel(
    private val auth: Authentication, private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val userId = auth.getCurrentUserId()
            if (userId != null) {
                val user = userRepo.get(userId)
                _uiState.value = _uiState.value.copy(username = user?.data?.username ?: "")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.logout()
            _uiEvent.emit(SettingsEvent.NavigateToAuth)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            auth.getCurrentUserId()?.let {
                userRepo.delete(it)
                auth.deleteCurrentUser()
                auth.logout()
                _uiEvent.emit(SettingsEvent.NavigateToAuth)
            } ?: _uiEvent.emit(SettingsEvent.ShowError("User not found"))
        }
    }

    fun toggleLanguageDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLanguageDialog = show)
    }
}
