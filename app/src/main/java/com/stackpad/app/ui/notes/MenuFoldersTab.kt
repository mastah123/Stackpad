package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.domain.notes.FolderFilter

/** Folders tab: "No folder" and every folder. [selected] is the folder the note list shows, if any. */
@Composable
fun MenuFoldersTab(
    folders: List<Folder>,
    selected: FolderFilter,
    onSelect: (FolderFilter) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.menu_folders),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
            )
        }
        item {
            MenuRow(
                AppIcons.FolderOff,
                stringResource(R.string.folder_none),
                selected == FolderFilter.Unfiled,
                { onSelect(FolderFilter.Unfiled) },
            )
        }
        items(folders, key = { it.id }) { folder ->
            MenuRow(
                AppIcons.Folder,
                folder.name,
                (selected as? FolderFilter.InFolder)?.id == folder.id,
                { onSelect(FolderFilter.InFolder(folder.id)) },
            )
        }
        if (folders.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.menu_no_folders),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
