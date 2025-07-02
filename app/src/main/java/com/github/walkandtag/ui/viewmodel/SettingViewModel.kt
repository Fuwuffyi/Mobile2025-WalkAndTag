package com.github.walkandtag.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingState(val theme: Theme)

class SettingViewModel(
    private val repository: ThemeRepository
) : ViewModel() {
    val state = repository.theme.map { SettingState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingState(Theme.System)
    )

    fun setTheme(theme: Theme) = viewModelScope.launch {
        repository.setTheme(theme)
    }
}