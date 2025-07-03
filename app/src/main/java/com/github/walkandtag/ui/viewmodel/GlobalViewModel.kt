package com.github.walkandtag.ui.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.repository.Language
import com.github.walkandtag.repository.LanguageRepository
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.repository.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GlobalState(val theme: Theme, val language: Language)

class GlobalViewModel(
    private val themeRepo: ThemeRepository,
    private val langRepo: LanguageRepository,

    ) : ViewModel() {
    val snackbarHostState = SnackbarHostState()
    val globalState = combine(
        themeRepo.theme, langRepo.language
    ) { theme, language ->
        GlobalState(theme, language)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = GlobalState(Theme.System, Language.System)
    )

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun toggleTheme() {
        val currTheme = globalState.value.theme
        viewModelScope.launch {
            if (currTheme == Theme.Dark) {
                themeRepo.setTheme(Theme.Light)
            } else {
                themeRepo.setTheme(Theme.Dark)
            }
        }
    }

    fun toggleSystemTheme() {
        val currTheme = globalState.value.theme
        viewModelScope.launch {
            if (currTheme == Theme.System) {
                themeRepo.setTheme(Theme.Light)
            } else {
                themeRepo.setTheme(Theme.System)
            }
        }
    }

    fun setLang(newLang: Language) {
        viewModelScope.launch {
            langRepo.setLang(newLang)
        }
    }
}
