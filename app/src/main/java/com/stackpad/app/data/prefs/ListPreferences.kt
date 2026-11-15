package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpad.app.domain.notes.NoteSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** How the note list is drawn. */
enum class ListLayout { LIST, GRID }

/** Persists how the note list is sorted (default last edited) and laid out (default list). */
class ListPreferences(private val context: Context) {
    private val key = stringPreferencesKey("note_sort")

    val sort: Flow<NoteSort> = context.settingsDataStore.data.map { prefs ->
        prefs[key]
            ?.let { stored -> NoteSort.values().firstOrNull { it.name == stored } }
            ?: NoteSort.LAST_EDITED
    }

    private val layoutKey = stringPreferencesKey("note_layout")

    val layout: Flow<ListLayout> = context.settingsDataStore.data.map { prefs ->
        prefs[layoutKey]
            ?.let { stored -> ListLayout.values().firstOrNull { it.name == stored } }
            ?: ListLayout.LIST
    }

    suspend fun setLayout(layout: ListLayout) {
        context.settingsDataStore.edit { it[layoutKey] = layout.name }
    }

    suspend fun setSort(sort: NoteSort) {
        context.settingsDataStore.edit { it[key] = sort.name }
    }
}
