package com.stackpad.app.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.data.model.Folder
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.prefs.ListLayout
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.FolderNames
import com.stackpad.app.domain.notes.Tags
import com.stackpad.app.domain.notes.TrashPolicy
import com.stackpad.app.domain.search.SearchQuery
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.TextInputDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onOpenNote: (String) -> Unit,
    /** Called with the folder selected in the list (or null), so a new note starts in it. */
    onNewNote: (folderId: String?) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val counts by viewModel.menuCounts.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var newFolderDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Folder?>(null) }
    var deleteTarget by remember { mutableStateOf<Folder?>(null) }
    var mode by rememberSaveable { mutableStateOf(ListMode.ALL) }
    var menuTab by rememberSaveable { mutableStateOf(MenuTab.HOME) }
    // Notes picked by long-press. The list is in selection mode while this is not empty.
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val selecting = selectedIds.isNotEmpty()
    // The folder shown right now; new notes start in it, and its rename/delete icons show in the top bar.
    val currentFolder = (ui.filter.folder as? FolderFilter.InFolder)?.let { selected ->
        ui.folders.firstOrNull { it.id == selected.id }
    }
    var folderChoiceDialog by remember { mutableStateOf(false) }
    var addTagDialog by remember { mutableStateOf(false) }
    // Notes the folder/tag dialogs act on: the selection, or the one note whose three-dot menu was used.
    var actionIds by remember { mutableStateOf(emptySet<String>()) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    val home = mode == ListMode.ALL || mode == ListMode.PINNED
    val searchWords = remember(query) { SearchQuery.words(query) }
    val menu = rememberPushMenuState()
    val scope = rememberCoroutineScope()
    fun closeMenu(then: () -> Unit) {
        scope.launch { menu.close() }
        then()
    }

    // After a note is moved to the trash, offer to undo it.
    LaunchedEffect(viewModel) {
        viewModel.trashed.collect { id ->
            val result = snackbar.showSnackbar(
                message = context.getString(R.string.msg_moved_to_trash),
                actionLabel = context.getString(R.string.action_undo),
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restore(id)
        }
    }

    // Several notes trashed together: one snackbar, one Undo for all of them.
    LaunchedEffect(viewModel) {
        viewModel.trashedMany.collect { ids ->
            val result = snackbar.showSnackbar(
                message = context.resources.getQuantityString(R.plurals.msg_notes_moved_to_trash, ids.size, ids.size),
                actionLabel = context.getString(R.string.action_undo),
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restore(ids)
        }
    }

    // Drop selected notes that disappeared (trashed, or filtered out).
    LaunchedEffect(ui.notes) {
        val visible = ui.notes?.map { it.id }?.toSet() ?: return@LaunchedEffect
        if (selectedIds.any { it !in visible }) selectedIds = selectedIds.filter { it in visible }.toSet()
    }
    BackHandler(enabled = selecting) { selectedIds = emptySet() }

    PushMenuLayout(
        state = menu,
        gesturesEnabled = !selecting,
        menu = {
            NoteSideMenu(
                ui = ui,
                counts = counts,
                mode = mode,
                themeMode = themeMode,
                tab = menuTab,
                onTab = { menuTab = it },
                onAll = { closeMenu { mode = ListMode.ALL; viewModel.showAll() } },
                onPinned = {
                    closeMenu {
                        mode = ListMode.PINNED
                        viewModel.showAll()
                        viewModel.togglePinnedOnly()
                    }
                },
                onReminders = { closeMenu(onOpenReminders) },
                onTrash = { closeMenu(onOpenTrash) },
                onSettings = { closeMenu(onOpenSettings) },
                onFolder = { folder ->
                    closeMenu {
                        mode = ListMode.FOLDERS
                        viewModel.showAll()
                        viewModel.selectFolder(folder)
                    }
                },
                onNewFolder = { newFolderDialog = true },
                onTag = { tag ->
                    closeMenu {
                        mode = ListMode.TAGS
                        viewModel.showAll()
                        viewModel.setTagFilter(tag)
                    }
                },
                onTheme = viewModel::setThemeMode,
            )
        },
    ) {
        Scaffold(
            topBar = {
                val barKind = when {
                    selecting -> 1
                    !home -> 2
                    else -> 0
                }
                Crossfade(targetState = barKind, animationSpec = tween(180), label = "listTopBar") { kind ->
                    when (kind) {
                        1 -> {
                            val chosen = ui.notes.orEmpty().filter { it.id in selectedIds }
                            NoteSelectionBar(
                                count = selectedIds.size,
                                allPinned = chosen.isNotEmpty() && chosen.all { it.pinned },
                                onClose = { selectedIds = emptySet() },
                                onSelectAll = { selectedIds = ui.notes.orEmpty().map { it.id }.toSet() },
                                onTogglePin = {
                                    viewModel.togglePinned(selectedIds)
                                    selectedIds = emptySet()
                                },
                                onTrash = {
                                    viewModel.moveToTrash(selectedIds)
                                    selectedIds = emptySet()
                                },
                                onMoveToFolder = { actionIds = selectedIds; folderChoiceDialog = true },
                                onAddTag = { actionIds = selectedIds; addTagDialog = true },
                            )
                        }
                        2 -> NoteListTopBar(
                            title = sectionTitle(ui, mode),
                            layout = ui.layout,
                            sort = ui.sort,
                            onOpenMenu = { scope.launch { menu.open() } },
                            onSearch = { searchOpen = !searchOpen },
                            onLayout = viewModel::setLayout,
                            onSort = viewModel::setSort,
                            onRename = currentFolder?.let { folder -> { renameTarget = folder } },
                            onDelete = currentFolder?.let { folder -> { deleteTarget = folder } },
                        )
                        else -> Unit
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !selecting,
                    enter = scaleIn(tween(180)) + fadeIn(tween(180)),
                    exit = scaleOut(tween(150)) + fadeOut(tween(150)),
                ) {
                    FloatingActionButton(onClick = { onNewNote(currentFolder?.id) }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_new_note))
                    }
                }
            },
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (home) {
                    HomeSearchBar(
                        query = query,
                        onQueryChange = viewModel::setQuery,
                        onOpenMenu = { scope.launch { menu.open() } },
                        modifier = Modifier
                            .then(if (selecting) Modifier else Modifier.statusBarsPadding())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                } else {
                    AnimatedVisibility(visible = searchOpen || query.isNotBlank()) {
                        SearchField(
                            query = query,
                            onQueryChange = viewModel::setQuery,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
                if ((query.isNotBlank() || ui.filter.color != null) && home) {
                    SearchFilterRow(
                        folders = ui.folders,
                        tags = ui.tags,
                        filter = ui.filter,
                        onFolder = viewModel::selectFolder,
                        onTag = viewModel::setTagFilter,
                        onColor = viewModel::setColorFilter,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                val current = ui.notes
                val actionsFor: (NoteSummary) -> NoteCardActions = { note ->
                    NoteCardActions(
                        onTogglePin = { viewModel.togglePin(note) },
                        onMoveToFolder = { actionIds = setOf(note.id); folderChoiceDialog = true },
                        onAddTag = { actionIds = setOf(note.id); addTagDialog = true },
                        onTrash = { viewModel.moveToTrash(note.id) },
                    )
                }
                val listIcons: @Composable () -> Unit = {
                    LayoutToggleButton(layout = ui.layout, onChange = viewModel::setLayout)
                    SortMenu(current = ui.sort, onSelect = viewModel::setSort)
                }
                Crossfade(
                    targetState = when {
                        current == null -> 0
                        current.isEmpty() -> 1
                        else -> 2
                    },
                    animationSpec = tween(200),
                    label = "listState",
                ) { state ->
                    when {
                        state == 0 || current == null -> Box(Modifier.fillMaxSize())
                        state == 1 -> NoteListEmpty(
                            query = query,
                            filtered = !ui.filter.isEmpty,
                            onCreate = { onNewNote(currentFolder?.id) },
                        )
                        else -> NoteList(
                            notes = current,
                            layout = ui.layout,
                            highlight = searchWords,
                            selecting = selecting,
                            selectedIds = selectedIds,
                            alwaysShowHeaders = home,
                            onOpen = { id ->
                                if (selecting) selectedIds = toggled(selectedIds, id) else onOpenNote(id)
                            },
                            onLongPress = { id -> selectedIds = selectedIds + id },
                            actionsFor = actionsFor,
                            headerActions = if (home) listIcons else null,
                        )
                    }
                }
            }
        }

    }

    if (folderChoiceDialog) {
        FolderChoiceDialog(
            folders = ui.folders,
            onChoose = { folderId ->
                viewModel.setFolder(actionIds, folderId)
                folderChoiceDialog = false
                selectedIds = emptySet()
            },
            onDismiss = { folderChoiceDialog = false },
        )
    }

    if (addTagDialog) {
        TextInputDialog(
            title = stringResource(R.string.dialog_add_tag_title),
            placeholder = stringResource(R.string.tag_hint),
            confirmLabel = stringResource(R.string.action_create),
            maxLength = Tags.MAX_LENGTH,
            onConfirm = {
                viewModel.addTag(actionIds, it)
                addTagDialog = false
                selectedIds = emptySet()
            },
            onDismiss = { addTagDialog = false },
        )
    }

    if (newFolderDialog) {
        TextInputDialog(
            title = stringResource(R.string.folder_new),
            placeholder = stringResource(R.string.folder_name_hint),
            confirmLabel = stringResource(R.string.action_create),
            maxLength = FolderNames.MAX_LENGTH,
            onConfirm = {
                newFolderDialog = false
                viewModel.createFolder(it)
            },
            onDismiss = { newFolderDialog = false },
        )
    }

    renameTarget?.let { folder ->
        TextInputDialog(
            title = stringResource(R.string.folder_rename),
            initialText = folder.name,
            placeholder = stringResource(R.string.folder_name_hint),
            confirmLabel = stringResource(R.string.action_save),
            maxLength = FolderNames.MAX_LENGTH,
            onConfirm = {
                renameTarget = null
                viewModel.renameFolder(folder.id, it)
            },
            onDismiss = { renameTarget = null },
        )
    }

    deleteTarget?.let { folder ->
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_folder_title, folder.name),
            message = stringResource(R.string.dialog_delete_folder_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteFolder(folder.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

/** Title for the top bar: what the list is currently showing. */
@Composable
private fun sectionTitle(ui: NoteListUiState, mode: ListMode): String {
    val filter = ui.filter
    return when {
        mode == ListMode.PINNED -> stringResource(R.string.menu_pinned)
        mode == ListMode.TAGS ->
            filter.tag?.let { "#$it" } ?: stringResource(R.string.section_all_notes)
        mode == ListMode.FOLDERS -> when {
            filter.folder == FolderFilter.Unfiled -> stringResource(R.string.folder_none)
            filter.folder is FolderFilter.InFolder ->
                ui.folders.firstOrNull { it.id == filter.folder.id }?.name ?: stringResource(R.string.section_all_notes)
            else -> stringResource(R.string.section_all_notes)
        }
        else -> stringResource(R.string.section_all_notes)
    }
}

private fun toggled(selected: Set<String>, id: String): Set<String> =
    if (id in selected) selected - id else selected + id
