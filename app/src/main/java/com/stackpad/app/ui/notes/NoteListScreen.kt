package com.stackpad.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.FolderNames
import com.stackpad.app.domain.notes.TrashPolicy
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.TextInputDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onOpenTrash: () -> Unit,
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var pendingTrash by remember { mutableStateOf<NoteSummary?>(null) }
    var newFolderDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Folder?>(null) }
    var deleteTarget by remember { mutableStateOf<Folder?>(null) }

    // After a note is moved to the trash, offer to undo it.
    LaunchedEffect(viewModel) {
        viewModel.trashed.collect { id ->
            val result = snackbar.showSnackbar(
                message = context.getString(R.string.msg_moved_to_trash),
                actionLabel = context.getString(R.string.action_undo),
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restore(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    SortMenu(current = ui.sort, onSelect = viewModel::setSort)
                    IconButton(onClick = onOpenTrash) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_open_trash))
                    }
                    ThemeMenu(current = themeMode, onSelect = viewModel::setThemeMode)
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_new_note))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchField(
                query = query,
                onQueryChange = viewModel::setQuery,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            FolderFilterBar(
                folders = ui.folders,
                selected = ui.filter.folder,
                onSelect = viewModel::selectFolder,
                onNewFolder = { newFolderDialog = true },
                onRenameFolder = { renameTarget = it },
                onDeleteFolder = { deleteTarget = it },
            )
            if (ui.tags.isNotEmpty()) {
                TagFilterBar(
                    tags = ui.tags,
                    selected = ui.filter.tag,
                    onToggle = viewModel::toggleTag,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            val current = ui.notes
            when {
                current == null -> Box(Modifier.fillMaxSize())
                current.isEmpty() -> EmptyState(query = query, filtered = ui.filter.folder != FolderFilter.All || ui.filter.tag != null)
                else -> NoteList(
                    notes = current,
                    onOpen = onOpenNote,
                    onTogglePin = viewModel::togglePin,
                    onTrash = { pendingTrash = it },
                )
            }
        }
    }

    pendingTrash?.let { note ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_trash_note_title),
            message = stringResource(R.string.dialog_trash_note_message, TrashPolicy.RETENTION_DAYS),
            confirmLabel = stringResource(R.string.action_move_to_trash),
            onConfirm = {
                viewModel.moveToTrash(note.id)
                pendingTrash = null
            },
            onDismiss = { pendingTrash = null },
        )
    }

    if (newFolderDialog) {
        TextInputDialog(
            title = stringResource(R.string.folder_new),
            placeholder = stringResource(R.string.folder_name_hint),
            confirmLabel = stringResource(R.string.action_create),
            maxLength = FolderNames.MAX_LENGTH,
            onConfirm = {
                newFolderDialog = false
                viewModel.createFolder(it)
            },
            onDismiss = { newFolderDialog = false },
        )
    }

    renameTarget?.let { folder ->
        TextInputDialog(
            title = stringResource(R.string.folder_rename),
            initialText = folder.name,
            placeholder = stringResource(R.string.folder_name_hint),
            confirmLabel = stringResource(R.string.action_save),
            maxLength = FolderNames.MAX_LENGTH,
            onConfirm = {
                renameTarget = null
                viewModel.renameFolder(folder.id, it)
            },
            onDismiss = { renameTarget = null },
        )
    }

    deleteTarget?.let { folder ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_folder_title, folder.name),
            message = stringResource(R.string.dialog_delete_folder_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteFolder(folder.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun NoteList(
    notes: List<NoteSummary>,
    onOpen: (String) -> Unit,
    onTogglePin: (NoteSummary) -> Unit,
    onTrash: (NoteSummary) -> Unit,
) {
    val pinned = notes.filter { it.pinned }
    val others = notes.filterNot { it.pinned }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (pinned.isNotEmpty()) {
            item(key = "header_pinned") { SectionLabel(stringResource(R.string.section_pinned)) }
            items(pinned, key = { it.id }) { note ->
                NoteCard(note, { onOpen(note.id) }, { onTogglePin(note) }, { onTrash(note) })
            }
            if (others.isNotEmpty()) {
                item(key = "header_notes") { SectionLabel(stringResource(R.string.section_notes)) }
            }
        }
        items(others, key = { it.id }) { note ->
            NoteCard(note, { onOpen(note.id) }, { onTogglePin(note) }, { onTrash(note) })
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
    )
}

@Composable
private fun EmptyState(query: String, filtered: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = when {
                query.isNotBlank() -> stringResource(R.string.empty_search, query)
                filtered -> stringResource(R.string.empty_filtered)
                else -> stringResource(R.string.empty_notes)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
