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

data class HomeFilters(
    val nameQuery: String = "",
    val authorQuery: String = "",
    val minLength: Int = 0,
    val maxLength: Int = 10000,
    val minTime: Int = 0,
    val maxTime: Int = 1440,
    val showFavorites: Boolean = false,
    val sortOptions: Map<SortOption, SortDirection> = emptyMap()
)

enum class SortOption {
    NAME, AUTHOR, LENGTH, TIME, NEAREST
}

enum class SortDirection {
    NONE, ASC, DESC;

    fun next(): SortDirection = when (this) {
        NONE -> ASC
        ASC -> DESC
        DESC -> NONE
    }
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val items: List<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>) :
        HomeState()

    data class Error(val message: String) : HomeState()
}

// @TODO(): Implement filter functionality
class HomeViewModel(
    private val pathRepo: FirestoreRepository<PathSchema>,
    private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state
    private val _filters = MutableStateFlow(HomeFilters())
    val filters: StateFlow<HomeFilters> = _filters
    private val loadedItems =
        mutableListOf<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>()
    private var lastPathId: String? = null
    private var isLoading = false
    private var endReached = false

    init {
        loadNextPage()
    }

    fun updateFilters(update: HomeFilters.() -> HomeFilters) {
        _filters.value = _filters.value.update()
        refresh()
    }

    private fun refresh() {
        lastPathId = null
        endReached = false
        loadedItems.clear()
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading || endReached) return
        isLoading = true
        _state.value = if (loadedItems.isEmpty()) HomeState.Loading else _state.value
        viewModelScope.launch {
            try {
                val page = pathRepo.getAllPaged(startAfterId = lastPathId)
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
                _state.value = HomeState.Success(loadedItems.toList())
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }
}
