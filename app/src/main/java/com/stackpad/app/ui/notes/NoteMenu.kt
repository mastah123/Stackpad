package com.stackpad.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.NoteFilter
import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.ui.components.StackpadLogo

/**
 * Thin side menu opened from the top-left button: the logo, then icon shortcuts for All notes,
 * Pinned, Folders, Tags and Trash. Folders and Tags open a picker.
 */
@Composable
fun NoteMenu(
    filter: NoteFilter,
    onAll: () -> Unit,
    onPinned: () -> Unit,
    onFolders: () -> Unit,
    onTags: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit,
) {
    // A plain Surface instead of ModalDrawerSheet, which cannot be narrower than 240dp.
    Surface(
        modifier = Modifier.width(MENU_WIDTH).fillMaxHeight(),
        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight().navigationBarsPadding(),
            containerColor = Color.Transparent,
            header = {
                StackpadLogo(
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    size = 34.dp,
                )
            },
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MenuItem(Icons.Filled.Notes, R.string.menu_all, filter.isEmpty, onAll)
                MenuItem(Icons.Filled.PushPin, R.string.menu_pinned, filter.pinnedOnly, onPinned)
                MenuItem(Icons.Filled.Folder, R.string.menu_folders, filter.folder != FolderFilter.All, onFolders)
                MenuItem(Icons.Filled.Sell, R.string.menu_tags, filter.tag != null, onTags)
                MenuItem(Icons.Filled.Delete, R.string.menu_trash, false, onTrash)
                MenuItem(Icons.Filled.Settings, R.string.menu_settings, false, onSettings)
            }
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, label: Int, selected: Boolean, onClick: () -> Unit) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(stringResource(label), style = MaterialTheme.typography.labelSmall, maxLines = 1) },
        alwaysShowLabel = true,
    )
}

/** Pick which folder to show, or create a new one. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FolderPickerSheet(
    folders: List<Folder>,
    selected: FolderFilter,
    onSelect: (FolderFilter) -> Unit,
    onNewFolder: () -> Unit,
    onDismiss: () -> Unit,
) {
    PickerSheet(title = stringResource(R.string.menu_folders), onDismiss = onDismiss) {
        FilterChip(
            selected = selected == FolderFilter.All,
            onClick = { onSelect(FolderFilter.All) },
            label = { Text(stringResource(R.string.filter_all)) },
        )
        FilterChip(
            selected = selected == FolderFilter.Unfiled,
            onClick = { onSelect(FolderFilter.Unfiled) },
            label = { Text(stringResource(R.string.folder_none)) },
        )
        folders.forEach { folder ->
            FilterChip(
                selected = (selected as? FolderFilter.InFolder)?.id == folder.id,
                onClick = { onSelect(FolderFilter.InFolder(folder.id)) },
                label = { Text(folder.name) },
                leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) },
            )
        }
        AssistChip(
            onClick = onNewFolder,
            label = { Text(stringResource(R.string.folder_new)) },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        )
    }
}

/** Pick a tag to filter by. Choosing the active tag again clears it. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagPickerSheet(
    tags: List<TagCount>,
    selected: String?,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerSheet(title = stringResource(R.string.menu_tags), onDismiss = onDismiss) {
        if (tags.isEmpty()) {
            Text(
                text = stringResource(R.string.menu_no_tags),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        tags.forEach { entry ->
            FilterChip(
                selected = entry.tag == selected,
                onClick = { onToggle(entry.tag) },
                label = { Text("#${entry.tag} · ${entry.count}") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PickerSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) { content() }
        }
    }
}

private val MENU_WIDTH = 88.dp
