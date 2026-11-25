package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.ui.components.SmallIconButton

/** What the three-dot menu on a note can do. */
class NoteCardActions(
    val onTogglePin: () -> Unit,
    val onMoveToFolder: () -> Unit,
    val onAddTag: () -> Unit,
    val onTrash: () -> Unit,
)

/** Three-dot button on a note with Pin, Move to folder, Add tag and Move to trash. */
@Composable
fun NoteCardMenu(pinned: Boolean, actions: NoteCardActions) {
    var open by remember { mutableStateOf(false) }
    Box {
        SmallIconButton(
            icon = AppIcons.MoreVert,
            contentDescription = stringResource(R.string.cd_more_actions),
            onClick = { open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(if (pinned) R.string.cd_unpin else R.string.cd_pin)) },
                onClick = { open = false; actions.onTogglePin() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_move_to_folder)) },
                onClick = { open = false; actions.onMoveToFolder() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_add_tag_to_notes)) },
                onClick = { open = false; actions.onAddTag() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_move_to_trash)) },
                onClick = { open = false; actions.onTrash() },
            )
        }
    }
}
