package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.data.prefs.ListLayout
import com.stackpad.app.domain.notes.ListOrder

/**
 * Top bar for folder and tag views: menu button, title, then Search, List/Grid and Sort icons.
 * For a folder ([onRename] and [onDelete] set) a three-dot menu adds Rename and Delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListTopBar(
    title: String,
    layout: ListLayout,
    order: ListOrder,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onLayout: (ListLayout) -> Unit,
    onOrder: (ListOrder) -> Unit,
    onRename: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onOpenMenu) {
                Icon(AppIcons.Menu, contentDescription = stringResource(R.string.cd_open_menu))
            }
        },
        title = { Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1) },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(AppIcons.Search, contentDescription = stringResource(R.string.cd_search))
            }
            LayoutToggleButton(layout = layout, onChange = onLayout)
            SortButton(order = order, onChange = onOrder)
            if (onRename != null && onDelete != null) {
                var open by remember { mutableStateOf(false) }
                IconButton(onClick = { open = true }) {
                    Icon(AppIcons.MoreVert, contentDescription = stringResource(R.string.cd_more_actions))
                }
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.folder_rename)) },
                        onClick = { open = false; onRename() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.folder_delete)) },
                        onClick = { open = false; onDelete() },
                    )
                }
            }
        },
    )
}
