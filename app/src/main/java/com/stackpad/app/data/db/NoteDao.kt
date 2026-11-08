package com.stackpad.app.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /**
     * Notes that are not in the trash and match [query] (title, content or tags).
     * [query] must already be escaped with [escapeLike]; an empty string matches everything.
     * Ordering is done by the caller.
     */
    @Query(
        """
        SELECT * FROM notes
        WHERE trashedAt IS NULL AND (
              :query = ''
           OR title LIKE '%' || :query || '%' ESCAPE '\'
           OR searchText LIKE '%' || :query || '%' ESCAPE '\'
           OR tags LIKE '%' || :query || '%' ESCAPE '\'
        )
        """,
    )
    fun observeActive(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE trashedAt IS NOT NULL ORDER BY trashedAt DESC")
    fun observeTrashed(): Flow<List<NoteEntity>>

    /** Raw tag strings of every active note that has tags. */
    @Query("SELECT tags FROM notes WHERE trashedAt IS NULL AND tags != ''")
    fun observeTagStrings(): Flow<List<String>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: String): NoteEntity?

    @Query("SELECT blocksJson FROM notes")
    suspend fun allBlocksJson(): List<String>

    @Query("SELECT * FROM notes WHERE trashedAt IS NOT NULL AND trashedAt <= :cutoff")
    suspend fun trashedAtOrBefore(cutoff: Long): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE trashedAt IS NULL")
    suspend fun allActive(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE trashedAt IS NOT NULL")
    suspend fun allTrashed(): List<NoteEntity>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE notes SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE notes SET trashedAt = :trashedAt WHERE id = :id")
    suspend fun setTrashedAt(id: String, trashedAt: Long?)

    @Query("UPDATE notes SET folderId = NULL WHERE folderId = :folderId")
    suspend fun clearFolder(folderId: String)
}

/** Escapes the LIKE wildcards so user input is matched literally. */
fun escapeLike(raw: String): String =
    raw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
