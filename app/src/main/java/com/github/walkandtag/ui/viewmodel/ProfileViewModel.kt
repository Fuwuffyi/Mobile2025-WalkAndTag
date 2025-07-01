package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.Filter
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    var user: FirestoreDocument<UserSchema>? = null,
    var paths: Collection<FirestoreDocument<PathSchema>> = emptyList(),
    var isRecording: Boolean = false
)

class ProfileViewModel(
    private val auth: Authentication,
    private val userRepo: FirestoreRepository<UserSchema>,
    private val pathRepo: FirestoreRepository<PathSchema>
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            val userDoc = userRepo.get(userId)
            _uiState.update { current -> current.copy(user = userDoc) }

            userDoc?.let {
                val userPaths = pathRepo.getFiltered(listOf(Filter("userId", it.id)))
                _uiState.update { current -> current.copy(paths = userPaths.toList()) }
            }
        }
    }

    fun isOwnProfile(): Boolean {
        val currentUserId = auth.getCurrentUserId()
        return currentUserId == _uiState.value.user?.id
    }

    fun toggleRecording() {
        _uiState.update { current -> current.copy(isRecording = !current.isRecording) }
    }
}
