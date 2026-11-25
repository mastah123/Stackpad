package com.stackpad.app.domain.notes

/** How the note list is split under headers. */
enum class NoteGroup { DEFAULT, NONE, ABC, YEAR, WEEK, MONTH }

/**
 * How the note list is ordered and grouped. [reversed] flips the usual direction of [sort]
 * (dates: newest first, title: A to Z). Pinned notes always stay on top.
 */
data class ListOrder(
    val sort: NoteSort = NoteSort.LAST_EDITED,
    val reversed: Boolean = false,
    val group: NoteGroup = NoteGroup.DEFAULT,
)
