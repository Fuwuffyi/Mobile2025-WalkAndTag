package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.db.FirestoreDocument
import com.github.walkandtag.firebase.db.FirestoreRepository
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.firebase.db.schemas.UserSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PathDetailsState(
    var publisher: FirestoreDocument<UserSchema>? = null,
    var path: FirestoreDocument<PathSchema>? = null
)

class PathDetailsViewModel(
    private val userRepo: FirestoreRepository<UserSchema>,
    private val pathRepo: FirestoreRepository<PathSchema>
) : ViewModel() {
    private val _uiState = MutableStateFlow(PathDetailsState())
    val uiState: StateFlow<PathDetailsState> = _uiState.asStateFlow()

    fun loadData(pathId: String) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(path = pathRepo.get(pathId)) }
            if (_uiState.value.path != null) _uiState.update { current ->
                current.copy(
                    publisher = userRepo.get(
                        _uiState.value.path!!.data.userId
                    )
                )
            }
        }
    }
}