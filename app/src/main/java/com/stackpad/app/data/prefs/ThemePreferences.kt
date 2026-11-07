package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

internal val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Persists the user's light/dark preference. Defaults to following the system. */
class ThemePreferences(private val context: Context) {
    private val key = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        prefs[key]
            ?.let { stored -> ThemeMode.values().firstOrNull { it.name == stored } }
            ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[key] = mode.name }
    }
}
