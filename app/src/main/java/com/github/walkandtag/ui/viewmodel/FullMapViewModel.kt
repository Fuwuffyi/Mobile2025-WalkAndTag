package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.firebase.db.schemas.PathSchema
import com.github.walkandtag.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FullMapViewModel(
    private val pathRepo: FirestoreRepository<PathSchema>
) : ViewModel() {
    private val _pathState = MutableStateFlow<PathSchema?>(null)
    val pathState: StateFlow<PathSchema?> = _pathState

    fun loadPath(pathId: String) {
        viewModelScope.launch {
            val result = pathRepo.get(pathId)
            _pathState.value = result?.data
        }
    }
}
