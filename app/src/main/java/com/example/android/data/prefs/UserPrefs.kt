package com.example.android.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPrefs(private val context: Context) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }

    suspend fun setAuthToken(token: String?, refreshToken: String? = null) {
        context.dataStore.edit { preferences ->
            if (token == null) {
                // Just set to empty string instead of removing
                preferences[KEY_TOKEN] = ""
                preferences[KEY_REFRESH_TOKEN] = ""
            } else {
                preferences[KEY_TOKEN] = token
                if (refreshToken != null) {
                    preferences[KEY_REFRESH_TOKEN] = refreshToken
                }
            }
        }
    }
}

