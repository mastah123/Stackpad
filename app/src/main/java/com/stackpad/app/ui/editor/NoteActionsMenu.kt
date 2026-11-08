package com.stackpad.app.ui.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.domain.export.ExportFormat

/** Overflow menu of the editor top bar: share the note as Markdown or plain text, or trash it. */
@Composable
fun NoteActionsMenu(
    onShare: (ExportFormat) -> Unit,
    onTrash: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_more_actions))
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
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
            text = { Text(stringResource(R.string.action_move_to_trash)) },
            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = { open = false; onTrash() },
        )
    }
}
