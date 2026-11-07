package com.stackpad.app.ui.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.ui.components.SmallIconButton

/** Scrollable chips to switch between All notes, Unfiled, and each folder, plus "new folder". */
@Composable
fun FolderFilterBar(
    folders: List<Folder>,
    selected: FolderFilter,
    onSelect: (FolderFilter) -> Unit,
    onNewFolder: () -> Unit,
    onRenameFolder: (Folder) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            val isSelected = (selected as? FolderFilter.InFolder)?.id == folder.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(FolderFilter.InFolder(folder.id)) },
                label = { Text(folder.name) },
                leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) },
            )
            if (isSelected) FolderMenu({ onRenameFolder(folder) }, { onDeleteFolder(folder) })
        }
        AssistChip(
            onClick = onNewFolder,
            label = { Text(stringResource(R.string.folder_new)) },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        )
    }
}

@Composable
private fun FolderMenu(onRename: () -> Unit, onDelete: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        SmallIconButton(
            icon = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.cd_folder_options),
            onClick = { open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.folder_rename)) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    open = false
                    onRename()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.folder_delete)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = {
                    open = false
                    onDelete()
                },
            )
        }
    }
}

/** Scrollable chips for filtering by tag. Tap the selected tag again to clear it. */
@Composable
fun TagFilterBar(
    tags: List<TagCount>,
    selected: String?,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tags.forEach { entry ->
            FilterChip(
                selected = entry.tag == selected,
                onClick = { onToggle(entry.tag) },
                label = { Text("#${entry.tag} · ${entry.count}") },
            )
        }
    }
}
