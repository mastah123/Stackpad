package com.stackpad.app.ui.editor

import com.stackpad.app.ui.icons.AppIcons
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteVersion
import com.stackpad.app.domain.export.DocItems
import com.stackpad.app.domain.export.ExportFormat
import com.stackpad.app.domain.export.NoteExporter
import com.stackpad.app.ui.notes.NotePreviewView

/** Earlier versions of the open note: pick one to look at it, then restore it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    loadVersions: suspend () -> List<NoteVersion>,
    onRestore: (NoteVersion) -> Unit,
    onDismiss: () -> Unit,
) {
    var versions by remember { mutableStateOf<List<NoteVersion>?>(null) }
    var viewing by remember { mutableStateOf<NoteVersion?>(null) }
    LaunchedEffect(Unit) { versions = loadVersions() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val selected = viewing
            if (selected == null) {
                Text(stringResource(R.string.history_title), style = MaterialTheme.typography.titleMedium)
                val list = versions
                when {
                    list == null -> Text(stringResource(R.string.settings_working), style = MaterialTheme.typography.bodyMedium)
                    list.isEmpty() -> Text(
                        stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> list.forEach { version -> VersionRow(version) { viewing = version } }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewing = null }) {
                        Icon(AppIcons.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                    Text(
                        text = whenSaved(selected),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (selected.title.isNotBlank()) {
                    Text(selected.title, style = MaterialTheme.typography.titleLarge)
                }
                NotePreviewView(
                    items = DocItems.blocks(selected.blocks),
                    maxHeight = 360.dp,
                    fadeInto = MaterialTheme.colorScheme.surfaceContainerLow,
                )
                Button(onClick = { onRestore(selected); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.history_restore))
                }
                Text(
                    stringResource(R.string.history_restore_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VersionRow(version: NoteVersion, onClick: () -> Unit) {
    val snippet = remember(version) {
        NoteExporter.exportBlocks(version.blocks, ExportFormat.PLAIN).lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(whenSaved(version), style = MaterialTheme.typography.bodyLarge)
        val label = version.title.ifBlank { snippet }
        if (label.isNotBlank()) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun whenSaved(version: NoteVersion): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(version.savedAt) {
        DateUtils.formatDateTime(
            context,
            version.savedAt,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH,
        )
    }
}
