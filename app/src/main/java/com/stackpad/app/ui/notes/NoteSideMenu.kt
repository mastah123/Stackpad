package com.stackpad.app.ui.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.data.prefs.ThemeMode
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.MenuCounts
import com.stackpad.app.domain.notes.TagList
import com.stackpad.app.domain.notes.TagSort

/** The side menu with its three tabs wired to the note list. Tapping an entry also closes the menu (caller). */
@Composable
fun NoteSideMenu(
    ui: NoteListUiState,
    counts: MenuCounts,
    mode: ListMode,
    themeMode: ThemeMode,
    tab: MenuTab,
    onTab: (MenuTab) -> Unit,
    onAll: () -> Unit,
    onPinned: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit,
    onFolder: (FolderFilter) -> Unit,
    onNewFolder: () -> Unit,
    onTag: (String) -> Unit,
    onTheme: (ThemeMode) -> Unit,
) {
    var tagFilter by rememberSaveable { mutableStateOf("") }
    var tagSort by rememberSaveable { mutableStateOf(TagSort.NAME) }
    NoteMenu(
        tab = tab,
        onTab = onTab,
        onSettings = onSettings,
        trailing = {
            when (tab) {
                MenuTab.HOME -> ThemeMenu(current = themeMode, onSelect = onTheme, icon = Icons.Filled.DarkMode)
                MenuTab.FOLDERS -> IconButton(onClick = onNewFolder) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.folder_new))
                }
                MenuTab.TAGS -> TagSortMenu(current = tagSort, onSelect = { tagSort = it })
            }
        },
    ) {
        when (tab) {
            MenuTab.HOME -> MenuHomeTab(mode, counts, onAll, onPinned, onTrash)
            MenuTab.FOLDERS -> MenuFoldersTab(
                folders = ui.folders,
                selected = if (mode == ListMode.FOLDERS) ui.filter.folder else FolderFilter.All,
                onSelect = onFolder,
            )
            MenuTab.TAGS -> MenuTagsTab(
                tags = TagList.visible(ui.tags, tagFilter, tagSort),
                hasAnyTags = ui.tags.isNotEmpty(),
                filterText = tagFilter,
                onFilterText = { tagFilter = it },
                selectedTag = if (mode == ListMode.TAGS) ui.filter.tag else null,
                onSelect = onTag,
            )
        }
    }
}
