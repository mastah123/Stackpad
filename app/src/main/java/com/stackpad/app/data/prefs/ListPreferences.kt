package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpad.app.domain.notes.NoteSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists how the note list is sorted. Defaults to last edited. */
class ListPreferences(private val context: Context) {
    private val key = stringPreferencesKey("note_sort")

    val sort: Flow<NoteSort> = context.settingsDataStore.data.map { prefs ->
        prefs[key]
            ?.let { stored -> NoteSort.values().firstOrNull { it.name == stored } }
            ?: NoteSort.LAST_EDITED
    }

    suspend fun setSort(sort: NoteSort) {
        context.settingsDataStore.edit { it[key] = sort.name }
    }
}
