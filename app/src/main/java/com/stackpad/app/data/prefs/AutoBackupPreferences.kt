package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpad.app.domain.backup.BackupFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AutoBackupSettings(
    val enabled: Boolean = false,
    val frequency: BackupFrequency = BackupFrequency.DAILY,
    /** The folder (a document-tree address) the user picked, or null. */
    val folderUri: String? = null,
    val lastSuccessAt: Long = 0L,
    /** True when the most recent attempt failed. */
    val lastFailed: Boolean = false,
)

/** Remembers the automatic backup settings and how the last run went. */
class AutoBackupPreferences(private val context: Context) {
    private val enabledKey = booleanPreferencesKey("autobackup_enabled")
    private val frequencyKey = stringPreferencesKey("autobackup_frequency")
    private val folderKey = stringPreferencesKey("autobackup_folder")
    private val lastSuccessKey = longPreferencesKey("autobackup_last_success")
    private val lastFailedKey = booleanPreferencesKey("autobackup_last_failed")

    val settings: Flow<AutoBackupSettings> = context.settingsDataStore.data.map { p ->
        AutoBackupSettings(
            enabled = p[enabledKey] ?: false,
            frequency = runCatching { BackupFrequency.valueOf(p[frequencyKey].orEmpty()) }
                .getOrDefault(BackupFrequency.DAILY),
            folderUri = p[folderKey],
            lastSuccessAt = p[lastSuccessKey] ?: 0L,
            lastFailed = p[lastFailedKey] ?: false,
        )
    }

    suspend fun setEnabled(enabled: Boolean) = context.settingsDataStore.edit { it[enabledKey] = enabled }

    suspend fun setFrequency(frequency: BackupFrequency) =
        context.settingsDataStore.edit { it[frequencyKey] = frequency.name }

    suspend fun setFolder(uri: String) = context.settingsDataStore.edit {
        it[folderKey] = uri
        it[lastFailedKey] = false
    }

    suspend fun recordRun(success: Boolean, now: Long) = context.settingsDataStore.edit {
        it[lastFailedKey] = !success
        if (success) it[lastSuccessKey] = now
    }
}
