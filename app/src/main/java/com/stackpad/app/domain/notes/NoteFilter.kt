package com.stackpad.app.domain.notes

/** Which folder the note list shows. */
sealed interface FolderFilter {
    data object All : FolderFilter
    data object Unfiled : FolderFilter
    data class InFolder(val id: String) : FolderFilter
}

/** Folder and tag restrictions for the note list. Both must match when both are set. */
data class NoteFilter(
    val folder: FolderFilter = FolderFilter.All,
    val tag: String? = null,
)
