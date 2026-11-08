package com.stackpad.app.ui.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.data.backup.BackupError
import com.stackpad.app.domain.backup.BackupRules
import com.stackpad.app.security.AppLockController
import com.stackpad.app.security.LockChangeResult

/** Backup and restore, app lock, and app info. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    appLock: AppLockController,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val lockOn by appLock.enabled.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> uri?.let { viewModel.exportTo(context.contentResolver, it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.restoreFrom(context.contentResolver, it) } }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message -> snackbar.showSnackbar(describe(context, message)) }
    }

    var lockMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(lockMessage) {
        lockMessage?.let {
            snackbar.showSnackbar(it)
            lockMessage = null
        }
    }
    fun changeLock(enable: Boolean) {
        appLock.requestChange(enable) { result ->
            if (result == LockChangeResult.UNAVAILABLE) lockMessage = context.getString(R.string.msg_lock_unavailable)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(R.string.settings_section_backup)
            ActionRow(
                icon = Icons.Filled.FileUpload,
                title = R.string.settings_export_title,
                subtitle = R.string.settings_export_subtitle,
                enabled = !busy,
                onClick = { exportLauncher.launch(BackupRules.fileName(System.currentTimeMillis())) },
            )
            ActionRow(
                icon = Icons.Filled.FileDownload,
                title = R.string.settings_restore_title,
                subtitle = R.string.settings_restore_subtitle,
                enabled = !busy,
                onClick = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
            )
            if (busy) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.settings_working), style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SectionHeader(R.string.settings_section_security)
            SwitchRow(
                icon = Icons.Filled.Lock,
                title = R.string.settings_lock_title,
                subtitle = R.string.settings_lock_subtitle,
                checked = lockOn,
                onCheckedChange = ::changeLock,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SectionHeader(R.string.settings_section_about)
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.settings_version, versionName(context)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.settings_license),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun ActionRow(icon: ImageVector, title: Int, subtitle: Int, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        RowText(title, subtitle, enabled)
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, title: Int, subtitle: Int, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) { RowTextContent(title, subtitle, true) }
        // The row handles the click; the switch only shows the state.
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.RowText(title: Int, subtitle: Int, enabled: Boolean) {
    Column(modifier = Modifier.weight(1f)) { RowTextContent(title, subtitle, enabled) }
}

@Composable
private fun RowTextContent(title: Int, subtitle: Int, enabled: Boolean) {
    val alpha = if (enabled) 1f else 0.5f
    Text(
        stringResource(title),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
    )
    Text(
        stringResource(subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
    )
}

private fun versionName(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull().orEmpty()

private fun describe(context: Context, message: SettingsMessage): String = when (message) {
    is SettingsMessage.Exported ->
        context.resources.getQuantityString(R.plurals.msg_exported, message.notes, message.notes)
    is SettingsMessage.Restored -> context.getString(
        R.string.msg_restored,
        message.summary.added,
        message.summary.updated,
        message.summary.skipped,
    )
    is SettingsMessage.Failed -> context.getString(
        when (message.error) {
            BackupError.NOT_A_BACKUP -> R.string.msg_backup_not_a_backup
            BackupError.UNSUPPORTED_VERSION -> R.string.msg_backup_too_new
            BackupError.TOO_LARGE -> R.string.msg_backup_too_large
            BackupError.UNREADABLE -> R.string.msg_backup_unreadable
            BackupError.IO -> R.string.msg_backup_io
        },
    )
}
