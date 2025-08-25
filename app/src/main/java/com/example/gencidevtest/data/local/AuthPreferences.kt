// app/src/main/java/com/example/gencidevtest/data/local/AuthPreferences.kt
package com.example.gencidevtest.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth_prefs")

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
    }

    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        username: String,
        email: String,
        firstName: String,
        lastName: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.REFRESH_TOKEN] = refreshToken
            preferences[Keys.USER_ID] = userId
            preferences[Keys.USERNAME] = username
            preferences[Keys.EMAIL] = email
            preferences[Keys.FIRST_NAME] = firstName
            preferences[Keys.LAST_NAME] = lastName
        }
    }

    fun getRefreshToken(): Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.REFRESH_TOKEN]
        }


    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun isLoggedIn(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            !preferences[Keys.ACCESS_TOKEN].isNullOrEmpty()
        }
}