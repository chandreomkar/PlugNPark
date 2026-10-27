package com.example.plugnpark.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session_prefs")

data class UserSession(
    val isOnboarded: Boolean = false,
    val isLoggedIn: Boolean = false,
    val phoneNumber: String = "",
    val userRole: String = "SEEKER" // SEEKER, HOST, BOTH
)

class UserSessionManager(private val context: Context) {

    companion object {
        val KEY_IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    val userSessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
        UserSession(
            isOnboarded = prefs[KEY_IS_ONBOARDED] ?: false,
            isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false,
            phoneNumber = prefs[KEY_PHONE_NUMBER] ?: "",
            userRole = prefs[KEY_USER_ROLE] ?: "SEEKER"
        )
    }

    suspend fun setOnboarded(completed: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_ONBOARDED] = completed
        }
    }

    suspend fun saveLoginSession(phone: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_PHONE_NUMBER] = phone
            prefs[KEY_USER_ROLE] = role
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_PHONE_NUMBER] = ""
            prefs[KEY_USER_ROLE] = "SEEKER"
        }
    }
}
