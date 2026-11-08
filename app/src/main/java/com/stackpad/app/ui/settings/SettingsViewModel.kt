package com.stackpad.app.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stackpad.app.AppContainer
import com.stackpad.app.data.backup.BackupError
import com.stackpad.app.data.backup.BackupException
import com.stackpad.app.data.backup.BackupManager
import com.stackpad.app.data.backup.RestoreSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/** One-off results shown as a snackbar. */
sealed interface SettingsMessage {
    data class Exported(val notes: Int) : SettingsMessage
    data class Restored(val summary: RestoreSummary) : SettingsMessage
    data class Failed(val error: BackupError) : SettingsMessage
}

class SettingsViewModel(private val backup: BackupManager) : ViewModel() {
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _messages = MutableSharedFlow<SettingsMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<SettingsMessage> = _messages.asSharedFlow()

    fun exportTo(resolver: ContentResolver, uri: Uri) = runBusy {
        val notes = withContext(Dispatchers.IO) {
            val out = resolver.openOutputStream(uri, "wt") ?: throw BackupException(BackupError.IO)
            out.use { backup.export(it) }
        }
        SettingsMessage.Exported(notes)
    }

    fun restoreFrom(resolver: ContentResolver, uri: Uri) = runBusy {
        val summary = withContext(Dispatchers.IO) {
            val input = resolver.openInputStream(uri) ?: throw BackupException(BackupError.IO)
            input.use { backup.restore(it) }
        }
        SettingsMessage.Restored(summary)
    }

    private fun runBusy(block: suspend () -> SettingsMessage) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            val message = try {
                block()
            } catch (e: BackupException) {
                SettingsMessage.Failed(e.error)
            } catch (e: IOException) {
                SettingsMessage.Failed(BackupError.IO)
            } catch (e: SecurityException) {
                SettingsMessage.Failed(BackupError.IO)
            }
            _busy.value = false
            _messages.tryEmit(message)
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container.backupManager) }
        }
    }
}
