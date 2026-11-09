package com.stackpad.app.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.domain.export.ExportFormat

/** Folder, tags and color of the open note, plus what the options popup needs to change them. */
data class NoteOptionsState(
    val folderId: String?,
    val tags: List<String>,
    val color: Int,
    val folders: List<Folder>,
    val knownTags: List<String>,
)

/** What the options popup can do to the note. */
class NoteOptionsActions(
    val onSelectFolder: (String?) -> Unit,
    val onCreateFolder: (String) -> Unit,
    val onAddTag: (String) -> Unit,
    val onRemoveTag: (String) -> Unit,
    val onSelectColor: (Int) -> Unit,
)

/**
 * Overflow menu of the editor top bar: note options (a small popup right below the dots),
 * share as Markdown or plain text, copy the whole note, and move to trash.
 */
@Composable
fun NoteActionsMenu(
    options: NoteOptionsState,
    optionsActions: NoteOptionsActions,
    optionsOpen: Boolean,
    onOptionsOpenChange: (Boolean) -> Unit,
    onShare: (ExportFormat) -> Unit,
    onCopy: () -> Unit,
    locked: Boolean,
    onToggleLock: () -> Unit,
    onInsertTemplate: () -> Unit,
    onSaveTemplate: () -> Unit,
    onSelectBlocks: () -> Unit,
    onTrash: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val hasOptions = options.folderId != null || options.tags.isNotEmpty() || options.color != 0

    // Both menus hang from the same anchor, so the popup opens directly below the 3 dots.
    Box {
        IconButton(onClick = { open = true }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.cd_more_actions),
                tint = if (hasOptions) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_note_options)) },
                leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                onClick = { open = false; onOptionsOpenChange(true) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_share_markdown)) },
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = { open = false; onShare(ExportFormat.MARKDOWN) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_share_plain)) },
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = { open = false; onShare(ExportFormat.PLAIN) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_copy_note)) },
                leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                onClick = { open = false; onCopy() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_select_blocks)) },
                leadingIcon = { Icon(Icons.Filled.Checklist, contentDescription = null) },
                onClick = { open = false; onSelectBlocks() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_templates)) },
                leadingIcon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                onClick = { open = false; onInsertTemplate() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_save_template)) },
                leadingIcon = { Icon(Icons.Filled.BookmarkAdd, contentDescription = null) },
                onClick = { open = false; onSaveTemplate() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(if (locked) R.string.action_unlock_note else R.string.action_lock_note)) },
                leadingIcon = { Icon(if (locked) Icons.Filled.LockOpen else Icons.Filled.Lock, contentDescription = null) },
                onClick = { open = false; onToggleLock() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_move_to_trash)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { open = false; onTrash() },
            )
        }
        DropdownMenu(expanded = optionsOpen, onDismissRequest = { onOptionsOpenChange(false) }) {
            NoteOptionsContent(
                folderId = options.folderId,
                tags = options.tags,
                color = options.color,
                folders = options.folders,
                knownTags = options.knownTags,
                onSelectFolder = optionsActions.onSelectFolder,
                onCreateFolder = optionsActions.onCreateFolder,
                onAddTag = optionsActions.onAddTag,
                onRemoveTag = optionsActions.onRemoveTag,
                onSelectColor = optionsActions.onSelectColor,
            )
        }
    }
}
