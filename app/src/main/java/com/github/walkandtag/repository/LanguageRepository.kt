package com.github.walkandtag.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.walkandtag.ui.pages.Languages
import kotlinx.coroutines.flow.map

class LanguageRepository (private val dataStore: DataStore<Preferences>) {
    companion object {
        private val LANG_KEY = stringPreferencesKey("language")
    }

    val language = dataStore.data
        .map { preferences ->
            try {
                Languages.valueOf(preferences[LANG_KEY] ?: Languages.System.name)
            } catch (_: Exception) {
                Languages.System
            }
        }

    suspend fun setLang(lang: Languages) =
        dataStore.edit { it[LANG_KEY] = lang.toString() }

}