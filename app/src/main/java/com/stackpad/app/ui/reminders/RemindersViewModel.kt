package com.stackpad.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stackpad.app.AppContainer
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.domain.reminders.ReminderEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Pending checklist reminders from every note, earliest first. Null while loading. */
class RemindersViewModel(repository: NoteRepository) : ViewModel() {
    val reminders: StateFlow<List<ReminderEntry>?> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { RemindersViewModel(container.repository) }
        }
    }
}
