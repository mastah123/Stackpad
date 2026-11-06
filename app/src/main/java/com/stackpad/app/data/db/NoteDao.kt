package com.stackpad.app.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /**
     * Notes matching [query] (title or content), pinned first, newest first.
     * [query] must already be escaped with [escapeLike]; an empty string matches everything.
     */
    @Query(
        """
        SELECT * FROM notes
        WHERE :query = ''
           OR title LIKE '%' || :query || '%' ESCAPE '\'
           OR searchText LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY pinned DESC, updatedAt DESC
        """,
    )
    fun observe(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: String): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE notes SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)
}

/** Escapes the LIKE wildcards so user input is matched literally. */
fun escapeLike(raw: String): String =
    raw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
