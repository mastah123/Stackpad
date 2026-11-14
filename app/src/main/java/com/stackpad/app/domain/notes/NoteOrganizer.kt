package com.stackpad.app.domain.notes

import com.stackpad.app.data.model.NoteSummary

/** Filters and orders the note list. Pure logic, so it is unit tested. */
object NoteOrganizer {

    fun organize(notes: List<NoteSummary>, sort: NoteSort, filter: NoteFilter): List<NoteSummary> {
        val visible = notes.filter { matches(it, filter) }
        val byPinned = compareByDescending<NoteSummary> { it.pinned }
        val comparator = when (sort) {
            NoteSort.LAST_EDITED -> byPinned.thenByDescending { it.updatedAt }
            NoteSort.CREATED -> byPinned.thenByDescending { it.createdAt }
            // Untitled notes go last; ties fall back to the newest edit.
            NoteSort.TITLE -> byPinned
                .thenBy { it.title.isBlank() }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title.trim() }
                .thenByDescending { it.updatedAt }
        }
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
