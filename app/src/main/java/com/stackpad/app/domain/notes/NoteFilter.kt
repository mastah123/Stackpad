package com.stackpad.app.domain.notes

/** Which folder the note list shows. */
sealed interface FolderFilter {
    data object All : FolderFilter
    data object Unfiled : FolderFilter
    data class InFolder(val id: String) : FolderFilter
}

/** Folder, tag and pinned restrictions for the note list. All that are set must match. */
data class NoteFilter(
    val folder: FolderFilter = FolderFilter.All,
    val tag: String? = null,
    val pinnedOnly: Boolean = false,
) {
    /** True when nothing is restricted, i.e. the list shows every note. */
    val isEmpty: Boolean get() = folder == FolderFilter.All && tag == null && !pinnedOnly
}
