package com.stackpad.app.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
    onOpenSettings: () -> Unit,
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var newFolderDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Folder?>(null) }
    var deleteTarget by remember { mutableStateOf<Folder?>(null) }
    var mode by rememberSaveable { mutableStateOf(ListMode.ALL) }
    // Notes picked by long-press. The list is in selection mode while this is not empty.
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val selecting = selectedIds.isNotEmpty()
    // The folder shown right now; new notes start in it, and its rename/delete icons show in the top bar.
    val currentFolder = (ui.filter.folder as? FolderFilter.InFolder)?.let { selected ->
        ui.folders.firstOrNull { it.id == selected.id }
    }
    var folderChoiceDialog by remember { mutableStateOf(false) }
    var addTagDialog by remember { mutableStateOf(false) }
    val searchWords = remember(query) { SearchQuery.words(query) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    fun closeMenu(then: () -> Unit) {
        scope.launch { drawerState.close() }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !selecting,
        drawerContent = {
            NoteMenu(
                mode = mode,
                onAll = { closeMenu { mode = ListMode.ALL; viewModel.showAll() } },
                onPinned = {
                    closeMenu {
                        mode = ListMode.PINNED
                        viewModel.showAll()
                        viewModel.togglePinnedOnly()
                    }
                },
                onFolders = { closeMenu { mode = ListMode.FOLDERS; viewModel.showAll() } },
                onTags = { closeMenu { mode = ListMode.TAGS; viewModel.showAll() } },
                onTrash = { closeMenu(onOpenTrash) },
                onSettings = { closeMenu(onOpenSettings) },
            )
        },
    ) {
        Scaffold(
            topBar = {
                if (selecting) {
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
                        onMoveToFolder = { folderChoiceDialog = true },
                        onAddTag = { addTagDialog = true },
                    )
                } else TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.cd_open_menu))
                        }
                    },
                    title = { Text(sectionTitle(ui, mode), style = MaterialTheme.typography.titleMedium, maxLines = 1) },
                    actions = {
                        currentFolder?.let { folder ->
                            IconButton(onClick = { renameTarget = folder }) {
                                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.folder_rename))
                            }
                            IconButton(onClick = { deleteTarget = folder }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.folder_delete))
                            }
                        }
                        val grid = ui.layout == ListLayout.GRID
                        IconButton(onClick = { viewModel.setLayout(if (grid) ListLayout.LIST else ListLayout.GRID) }) {
                            Icon(
                                imageVector = if (grid) Icons.Filled.ViewAgenda else Icons.Filled.GridView,
                                contentDescription = stringResource(if (grid) R.string.cd_layout_list else R.string.cd_layout_grid),
                            )
                        }
                        SortMenu(current = ui.sort, onSelect = viewModel::setSort)
                        ThemeMenu(current = themeMode, onSelect = viewModel::setThemeMode)
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            floatingActionButton = {
                if (!selecting) {
                    FloatingActionButton(onClick = { onNewNote(currentFolder?.id) }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_new_note))
                    }
                }
            },
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                SearchField(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
                if ((query.isNotBlank() || ui.filter.color != null) && (mode == ListMode.ALL || mode == ListMode.PINNED)) {
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
                when (mode) {
                    ListMode.FOLDERS -> FolderGrid(
                        folders = ui.folders,
                        selected = ui.filter.folder,
                        onSelect = viewModel::selectFolder,
                        onNewFolder = { newFolderDialog = true },
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    ListMode.TAGS -> if (ui.tags.isEmpty()) {
                        Text(
                            text = stringResource(R.string.menu_no_tags),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    } else {
                        TagFilterBar(
                            tags = ui.tags,
                            selected = ui.filter.tag,
                            onToggle = viewModel::toggleTag,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    ListMode.ALL, ListMode.PINNED -> Unit
                }
                val current = ui.notes
                when {
                    current == null -> Box(Modifier.fillMaxSize())
                    current.isEmpty() -> EmptyState(query = query, filtered = !ui.filter.isEmpty)
                    else -> NoteList(
                        notes = current,
                        layout = ui.layout,
                        highlight = searchWords,
                        selecting = selecting,
                        selectedIds = selectedIds,
                        onOpen = { id ->
                            if (selecting) selectedIds = toggled(selectedIds, id) else onOpenNote(id)
                        },
                        onLongPress = { id -> selectedIds = selectedIds + id },
                        onTogglePin = viewModel::togglePin,
                    )
                }
            }
        }

    }

    if (folderChoiceDialog) {
        FolderChoiceDialog(
            folders = ui.folders,
            onChoose = { folderId ->
                viewModel.setFolder(selectedIds, folderId)
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
                viewModel.addTag(selectedIds, it)
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
            filter.tag?.let { "#$it" } ?: stringResource(R.string.menu_tags)
        mode == ListMode.FOLDERS -> when {
            filter.folder == FolderFilter.Unfiled -> stringResource(R.string.folder_none)
            filter.folder is FolderFilter.InFolder ->
                ui.folders.firstOrNull { it.id == filter.folder.id }?.name ?: stringResource(R.string.menu_folders)
            else -> stringResource(R.string.menu_folders)
        }
        else -> stringResource(R.string.section_all_notes)
    }
}

private fun toggled(selected: Set<String>, id: String): Set<String> =
    if (id in selected) selected - id else selected + id

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteList(
    notes: List<NoteSummary>,
    layout: ListLayout,
    highlight: List<String>,
    selecting: Boolean,
    selectedIds: Set<String>,
    onOpen: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onTogglePin: (NoteSummary) -> Unit,
) {
    val pinned = notes.filter { it.pinned }
    val others = notes.filterNot { it.pinned }
    val grid = layout == ListLayout.GRID

    @Composable
    fun card(note: NoteSummary) = NoteCard(
        note = note,
        onClick = { onOpen(note.id) },
        onTogglePin = { onTogglePin(note) },
        highlight = highlight,
        selecting = selecting,
        selected = note.id in selectedIds,
        onLongClick = { onLongPress(note.id) },
        previewLines = if (grid) GRID_PREVIEW_LINES else LIST_PREVIEW_LINES,
        previewMaxHeight = if (grid) GRID_PREVIEW_HEIGHT else LIST_PREVIEW_HEIGHT,
        previewItems = if (grid) GRID_PREVIEW_ITEMS else LIST_PREVIEW_ITEMS,
    )

    if (grid) {
        // Two columns of cards at their natural height; section labels span both.
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp),
            verticalItemSpacing = 6.dp,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (pinned.isNotEmpty()) {
                item(key = "header_pinned", span = StaggeredGridItemSpan.FullLine) {
                    SectionLabel(stringResource(R.string.section_pinned))
                }
                items(pinned, key = { it.id }) { note -> card(note) }
                if (others.isNotEmpty()) {
                    item(key = "header_notes", span = StaggeredGridItemSpan.FullLine) {
                        SectionLabel(stringResource(R.string.section_notes))
                    }
                }
            }
            items(others, key = { it.id }) { note -> card(note) }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (pinned.isNotEmpty()) {
                item(key = "header_pinned") { SectionLabel(stringResource(R.string.section_pinned)) }
                items(pinned, key = { it.id }) { note -> card(note) }
                if (others.isNotEmpty()) {
                    item(key = "header_notes") { SectionLabel(stringResource(R.string.section_notes)) }
                }
            }
            items(others, key = { it.id }) { note -> card(note) }
        }
    }
}

private const val LIST_PREVIEW_LINES = 2
private const val GRID_PREVIEW_LINES = 6
private val LIST_PREVIEW_HEIGHT = 96.dp
private val GRID_PREVIEW_HEIGHT = 200.dp
private const val LIST_PREVIEW_ITEMS = 5
private const val GRID_PREVIEW_ITEMS = 9

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
    )
}

@Composable
private fun EmptyState(query: String, filtered: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = when {
                query.isNotBlank() && filtered -> stringResource(R.string.empty_search_filtered, query)
                query.isNotBlank() -> stringResource(R.string.empty_search, query)
                filtered -> stringResource(R.string.empty_filtered)
                else -> stringResource(R.string.empty_notes)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
