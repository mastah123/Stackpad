package com.stackpad.app

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.data.prefs.ThemeMode
import com.stackpad.app.notifications.ReminderNotifications
import com.stackpad.app.ui.navigation.AppNavigation
import com.stackpad.app.ui.theme.StackpadTheme

class MainActivity : ComponentActivity() {
    /** Note to open because the user tapped a reminder notification. */
    private var noteToOpen by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as StackpadApp).container
        noteToOpen = intent?.getStringExtra(ReminderNotifications.EXTRA_NOTE_ID)

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

            StackpadTheme(darkTheme = dark) {
                AppNavigation(
                    container = container,
                    noteToOpen = noteToOpen,
                    onNoteOpened = { noteToOpen = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(ReminderNotifications.EXTRA_NOTE_ID)?.let { noteToOpen = it }
    }
}
