package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.ui.components.SmallIconButton

/** What the checklist menu can do; the card decides how each action edits the block. */
data class ChecklistMenuState(
    val autoSort: Boolean,
    val hideCompleted: Boolean,
    val canMoveDoneDown: Boolean,
    val canSortByDate: Boolean,
    val hasDone: Boolean,
)

data class ChecklistMenuActions(
    val onAutoSort: (Boolean) -> Unit,
    val onHideCompleted: (Boolean) -> Unit,
    val onMoveDoneDown: () -> Unit,
    val onSortByDate: () -> Unit,
    val onUncheckAll: () -> Unit,
    val onDeleteCompleted: () -> Unit,
)

/** The "more" button on a checklist card with its drop-down of list actions. */
@Composable
fun ChecklistMenu(state: ChecklistMenuState, actions: ChecklistMenuActions) {
    var open by remember { mutableStateOf(false) }
    Box {
        SmallIconButton(
            icon = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.cd_checklist_menu),
            onClick = { open = true },
            size = 28.dp,
            iconSize = 16.dp,
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.checklist_auto_sort)) },
                trailingIcon = { Switch(checked = state.autoSort, onCheckedChange = null) },
                onClick = { actions.onAutoSort(!state.autoSort) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.checklist_hide_completed)) },
                trailingIcon = { Switch(checked = state.hideCompleted, onCheckedChange = null) },
                onClick = { actions.onHideCompleted(!state.hideCompleted) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.cd_sort_checklist)) },
                enabled = state.canMoveDoneDown,
                onClick = { open = false; actions.onMoveDoneDown() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.checklist_sort_due)) },
                enabled = state.canSortByDate,
                onClick = { open = false; actions.onSortByDate() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.checklist_uncheck_all)) },
                enabled = state.hasDone,
                onClick = { open = false; actions.onUncheckAll() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.checklist_delete_completed)) },
                enabled = state.hasDone,
                onClick = { open = false; actions.onDeleteCompleted() },
            )
        }
    }
}
