package com.github.walkandtag.ui.viewmodel

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PathDetailsState(
    var publisher: FirestoreDocument<UserSchema>? = null,
    var path: FirestoreDocument<PathSchema>? = null,
    var isFavorite: Boolean = false
)

class PathDetailsViewModel(
    private val auth: Authentication,
    private val userRepo: FirestoreRepository<UserSchema>,
    private val pathRepo: FirestoreRepository<PathSchema>
) : ViewModel() {
    private val _uiState = MutableStateFlow(PathDetailsState())
    val uiState: StateFlow<PathDetailsState> = _uiState.asStateFlow()

    fun loadData(pathId: String) {
        viewModelScope.launch {
            val pathDoc = pathRepo.get(pathId)
            _uiState.update { it.copy(path = pathDoc) }
            pathDoc?.let { pathWrapper ->
                val publisherDoc = userRepo.get(pathWrapper.data.userId)
                val user = userRepo.get(auth.getCurrentUserId()!!)
                val isFav = user?.data?.favoritePathIds?.contains(pathId) == true
                _uiState.update {
                    it.copy(
                        publisher = publisherDoc, isFavorite = isFav
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentUserId = auth.getCurrentUserId()!!
            val currentPath = _uiState.value.path
            if (currentPath == null) return@launch
            val pathId = currentPath.id
            val userDoc = userRepo.get(currentUserId)
            if (userDoc != null) {
                val favorites = userDoc.data.favoritePathIds
                val isCurrentlyFav = pathId in favorites
                if (isCurrentlyFav) {
                    favorites.remove(pathId)
                } else {
                    favorites.add(pathId)
                }
                userRepo.update(
                    id = currentUserId, item = userDoc.data.copy(favoritePathIds = favorites)
                )
                _uiState.update { it.copy(isFavorite = !isCurrentlyFav) }
            }
        }
    }
}
