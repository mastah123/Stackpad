package com.stackpad.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.NoteFilter
import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.ui.theme.NoteColors

/** Chips under the search box to narrow results by folder, tag and color. */
@Composable
fun SearchFilterRow(
    folders: List<Folder>,
    tags: List<TagCount>,
    filter: NoteFilter,
    onFolder: (FolderFilter) -> Unit,
    onTag: (String?) -> Unit,
    onColor: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorNames = stringArrayResource(R.array.note_color_names)
    val folder = filter.folder
    val folderLabel = when (folder) {
        FolderFilter.All -> stringResource(R.string.filter_folder)
        FolderFilter.Unfiled -> stringResource(R.string.folder_none)
        is FolderFilter.InFolder -> folders.firstOrNull { it.id == folder.id }?.name ?: stringResource(R.string.filter_folder)
    }
    val anySet = folder != FolderFilter.All || filter.tag != null || filter.color != null

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterMenuChip(label = folderLabel, selected = folder != FolderFilter.All) { close ->
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_any)) }, onClick = { close(); onFolder(FolderFilter.All) })
            DropdownMenuItem(text = { Text(stringResource(R.string.folder_none)) }, onClick = { close(); onFolder(FolderFilter.Unfiled) })
            folders.forEach { f ->
                DropdownMenuItem(text = { Text(f.name) }, onClick = { close(); onFolder(FolderFilter.InFolder(f.id)) })
            }
        }
        FilterMenuChip(
            label = filter.tag?.let { "#$it" } ?: stringResource(R.string.filter_tag),
            selected = filter.tag != null,
        ) { close ->
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_any)) }, onClick = { close(); onTag(null) })
            tags.forEach { t ->
                DropdownMenuItem(text = { Text("#${t.tag} · ${t.count}") }, onClick = { close(); onTag(t.tag) })
            }
        }
        FilterMenuChip(
            label = filter.color?.let { colorNames.getOrNull(it) } ?: stringResource(R.string.filter_color),
            selected = filter.color != null,
        ) { close ->
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_any)) }, onClick = { close(); onColor(null) })
            for (index in 1 until NoteColors.count) {
                DropdownMenuItem(
                    text = { Text(colorNames.getOrElse(index) { "" }) },
                    leadingIcon = {
                        val accent = NoteColors.accent(index)
                        if (accent != null) Box(Modifier.size(16.dp).clip(CircleShape).background(accent))
                    },
                    onClick = { close(); onColor(index) },
                )
            }
        }
        if (anySet) {
            AssistChip(
                onClick = {
                    onFolder(FolderFilter.All)
                    onTag(null)
                    onColor(null)
                },
                label = { Text(stringResource(R.string.filter_clear), color = MaterialTheme.colorScheme.primary) },
            )
        }
    }
}

/** A filter chip that opens a dropdown. [menu] receives a function that closes the dropdown. */
@Composable
private fun FilterMenuChip(label: String, selected: Boolean, menu: @Composable (close: () -> Unit) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        FilterChip(selected = selected, onClick = { open = true }, label = { Text(label, maxLines = 1) })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            menu { open = false }
        }
    }
}
