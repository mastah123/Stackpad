package com.stackpad.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stackpad.app.AppContainer
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.model.Folder
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.prefs.ListPreferences
import com.stackpad.app.data.prefs.ThemeMode
import com.stackpad.app.data.prefs.ThemePreferences
import com.stackpad.app.domain.notes.FolderFilter
import com.stackpad.app.domain.notes.NoteFilter
import com.stackpad.app.domain.notes.NoteOrganizer
import com.stackpad.app.domain.notes.NoteSort
import com.stackpad.app.domain.notes.TagCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the note list screen shows. [notes] is null while the first result loads. */
data class NoteListUiState(
    val notes: List<NoteSummary>? = null,
    val folders: List<Folder> = emptyList(),
    val tags: List<TagCount> = emptyList(),
    val sort: NoteSort = NoteSort.LAST_EDITED,
    val filter: NoteFilter = NoteFilter(),
)

class NoteListViewModel(
    private val repository: NoteRepository,
    private val themePreferences: ThemePreferences,
    private val listPreferences: ListPreferences,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(NoteFilter())

    /** Emits the id of a note that was just moved to the trash, so the screen can offer Undo. */
    private val _trashed = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val trashed: SharedFlow<String> = _trashed.asSharedFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val summaries = _query
        .debounce(SEARCH_DEBOUNCE_MS)
        .flatMapLatest { repository.observeSummaries(it) }

    val state: StateFlow<NoteListUiState> = combine(
        summaries,
        repository.observeFolders(),
        repository.observeTagCounts(),
        listPreferences.sort,
        _filter,
    ) { notes, folders, tags, sort, filter ->
        // A filter pointing at a deleted folder or an unused tag quietly falls back to "all".
        val folder = filter.folder
        val folderOk = folder !is FolderFilter.InFolder || folders.any { it.id == folder.id }
        val tagOk = filter.tag == null || tags.any { it.tag == filter.tag }
        val effective = NoteFilter(
            folder = if (folderOk) folder else FolderFilter.All,
            tag = if (tagOk) filter.tag else null,
        )
        NoteListUiState(
            notes = NoteOrganizer.organize(notes, sort, effective),
            folders = folders,
            tags = tags,
            sort = sort,
            filter = effective,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), NoteListUiState())

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ThemeMode.SYSTEM)

    fun setQuery(value: String) {
        _query.value = value
    }

    fun togglePin(note: NoteSummary) {
        viewModelScope.launch { repository.setPinned(note.id, !note.pinned) }
    }

    fun moveToTrash(id: String) {
        viewModelScope.launch {
            repository.moveToTrash(id)
            _trashed.emit(id)
        }
    }

    fun restore(id: String) {
        viewModelScope.launch { repository.restore(id) }
    }

    fun setSort(sort: NoteSort) {
        viewModelScope.launch { listPreferences.setSort(sort) }
    }

    fun selectFolder(folder: FolderFilter) {
        _filter.value = _filter.value.copy(folder = folder)
    }

    /** Selecting the active tag again clears it. */
    fun toggleTag(tag: String) {
        val current = _filter.value
        _filter.value = current.copy(tag = if (current.tag == tag) null else tag)
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)?.let { selectFolder(FolderFilter.InFolder(it.id)) }
        }
    }

    fun renameFolder(id: String, name: String) {
        viewModelScope.launch { repository.renameFolder(id, name) }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            repository.deleteFolder(id)
            if ((_filter.value.folder as? FolderFilter.InFolder)?.id == id) selectFolder(FolderFilter.All)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePreferences.setThemeMode(mode) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 150L
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                NoteListViewModel(container.repository, container.themePreferences, container.listPreferences)
            }
        }
    }
}
