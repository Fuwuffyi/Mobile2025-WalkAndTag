package com.github.walkandtag.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.repository.SavedPathRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val user: FirestoreDocument<UserSchema>? = null,
    val paths: List<FirestoreDocument<PathSchema>> = emptyList(),
    val isRecording: Boolean = false,
    val favoritePathIds: Collection<String> = emptyList()
)

class ProfileViewModel(
    private val auth: Authentication,
    private val userRepo: FirestoreRepository<UserSchema>,
    private val pathRepo: FirestoreRepository<PathSchema>,
    private val savedPathRepo: SavedPathRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    private var lastPathId: String? = null
    private var isLoading = false
    private var endReached = false
    private val pageSize = 10u
    private var currentUserId: String? = null
    fun loadUserProfile(userId: String) {
        currentUserId = userId
        lastPathId = null
        endReached = false
        isLoading = false
        _state.value = ProfileState()
        viewModelScope.launch {
            val userDoc = userRepo.get(userId)
            val currentUserDoc = userRepo.get(auth.getCurrentUserId()!!)
            val favorites = currentUserDoc?.data?.favoritePathIds ?: emptyList()
            _state.value = ProfileState(
                user = userDoc, favoritePathIds = favorites
            )
            loadNextPage()
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
            _state.update { it.copy(favoritePathIds = newFavorites) }
        }
    }

    fun loadNextPage() {
        val userId = currentUserId ?: return
        if (isLoading || endReached) return
        isLoading = true
        viewModelScope.launch {
            try {
                val pagedResult = pathRepo.queryPaged(
                    limit = pageSize, startAfterId = lastPathId
                ) {
                    equalTo(PathSchema::userId, userId)
                }
                val newPaths = pagedResult.documents
                val endReached = newPaths.isEmpty()
                val lastDocId = pagedResult.lastDocumentId
                if (!endReached) {
                    lastPathId = lastDocId
                    _state.update { it.copy(paths = it.paths + newPaths) }
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun isOwnProfile(): Boolean {
        return _state.value.user?.id == auth.getCurrentUserId()
    }

    fun toggleRecording() {
        _state.update { it.copy(isRecording = !it.isRecording) }
    }

    fun savePath(pathName: String, pathDescription: String? = null) {
        val userId = auth.getCurrentUserId() ?: return
        val pathPoints = savedPathRepo.points
        if (pathPoints.size < 2) return
        val lengthMeters = pathPoints.zipWithNext().sumOf { (start, end) ->
            FloatArray(1).also {
                Location.distanceBetween(
                    start.latitude, start.longitude, end.latitude, end.longitude, it
                )
            }[0].toDouble()
        }
        val estimatedSeconds = lengthMeters / 1.39
        viewModelScope.launch {
            pathRepo.create(
                PathSchema(
                    userId = userId,
                    creationTimestamp = Timestamp.now(),
                    name = pathName,
                    description = pathDescription.orEmpty(),
                    length = lengthMeters / 1000.0,
                    time = estimatedSeconds / 3600.0,
                    points = pathPoints.toMutableList()
                )
            )
            savedPathRepo.clear()
        }
    }
}
