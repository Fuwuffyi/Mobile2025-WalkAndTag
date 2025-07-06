package com.github.walkandtag.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BiometricRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val BiometricEnabledPreferredKey = booleanPreferencesKey("biometric")
    }

    val biometricEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
            try {
                preferences[BiometricEnabledPreferredKey] ?: false
            } catch (e: Exception) {
                false
            }
        }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BiometricEnabledPreferredKey] = enabled
        }
    }
}
