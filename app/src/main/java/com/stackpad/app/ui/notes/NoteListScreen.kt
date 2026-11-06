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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<NoteSummary?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                actions = { ThemeMenu(current = themeMode, onSelect = viewModel::setThemeMode) },
            )
        },
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
            val current = notes
            when {
                current == null -> Box(Modifier.fillMaxSize())
                current.isEmpty() -> EmptyState(query = query)
                else -> NoteList(
                    notes = current,
                    onOpen = onOpenNote,
                    onTogglePin = viewModel::togglePin,
                    onDelete = { pendingDelete = it },
                )
            }
        }
    }

    pendingDelete?.let { note ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_note_title),
            message = stringResource(R.string.dialog_delete_note_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.delete(note.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun NoteList(
    notes: List<NoteSummary>,
    onOpen: (String) -> Unit,
    onTogglePin: (NoteSummary) -> Unit,
    onDelete: (NoteSummary) -> Unit,
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
                NoteCard(note, { onOpen(note.id) }, { onTogglePin(note) }, { onDelete(note) })
            }
            if (others.isNotEmpty()) {
                item(key = "header_notes") { SectionLabel(stringResource(R.string.section_notes)) }
            }
        }
        items(others, key = { it.id }) { note ->
            NoteCard(note, { onOpen(note.id) }, { onTogglePin(note) }, { onDelete(note) })
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
private fun EmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = if (query.isBlank()) {
                stringResource(R.string.empty_notes)
            } else {
                stringResource(R.string.empty_search, query)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
