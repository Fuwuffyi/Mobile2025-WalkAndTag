package com.github.walkandtag.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

enum class Theme { Light, Dark, System }

class ThemeRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
    }

    val theme = dataStore.data
        .map { preferences ->
            try {
                Theme.valueOf(preferences[THEME_KEY] ?: Theme.System.name)
            } catch (_: Exception) {
                Theme.System
            }
        }

    suspend fun setTheme(theme: Theme) =
        dataStore.edit { it[THEME_KEY] = theme.toString() }
}
