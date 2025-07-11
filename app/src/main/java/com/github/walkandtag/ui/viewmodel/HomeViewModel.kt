package com.github.walkandtag.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeFilters(
    val nameQuery: String = "",
    val minLength: Int = 0,
    val maxLength: Int = 10000,
    val minTime: Int = 0,
    val maxTime: Int = 1440,
    val showFavorites: Boolean = false,
    val sortOptions: Map<SortOption, SortDirection> = mapOf(SortOption.DATE to SortDirection.DESC)
)

enum class SortOption {
    NAME, AUTHOR, LENGTH, TIME, DATE
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
    data class Success(
        val items: List<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>,
        val favoritePathIds: Collection<String>
    ) : HomeState()

    data class Error(val message: String) : HomeState()
}

class HomeViewModel(
    private val auth: Authentication,
    private val pathRepo: FirestoreRepository<PathSchema>,
    private val userRepo: FirestoreRepository<UserSchema>
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()
    private val _favoritePathIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritePathIds: StateFlow<Set<String>> = _favoritePathIds.asStateFlow()
    private val _filters = MutableStateFlow(HomeFilters())
    val filters: StateFlow<HomeFilters> = _filters.asStateFlow()
    private val loadedItems =
        mutableListOf<Pair<FirestoreDocument<UserSchema>, FirestoreDocument<PathSchema>>>()
    private var lastPathId: String? = null
    private var isLoading = false
    private var endReached = false

    init {
        loadCurrentUserFavorites()
        loadNextPage()
    }

    private fun loadCurrentUserFavorites() {
        viewModelScope.launch {
            val currentUserId = auth.getCurrentUserId() ?: return@launch
            val currentUser = userRepo.get(currentUserId)
            _favoritePathIds.value = currentUser?.data?.favoritePathIds?.toSet() ?: emptySet()
        }
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
                val currentFilters = filters.value
                val pageResult = pathRepo.queryPaged(
                    limit = 15u, startAfterId = lastPathId
                ) {
                    if (currentFilters.nameQuery.isNotBlank()) {
                        equalTo(PathSchema::name, currentFilters.nameQuery)
                    }
                    if (currentFilters.minLength > 0) {
                        greaterThanOrEqualTo(
                            PathSchema::length,
                            currentFilters.minLength.toDouble() / 1000.0
                        )
                    }
                    if (currentFilters.maxLength < 10000) {
                        lessThanOrEqualTo(
                            PathSchema::length,
                            currentFilters.maxLength.toDouble() / 1000.0
                        )
                    }
                    if (currentFilters.minTime > 0) {
                        greaterThanOrEqualTo(
                            PathSchema::time,
                            currentFilters.minTime.toDouble() / 60.0
                        )
                    }
                    if (currentFilters.maxTime < 1440) {
                        lessThanOrEqualTo(
                            PathSchema::time,
                            currentFilters.maxTime.toDouble() / 60.0
                        )
                    }
                    if (currentFilters.showFavorites) {
                        val favIds = _favoritePathIds.value.toList()
                        if (favIds.isNotEmpty()) {
                            whereDocumentIdIn(favIds)
                        }
                    }
                    if (currentFilters.sortOptions.isNotEmpty()) {
                        val (sortOption, direction) = currentFilters.sortOptions.entries.first()
                        val ascending = direction == SortDirection.ASC
                        when (sortOption) {
                            SortOption.NAME -> orderBy(PathSchema::name, ascending)
                            SortOption.AUTHOR -> orderBy(PathSchema::userId, ascending)
                            SortOption.LENGTH -> orderBy(PathSchema::length, ascending)
                            SortOption.TIME -> orderBy(PathSchema::time, ascending)
                            SortOption.DATE -> orderBy(PathSchema::creationTimestamp, ascending)
                        }
                    }
                }
                val paths = pageResult.documents
                if (paths.isEmpty()) {
                    endReached = true
                    return@launch
                }
                lastPathId = pageResult.lastDocumentId
                val userIds = paths.map { it.data.userId }.toSet()
                val users = userRepo.get(userIds).associateBy { it.id }
                val newItems = paths.map { path ->
                    val user = users[path.data.userId]?.data ?: UserSchema("Deleted User")
                    FirestoreDocument(path.data.userId, user) to path
                }
                loadedItems += newItems
                val filteredItems = if (filters.value.showFavorites) {
                    loadedItems.filter { _favoritePathIds.value.contains(it.second.id) }
                } else {
                    loadedItems
                }
                _state.value = HomeState.Success(filteredItems.toList(), _favoritePathIds.value)
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Unknown error")
                Log.e("HOME_FILTERS", "Error while applying filters", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleFavorite(pathId: String) {
        viewModelScope.launch {
            val currentUserId = auth.getCurrentUserId() ?: return@launch
            val userDoc = userRepo.get(currentUserId) ?: return@launch
            val newFavorites = userDoc.data.favoritePathIds.toMutableSet().apply {
                if (contains(pathId)) remove(pathId) else add(pathId)
            }
            userRepo.update(
                userDoc.data.copy(favoritePathIds = newFavorites.toMutableList()), userDoc.id
            )
            _favoritePathIds.value = newFavorites
            (_state.value as? HomeState.Success)?.let {
                _state.value = it.copy(favoritePathIds = newFavorites.toList())
            }
        }
    }
}
