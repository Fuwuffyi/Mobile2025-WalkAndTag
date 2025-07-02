package com.github.walkandtag.ui.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingState(val theme: Theme)

class GlobalViewModel(
    private val themeRepo: ThemeRepository
) : ViewModel() {
    val snackbarHostState = SnackbarHostState()
    val themeState = themeRepo.theme.map { SettingState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingState(Theme.System)
    )

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun setTheme(theme: Theme) = viewModelScope.launch {
        themeRepo.setTheme(theme)
    }
}
