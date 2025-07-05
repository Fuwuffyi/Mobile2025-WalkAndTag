package com.github.walkandtag.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.auth.Authentication
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import com.github.walkandtag.repository.Filter
import com.github.walkandtag.repository.FirestoreRepository
import com.github.walkandtag.repository.SavedPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val user: FirestoreDocument<UserSchema>? = null,
    val paths: List<FirestoreDocument<PathSchema>> = emptyList(),
    val isRecording: Boolean = false
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
            _state.update { it.copy(user = userDoc) }
            loadNextPage()
        }
    }

    fun loadNextPage() {
        val userId = currentUserId ?: return
        if (isLoading || endReached) return
        isLoading = true
        viewModelScope.launch {
            try {
                val page = pathRepo.getFilteredPaged(
                    filters = listOf(Filter("userId", userId)),
                    limit = pageSize,
                    startAfterId = lastPathId
                )

                val paths = page.documents
                if (paths.isEmpty()) {
                    endReached = true
                } else {
                    lastPathId = page.lastDocumentId
                    _state.update { current ->
                        current.copy(paths = current.paths + paths)
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun isOwnProfile(): Boolean {
        val userId = _state.value.user?.id ?: return false
        return auth.getCurrentUserId() == userId
    }

    fun toggleRecording() {
        _state.update { it.copy(isRecording = !it.isRecording) }
    }

    fun savePath(pathName: String, pathDescription: String? = null) {
        val userId = auth.getCurrentUserId() ?: return
        val pathPoints = savedPathRepo.points
        if (pathPoints.size < 2) {
            return
        }
        // Calculate distance (meters)
        val lengthMeters = pathPoints.zipWithNext().sumOf {
            val result = FloatArray(1)
            Location.distanceBetween(
                it.first.latitude,
                it.first.longitude,
                it.second.latitude,
                it.second.longitude,
                result
            )
            result[0].toDouble()
        }
        // Estimate walking time (in seconds)
        val estimatedSeconds = lengthMeters / 1.39
        viewModelScope.launch {
            pathRepo.create(
                PathSchema(
                    userId = userId,
                    name = pathName,
                    description = pathDescription.orEmpty(),
                    length = lengthMeters / 1000.0,   // in km
                    time = estimatedSeconds / 3600.0, // in hours
                    points = pathPoints.toMutableList()
                )
            )
        }
    }
}
