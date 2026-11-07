package com.stackpad.app.ui.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.domain.notes.TrashPolicy
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.SmallIconButton
import com.stackpad.app.ui.theme.noteCardColor

/** Deleted notes. Each can be restored or erased for good; old ones are erased automatically. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onBack: () -> Unit,
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var pendingErase by remember { mutableStateOf<NoteSummary?>(null) }
    var confirmEmpty by remember { mutableStateOf(false) }
    val current = notes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trash_title), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(onClick = { confirmEmpty = true }, enabled = !current.isNullOrEmpty()) {
                        Text(stringResource(R.string.trash_empty_action))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                current == null -> Box(Modifier.fillMaxSize())
                current.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.trash_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.trash_info, TrashPolicy.RETENTION_DAYS),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(current, key = { it.id }) { note ->
                            TrashCard(
                                note = note,
                                onRestore = { viewModel.restore(note.id) },
                                onEraseForever = { pendingErase = note },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingErase?.let { note ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_erase_note_title),
            message = stringResource(R.string.dialog_erase_note_message),
            confirmLabel = stringResource(R.string.action_delete_forever),
            onConfirm = {
                viewModel.deleteForever(note.id)
                pendingErase = null
            },
            onDismiss = { pendingErase = null },
        )
    }

    if (confirmEmpty) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_empty_trash_title),
            message = stringResource(R.string.dialog_empty_trash_message),
            confirmLabel = stringResource(R.string.action_delete_forever),
            onConfirm = {
                viewModel.emptyTrash()
                confirmEmpty = false
            },
            onDismiss = { confirmEmpty = false },
        )
    }
}

@Composable
private fun TrashCard(
    note: NoteSummary,
    onRestore: () -> Unit,
    onEraseForever: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val daysLeft = note.trashedAt?.let { TrashPolicy.daysLeft(it, System.currentTimeMillis()) } ?: 0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = noteCardColor(note.color),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { stringResource(R.string.note_untitled) },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.preview.isNotBlank()) {
                    Text(
                        text = note.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = LocalContext.current.resources.getQuantityString(R.plurals.trash_days_left, daysLeft, daysLeft),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            SmallIconButton(
                icon = Icons.Filled.RestoreFromTrash,
                contentDescription = stringResource(R.string.cd_restore_note),
                onClick = onRestore,
                tint = colors.primary,
            )
            SmallIconButton(
                icon = Icons.Filled.DeleteForever,
                contentDescription = stringResource(R.string.cd_erase_note),
                onClick = onEraseForever,
            )
        }
    }
}
