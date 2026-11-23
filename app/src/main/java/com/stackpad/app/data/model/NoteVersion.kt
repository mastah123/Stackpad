package com.stackpad.app.data.model

/** An earlier state of a note, as shown in the history list. */
data class NoteVersion(val id: Long, val savedAt: Long, val title: String, val blocks: List<Block>)
