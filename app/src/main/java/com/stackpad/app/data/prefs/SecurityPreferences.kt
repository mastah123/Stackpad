package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Persists whether the app lock is on. Off by default. */
class SecurityPreferences(private val context: Context) {
    private val key = booleanPreferencesKey("app_lock_enabled")

    val appLockEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[key] ?: false }

    suspend fun isAppLockEnabled(): Boolean = appLockEnabled.first()

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[key] = enabled }
    }
}
