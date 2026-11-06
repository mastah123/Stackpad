package com.stackpad.app.data.db

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
)
