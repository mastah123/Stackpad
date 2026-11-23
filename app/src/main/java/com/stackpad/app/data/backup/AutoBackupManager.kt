package com.stackpad.app.data.backup

import android.content.Context
import android.net.Uri
import com.stackpad.app.data.prefs.AutoBackupPreferences
import com.stackpad.app.data.prefs.AutoBackupSettings
import com.stackpad.app.domain.backup.BackupFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Settings and actions for automatic backup; keeps the background job in step with the settings. */
class AutoBackupManager(
    private val context: Context,
    private val prefs: AutoBackupPreferences,
    private val writer: AutoBackupWriter,
) {
    val settings: Flow<AutoBackupSettings> = prefs.settings

    suspend fun setEnabled(enabled: Boolean) {
        prefs.setEnabled(enabled)
        reschedule()
    }

    suspend fun setFrequency(frequency: BackupFrequency) {
        prefs.setFrequency(frequency)
        reschedule()
    }

    suspend fun setFolder(uri: String) {
        prefs.setFolder(uri)
        reschedule()
    }

    /** Backs up now, whatever the schedule says. False when no folder is set or writing failed. */
    suspend fun runNow(): Boolean {
        val folder = prefs.settings.first().folderUri?.let(Uri::parse) ?: return false
        val ok = writer.run(folder)
        prefs.recordRun(ok, System.currentTimeMillis())
        return ok
    }

    /** Called by the background job: does nothing when the feature is off. */
    suspend fun runScheduled() {
        val s = prefs.settings.first()
        if (s.enabled && s.folderUri != null) runNow()
    }

    private suspend fun reschedule() {
        val s = prefs.settings.first()
        if (s.enabled && s.folderUri != null) {
            AutoBackupScheduler.schedule(context, s.frequency)
        } else {
            AutoBackupScheduler.cancel(context)
        }
    }
}
