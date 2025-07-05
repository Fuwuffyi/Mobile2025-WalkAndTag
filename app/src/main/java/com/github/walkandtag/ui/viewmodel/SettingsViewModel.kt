package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val username: String = "", val showLanguageDialog: Boolean = false
)

class SettingsViewModel(
    private val auth: Authentication, private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

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

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onSuccess()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            auth.getCurrentUserId()?.let {
                userRepo.delete(it)
                auth.deleteCurrentUser()
                auth.logout()
                onSuccess()
            }
        }
    }

    fun toggleLanguageDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLanguageDialog = show)
    }
}