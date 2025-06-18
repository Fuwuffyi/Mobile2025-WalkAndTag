package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NavbarState(
    val currentPage: String = "",
)

class NavbarViewModel(startPage: String) : ViewModel() {
    private val _uiState = MutableStateFlow(NavbarState(currentPage = startPage))
    val uiState: StateFlow<NavbarState> = _uiState.asStateFlow()

    private fun changePage(value: String) {
        _uiState.update { current -> current.copy(currentPage = value) }
    }

    fun onChangePage(value: String, navController: NavController) {
        if (value == _uiState.value.currentPage) return
        changePage(value)
        navController.navigate(value)
    }
}