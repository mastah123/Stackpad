package com.stackpad.app

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.fragment.app.FragmentActivity
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.security.AppLockController
import com.stackpad.app.ui.lock.LockScreen
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.data.prefs.ThemeMode
import com.stackpad.app.notifications.ReminderNotifications
import com.stackpad.app.ui.navigation.AppNavigation
import com.stackpad.app.ui.theme.StackpadTheme

class MainActivity : FragmentActivity() {
    private lateinit var appLock: AppLockController

    /** Note to open because the user tapped a reminder notification. */
    private var noteToOpen by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as StackpadApp).container
        appLock = AppLockController(this, container.securityPreferences)
        // Only on a fresh launch: after a rotation the saved navigation state already has the note.
        if (savedInstanceState == null) noteToOpen = noteFrom(intent)

        setContent {
            val mode by container.themePreferences.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Keep status/navigation bar icon colors matching the chosen theme, not just the system's.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }

            val locked by appLock.locked.collectAsStateWithLifecycle()

            StackpadTheme(darkTheme = dark) {
                Box {
                    // While locked the app stays in place (so no state is lost) but is hidden from
                    // touch and from accessibility services behind the lock screen.
                    Box(modifier = if (locked) Modifier.clearAndSetSemantics {} else Modifier) {
                        AppNavigation(
                            container = container,
                            appLock = appLock,
                            noteToOpen = noteToOpen,
                            onNoteOpened = { noteToOpen = null },
                        )
                    }
                    if (locked) LockScreen(onUnlock = appLock::unlock)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appLock.onStart()
    }

    override fun onStop() {
        super.onStop()
        appLock.onStop()
        // The widget shows the latest note; refresh it once pending edits have been saved.
        (application as StackpadApp).container.refreshWidget()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        noteFrom(intent)?.let { noteToOpen = it }
    }

    /** The note a launch intent asks for: a reminder's note, the widget's latest note, or a new one. */
    private fun noteFrom(intent: Intent?): String? = when {
        intent == null -> null
        intent.getBooleanExtra(EXTRA_NEW_NOTE, false) -> {
            // Consumed so that recreating the activity does not open another blank note.
            intent.removeExtra(EXTRA_NEW_NOTE)
            BlockFactory.newId()
        }
        else -> intent.getStringExtra(ReminderNotifications.EXTRA_NOTE_ID)
    }

    companion object {
        /** Same extra as reminder notifications use, so the widget can open a note. */
        const val EXTRA_NOTE_ID = ReminderNotifications.EXTRA_NOTE_ID
        const val EXTRA_NEW_NOTE = "com.stackpad.app.extra.NEW_NOTE"
    }
}
