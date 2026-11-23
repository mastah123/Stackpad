package com.stackpad.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteVersionDao {
    @Insert
    suspend fun insert(version: NoteVersionEntity)

    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY capturedAt DESC, id DESC")
    suspend fun forNote(noteId: String): List<NoteVersionEntity>

    @Query("DELETE FROM note_versions WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("DELETE FROM note_versions WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: String)

    @Query("SELECT blocksJson FROM note_versions")
    suspend fun allBlocksJson(): List<String>
}
