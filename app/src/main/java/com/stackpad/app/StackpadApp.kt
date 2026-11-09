package com.stackpad.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.stackpad.app.audio.AudioPlayer
import com.stackpad.app.audio.AudioRecorder
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.backup.BackupManager
import com.stackpad.app.data.db.MIGRATION_1_2
import com.stackpad.app.data.db.MIGRATION_2_3
import com.stackpad.app.data.db.StackpadDatabase
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.prefs.ListPreferences
import com.stackpad.app.data.prefs.SecurityPreferences
import com.stackpad.app.data.prefs.TemplatePreferences
import com.stackpad.app.data.prefs.ThemePreferences
import com.stackpad.app.notifications.AlarmReminderScheduler
import com.stackpad.app.widget.WidgetContent
import com.stackpad.app.widget.WidgetRenderer
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Hand-rolled dependency container shared by the whole app. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: StackpadDatabase =
        Room.databaseBuilder(appContext, StackpadDatabase::class.java, "stackpad.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    val mediaStorage = MediaStorage(appContext)
    val repository = NoteRepository(
        database.noteDao(),
        database.folderDao(),
        mediaStorage,
        AlarmReminderScheduler(appContext),
        appScope,
    )
    val themePreferences = ThemePreferences(appContext)
    val listPreferences = ListPreferences(appContext)
    val securityPreferences = SecurityPreferences(appContext)
    val templatePreferences = TemplatePreferences(appContext)
    val audioPlayer = AudioPlayer()

    val backupManager = BackupManager(
        appContext,
        repository,
        mediaStorage,
        runCatching { appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName }
            .getOrNull().orEmpty(),
    )

    /** Redraws the home-screen widget with the latest note (or "Locked"). [onDone] runs afterwards. */
    fun refreshWidget(onDone: () -> Unit = {}) {
        appScope.launch {
            try {
                val content = when {
                    securityPreferences.appLockEnabled.first() -> WidgetContent.Locked
                    else -> repository.latestSummary()?.let { WidgetContent.Latest(it) } ?: WidgetContent.Empty
                }
                WidgetRenderer.push(appContext, content)
            } finally {
                onDone()
            }
        }
    }

    /** Housekeeping at launch: erase expired trash, then remove unreferenced media files. */
    fun runStartupCleanup() {
        appScope.launch {
            repository.purgeExpiredTrash()
            repository.sweepOrphanMedia()
            repository.rescheduleAllReminders()
        }
    }

    /** Re-creates reminder alarms that the system dropped (after a reboot or an app update). */
    fun rescheduleReminders(): Job = appScope.launch { repository.rescheduleAllReminders() }

    fun createAudioRecorder(): AudioRecorder = AudioRecorder(appContext)
}

class StackpadApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.runStartupCleanup()
    }
}
