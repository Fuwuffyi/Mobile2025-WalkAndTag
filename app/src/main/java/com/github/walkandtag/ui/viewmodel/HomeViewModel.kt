package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val items: List<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(
    private val pathRepo: FirestoreRepository<PathSchema>,
    private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            try {
                val paths = pathRepo.getAll().toList()
                val userIds = paths.map { it.data.userId }.toSet()
                val users = userRepo.get(userIds).associate { it.id to it.data }
                val feedItems = paths.map { path ->
                    val user = users[path.data.userId] ?: UserSchema("Deleted User")
                    Pair(FirestoreDocument<UserSchema>(path.data.userId, user), path)
                }
                _uiState.value = HomeState.Success(feedItems)
            } catch (e: Exception) {
                _uiState.value = HomeState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
