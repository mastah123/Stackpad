package com.stackpad.app.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * Small grid of folder tiles: All, No folder, every folder and "New folder". The selected folder
 * gets an extra "Edit" tile with rename and delete. Scrolls on its own when there are many folders.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderGrid(
    folders: List<Folder>,
    selected: FolderFilter,
    onSelect: (FolderFilter) -> Unit,
    onNewFolder: () -> Unit,
    onRenameFolder: (Folder) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .heightIn(max = 152.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FolderTile(Icons.Filled.Notes, stringResource(R.string.filter_all), selected == FolderFilter.All) {
            onSelect(FolderFilter.All)
        }
        FolderTile(Icons.Filled.FolderOff, stringResource(R.string.folder_none), selected == FolderFilter.Unfiled) {
            onSelect(FolderFilter.Unfiled)
        }
        folders.forEach { folder ->
            FolderTile(
                Icons.Filled.Folder,
                folder.name,
                (selected as? FolderFilter.InFolder)?.id == folder.id,
            ) { onSelect(FolderFilter.InFolder(folder.id)) }
        }
        val current = folders.firstOrNull { (selected as? FolderFilter.InFolder)?.id == it.id }
        if (current != null) {
            FolderEditTile({ onRenameFolder(current) }, { onDeleteFolder(current) })
        }
        FolderTile(Icons.Filled.Add, stringResource(R.string.folder_new), false, onClick = onNewFolder)
    }
}

@Composable
private fun FolderTile(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceVariant,
        modifier = Modifier
            .width(TILE_WIDTH)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private val TILE_WIDTH = 104.dp

/** Tile that opens the rename / delete menu of the selected folder. */
@Composable
private fun FolderEditTile(onRename: () -> Unit, onDelete: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        FolderTile(Icons.Filled.Edit, stringResource(R.string.cd_folder_options), false) { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.folder_rename)) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = { open = false; onRename() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.folder_delete)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { open = false; onDelete() },
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
