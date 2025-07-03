package com.github.walkandtag.ui.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.walkandtag.repository.LanguageRepository
import com.github.walkandtag.repository.Theme
import com.github.walkandtag.repository.ThemeRepository
import com.github.walkandtag.ui.pages.Languages
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeState(val theme: Theme)
data class LanguageState(val lang: Languages)

class GlobalViewModel(
    private val themeRepo: ThemeRepository,
    private val langRepo: LanguageRepository,

    ) : ViewModel() {
    val snackbarHostState = SnackbarHostState()
    val themeState = themeRepo.theme.map { ThemeState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ThemeState(Theme.System)
    )
    val languageState = langRepo.language.map { LanguageState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = LanguageState(Languages.System)
    )

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun toggleTheme() {
        val currTheme = themeState.value.theme
        viewModelScope.launch {
            if (currTheme == Theme.Dark) {
                themeRepo.setTheme(Theme.Light)
            } else {
                themeRepo.setTheme(Theme.Dark)
            }
        }
    }

    fun toggleSystemTheme() {
        val currTheme = themeState.value.theme
        viewModelScope.launch {
            if (currTheme == Theme.System) {
                themeRepo.setTheme(Theme.Light)
            } else {
                themeRepo.setTheme(Theme.System)
            }
        }
    }

    fun setLang(newLang: Languages) {
        viewModelScope.launch {
            langRepo.setLang(newLang)
        }
    }
}
