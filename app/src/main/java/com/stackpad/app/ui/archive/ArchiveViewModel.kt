package com.stackpad.app.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stackpad.app.AppContainer
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.model.NoteSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchiveViewModel(private val repository: NoteRepository) : ViewModel() {

    /** Null while the first result is loading. */
    val notes: StateFlow<List<NoteSummary>?> = repository.observeArchived()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun unarchive(id: String) {
        viewModelScope.launch { repository.setArchived(id, false) }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ArchiveViewModel(container.repository) }
        }
    }
}
