package com.stackpad.app.domain.notes

import com.stackpad.app.data.model.NoteSummary

/** Filters and orders the note list. Pure logic, so it is unit tested. */
object NoteOrganizer {

    fun organize(notes: List<NoteSummary>, sort: NoteSort, filter: NoteFilter, reversed: Boolean = false): List<NoteSummary> {
        val visible = notes.filter { matches(it, filter) }
        val field: Comparator<NoteSummary> = when (sort) {
            NoteSort.LAST_EDITED -> compareByDescending { it.updatedAt }
            NoteSort.CREATED -> compareByDescending { it.createdAt }
            // Ties fall back to the newest edit.
            NoteSort.TITLE -> compareBy<NoteSummary, String>(String.CASE_INSENSITIVE_ORDER) { it.title.trim() }
                .thenByDescending { it.updatedAt }
        }
        // Pinned notes stay on top and untitled notes stay last, whichever way the list is turned.
        val comparator = compareByDescending<NoteSummary> { it.pinned }
            .thenBy { sort == NoteSort.TITLE && it.title.isBlank() }
            .then(if (reversed) field.reversed() else field)
        return visible.sortedWith(comparator)
    }

    fun matches(note: NoteSummary, filter: NoteFilter): Boolean {
        val folderOk = when (val folder = filter.folder) {
            FolderFilter.All -> true
            FolderFilter.Unfiled -> note.folderId == null
            is FolderFilter.InFolder -> note.folderId == folder.id
        }
        return folderOk &&
            (filter.tag == null || filter.tag in note.tags) &&
            (!filter.pinnedOnly || note.pinned) &&
            (filter.color == null || note.color == filter.color)
    }
}
