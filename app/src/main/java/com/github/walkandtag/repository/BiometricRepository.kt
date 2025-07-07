package com.github.walkandtag.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BiometricRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val DATASTORE_KEY = booleanPreferencesKey("biometric")
    }

    sealed class BiometricPreferenceState {
        object Loading : BiometricPreferenceState()
        data class Loaded(val enabled: Boolean) : BiometricPreferenceState()
    }

    val biometricEnabledFlow: Flow<BiometricPreferenceState> =
        dataStore.data.onStart { emit(emptyPreferences()) }.map { preferences ->
            if (preferences.asMap().isEmpty()) {
                BiometricPreferenceState.Loading
            } else {
                val enabled = preferences[DATASTORE_KEY] ?: false
                BiometricPreferenceState.Loaded(enabled)
            }
        }.catch { emit(BiometricPreferenceState.Loaded(false)) }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATASTORE_KEY] = enabled
        }
    }
}
