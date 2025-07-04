package com.github.walkandtag.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import java.util.Locale

enum class Language(val locale: Locale?) {
    System(null), Italiano(Locale.ITALIAN), English(Locale.ENGLISH)
}

class LanguageRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val LANG_KEY = stringPreferencesKey("language")
    }

    val language = dataStore.data.map { preferences ->
            try {
                Language.valueOf(preferences[LANG_KEY] ?: Language.System.name)
            } catch (_: Exception) {
                Language.System
            }
        }

    suspend fun setLang(lang: Language) = dataStore.edit { it[LANG_KEY] = lang.toString() }
}
