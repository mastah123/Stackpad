package com.stackpad.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** The note's blocks, serialized by [com.stackpad.app.data.BlockJsonCodec]. */
    val blocksJson: String,
    /** Plain text of all blocks, used for search and previews. */
    val searchText: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    /** Folder the note is filed in, or null. */
    val folderId: String? = null,
    /** Tags separated by newlines (see [com.stackpad.app.domain.notes.Tags]). */
    @ColumnInfo(defaultValue = "''") val tags: String = "",
    /** Index into the note color palette; 0 means no color. */
    @ColumnInfo(defaultValue = "0") val color: Int = 0,
    /** When the note was moved to the trash, or null for a normal note. */
    val trashedAt: Long? = null,
)
