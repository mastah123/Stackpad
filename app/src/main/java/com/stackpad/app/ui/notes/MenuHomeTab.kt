package com.stackpad.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.notes.MenuCounts

/** Home tab: All notes, Pinned, Reminders and Trash, each with its note count. */
@Composable
fun MenuHomeTab(
    mode: ListMode,
    counts: MenuCounts,
    onAll: () -> Unit,
    onPinned: () -> Unit,
    onReminders: () -> Unit,
    onTrash: () -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MenuRow(Icons.Filled.Notes, stringResource(R.string.section_all_notes), mode == ListMode.ALL, onAll, counts.all)
        MenuRow(Icons.Filled.PushPin, stringResource(R.string.menu_pinned), mode == ListMode.PINNED, onPinned, counts.pinned)
        MenuRow(Icons.Filled.Notifications, stringResource(R.string.menu_reminders), false, onReminders, counts.reminders)
        MenuRow(Icons.Filled.Delete, stringResource(R.string.menu_trash), false, onTrash, counts.trashed)
    }
}
