package com.stackpad.app.ui.trash

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

class TrashViewModel(private val repository: NoteRepository) : ViewModel() {

    /** Null while the first result is loading. */
    val notes: StateFlow<List<NoteSummary>?> = repository.observeTrashed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun restore(id: String) {
        viewModelScope.launch { repository.restore(id) }
    }

    fun deleteForever(id: String) {
        viewModelScope.launch { repository.deleteForever(id) }
    }

    fun emptyTrash() {
        viewModelScope.launch { repository.emptyTrash() }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(container: AppContainer) = viewModelFactory {
            initializer { TrashViewModel(container.repository) }
        }
    }
}
