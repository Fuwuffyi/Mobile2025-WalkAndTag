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
    var user: FirestoreDocument<UserSchema>? = null,
    var paths: Collection<FirestoreDocument<PathSchema>> = emptyList(),
    var isRecording: Boolean = false
)

class ProfileViewModel(
    private val auth: Authentication,
    private val userRepo: FirestoreRepository<UserSchema>,
    private val pathRepo: FirestoreRepository<PathSchema>,
    private val savedPathRepo: SavedPathRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    private val loadedPaths = mutableListOf<FirestoreDocument<PathSchema>>()
    private var lastPathId: String? = null
    private var isLoading = false
    private var endReached = false
    private val pageSize = 10u
    private var currentUserId: String? = null

    fun loadUserProfile(userId: String) {
        currentUserId = userId
        loadedPaths.clear()
        lastPathId = null
        endReached = false
        isLoading = false
        _state.value = ProfileState(user = null, paths = emptyList(), isRecording = false)

        viewModelScope.launch {
            val userDoc = userRepo.get(userId)
            _state.update { it.copy(user = userDoc) }
            loadNextPage() // Load first page of paths for user
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
                    loadedPaths += paths
                    _state.update { it.copy(paths = loadedPaths.toList()) }
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun isOwnProfile(): Boolean {
        return if (_state.value.user == null) {
            false
        } else {
            val currentUserId = auth.getCurrentUserId()
            return currentUserId == _state.value.user!!.id
        }
    }

    fun toggleRecording() {
        _state.update { current -> current.copy(isRecording = !current.isRecording) }
    }

    fun savePath(pathName: String, pathDescription: String? = null) {
        if (!savedPathRepo.isValid) {
            return
        }
        val pathPoints = savedPathRepo.points
        // Calculate distance (meters) for the path
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
        // Calculate estimated walking time (in seconds)
        val estTime = lengthMeters / 1.39
        // Save the new path to the database
        viewModelScope.launch {
            pathRepo.create(
                PathSchema(
                    userId = auth.getCurrentUserId()!!,
                    name = pathName,
                    description = pathDescription ?: "",
                    length = (lengthMeters / 1000.0),
                    time = (estTime / 3600),
                    points = pathPoints.toMutableList()
                )
            )
        }
    }
}
