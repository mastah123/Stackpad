package com.stackpad.app.ui.settings

import com.stackpad.app.ui.icons.AppIcons
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.prefs.AutoBackupSettings
import com.stackpad.app.domain.backup.BackupFrequency

/** Settings rows for automatic backup: switch, folder, how often, back up now, last result. */
@Composable
fun AutoBackupSection(settings: AutoBackupSettings, busy: Boolean, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    // True while the switch waits for the user to pick a folder first.
    var enableAfterPick by remember { mutableStateOf(false) }
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            viewModel.setBackupFolder(uri.toString(), enable = enableAfterPick)
        }
        enableAfterPick = false
    }

    SectionHeader(R.string.settings_section_auto_backup)
    SwitchRow(
        icon = AppIcons.Schedule,
        title = R.string.settings_auto_backup_title,
        subtitle = R.string.settings_auto_backup_subtitle,
        checked = settings.enabled,
        onCheckedChange = { on ->
            if (on && settings.folderUri == null) {
                enableAfterPick = true
                folderLauncher.launch(null)
            } else {
                viewModel.setAutoBackupEnabled(on)
            }
        },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = settings.frequency == BackupFrequency.DAILY,
            onClick = { viewModel.setBackupFrequency(BackupFrequency.DAILY) },
            label = { Text(stringResource(R.string.settings_auto_backup_daily)) },
        )
        FilterChip(
            selected = settings.frequency == BackupFrequency.WEEKLY,
            onClick = { viewModel.setBackupFrequency(BackupFrequency.WEEKLY) },
            label = { Text(stringResource(R.string.settings_auto_backup_weekly)) },
        )
    }
    ActionRow(
        icon = AppIcons.Folder,
        title = R.string.settings_auto_backup_folder_title,
        subtitle = if (settings.folderUri == null) R.string.settings_auto_backup_folder_none else R.string.settings_auto_backup_folder_change,
        enabled = !busy,
        onClick = { folderLauncher.launch(null) },
    )
    ActionRow(
        icon = AppIcons.FileUpload,
        title = R.string.settings_auto_backup_now_title,
        subtitle = R.string.settings_auto_backup_now_subtitle,
        enabled = !busy && settings.folderUri != null,
        onClick = { viewModel.backUpNow() },
    )
    val status = when {
        settings.lastFailed -> stringResource(R.string.settings_auto_backup_failed)
        settings.lastSuccessAt > 0L -> stringResource(
            R.string.settings_auto_backup_last,
            DateUtils.getRelativeTimeSpanString(settings.lastSuccessAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS),
        )
        else -> stringResource(R.string.settings_auto_backup_never)
    }
    Text(
        text = status,
        style = MaterialTheme.typography.bodySmall,
        color = if (settings.lastFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}
