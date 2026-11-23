package com.stackpad.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** An earlier state of a note, kept so it can be restored. */
@Entity(tableName = "note_versions", indices = [Index("noteId")])
data class NoteVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: String,
    /** When the content was last saved (shown to the user). */
    val savedAt: Long,
    /** When this copy was made; the spacing of versions is based on it. */
    val capturedAt: Long,
    val title: String,
    val blocksJson: String,
)
