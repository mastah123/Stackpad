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
import com.stackpad.app.data.backup.AutoBackupManager
import com.stackpad.app.data.export.NotesArchiveExporter
import com.stackpad.app.data.prefs.AutoBackupSettings
import com.stackpad.app.domain.backup.BackupFrequency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.stackpad.app.data.importing.ImportError
import com.stackpad.app.data.importing.ImportException
import com.stackpad.app.data.importing.ImportSummary
import com.stackpad.app.data.importing.ImportKind
import com.stackpad.app.data.importing.Importers
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
    data class ArchiveExported(val notes: Int, val skippedLocked: Int) : SettingsMessage
    data class AutoBackupRan(val success: Boolean) : SettingsMessage
    data class Imported(val summary: ImportSummary) : SettingsMessage
    data class ImportFailed(val error: ImportError) : SettingsMessage
}

class SettingsViewModel(
    private val backup: BackupManager,
    private val importers: Importers,
    private val archive: NotesArchiveExporter,
    private val autoBackup: AutoBackupManager,
) : ViewModel() {
    val autoBackupSettings: StateFlow<AutoBackupSettings> = autoBackup.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AutoBackupSettings())

    fun exportAllAsMarkdown(resolver: ContentResolver, uri: Uri) = runBusy {
        val result = withContext(Dispatchers.IO) {
            val out = resolver.openOutputStream(uri, "wt") ?: throw BackupException(BackupError.IO)
            out.use { archive.export(it) }
        }
        SettingsMessage.ArchiveExported(result.exported, result.skippedLocked)
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch { autoBackup.setEnabled(enabled) }
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        viewModelScope.launch { autoBackup.setFrequency(frequency) }
    }

    /** Remembers the picked folder; with [enable] the switch is turned on as well. */
    fun setBackupFolder(uri: String, enable: Boolean) {
        viewModelScope.launch {
            autoBackup.setFolder(uri)
            if (enable) autoBackup.setEnabled(true)
        }
    }

    fun backUpNow() = runBusy { SettingsMessage.AutoBackupRan(autoBackup.runNow()) }

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

    fun importFrom(kind: ImportKind, resolver: ContentResolver, uris: List<Uri>) = runBusy {
        if (uris.isEmpty()) return@runBusy SettingsMessage.ImportFailed(ImportError.NOTHING_FOUND)
        SettingsMessage.Imported(importers.import(kind, resolver, uris))
    }

    private fun runBusy(block: suspend () -> SettingsMessage) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            val message = try {
                block()
            } catch (e: BackupException) {
                SettingsMessage.Failed(e.error)
            } catch (e: ImportException) {
                SettingsMessage.ImportFailed(e.error)
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
            initializer { SettingsViewModel(container.backupManager, container.importers, container.notesArchiveExporter, container.autoBackup) }
        }
    }
}
