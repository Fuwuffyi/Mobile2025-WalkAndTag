package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val items: List<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>) :
        HomeState()

    data class Error(val message: String) : HomeState()
}

class HomeViewModel(
    private val pathRepo: FirestoreRepository<PathSchema>,
    private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState
    private val loadedItems =
        mutableListOf<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>()
    private var lastPathId: String? = null
    private var isLoading = false
    private var endReached = false
    private val pageSize = 10u

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading || endReached) return
        isLoading = true
        _uiState.value = if (loadedItems.isEmpty()) HomeState.Loading else _uiState.value
        viewModelScope.launch {
            try {
                val page = pathRepo.getAllPaged(limit = pageSize, startAfterId = lastPathId)
                val paths = page.documents
                if (paths.isEmpty()) {
                    endReached = true
                    isLoading = false
                    return@launch
                }
                lastPathId = page.lastDocumentId
                val userIds = paths.map { it.data.userId }.toSet()
                val users = userRepo.get(userIds).associate { it.id to it.data }
                val newItems = paths.map { path ->
                    val user = users[path.data.userId] ?: UserSchema("Deleted User")
                    Pair(FirestoreDocument(path.data.userId, user), path)
                }
                loadedItems += newItems
                _uiState.value = HomeState.Success(loadedItems.toList())
            } catch (e: Exception) {
                _uiState.value = HomeState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }
}

