package com.stackpad.app.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder

/** Top bar while several notes are selected: close, count, select all, pin, trash and a menu for folder and tag. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSelectionBar(
    count: Int,
    allPinned: Boolean,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onTogglePin: () -> Unit,
    onTrash: () -> Unit,
    onMoveToFolder: () -> Unit,
    onAddTag: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(R.string.selection_count, count), style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close_selection))
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Filled.SelectAll, contentDescription = stringResource(R.string.action_select_all))
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = if (allPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                    contentDescription = stringResource(if (allPinned) R.string.cd_unpin else R.string.cd_pin),
                )
            }
            IconButton(onClick = onTrash) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_trash_note))
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_selected_notes_more))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_move_to_folder)) },
                        onClick = { menuOpen = false; onMoveToFolder() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_add_tag_to_notes)) },
                        onClick = { menuOpen = false; onAddTag() },
                    )
                }
            }
        },
    )
}

/** Choose a folder (or "No folder") to file the selected notes in. */
@Composable
fun FolderChoiceDialog(
    folders: List<Folder>,
    onChoose: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_choose_folder_title), style = MaterialTheme.typography.titleMedium) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn {
                item {
                    ChoiceRow(stringResource(R.string.folder_none)) { onChoose(null) }
                }
                items(folders.size) { index ->
                    ChoiceRow(folders[index].name) { onChoose(folders[index].id) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun ChoiceRow(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
    )
}
