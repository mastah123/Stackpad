package com.stackpad.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.stackpad.app.audio.AudioPlayer
import com.stackpad.app.audio.AudioRecorder
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.db.StackpadDatabase
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.prefs.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Hand-rolled dependency container shared by the whole app. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: StackpadDatabase =
        Room.databaseBuilder(appContext, StackpadDatabase::class.java, "stackpad.db").build()

    val mediaStorage = MediaStorage(appContext)
    val repository = NoteRepository(database.noteDao(), mediaStorage, appScope)
    val themePreferences = ThemePreferences(appContext)
    val audioPlayer = AudioPlayer()

    fun createAudioRecorder(): AudioRecorder = AudioRecorder(appContext)
}

class StackpadApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
