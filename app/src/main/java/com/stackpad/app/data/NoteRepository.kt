package com.stackpad.app.data

import com.stackpad.app.data.db.NoteDao
import com.stackpad.app.data.db.escapeLike
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.model.mediaFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Single entry point for reading and writing notes and their media files. */
class NoteRepository(
    private val dao: NoteDao,
    private val media: MediaStorage,
    private val scope: CoroutineScope,
) {
    private val writeLock = Mutex()

    fun observeSummaries(query: String): Flow<List<NoteSummary>> =
        dao.observe(escapeLike(query.trim())).map { list -> list.map { it.toSummary() } }

    suspend fun load(id: String): Note? = dao.get(id)?.toNote()

    suspend fun save(note: Note) {
        writeLock.withLock {
            dao.upsert(note.toEntity(updatedAt = System.currentTimeMillis()))
        }
    }

    /** Saves on the app-wide scope so the write survives the caller being destroyed. */
    fun saveAsync(note: Note) {
        scope.launch { save(note) }
    }

    suspend fun setPinned(id: String, pinned: Boolean) {
        writeLock.withLock { dao.setPinned(id, pinned) }
    }

    /** Deletes the note and every image/audio file it owns. */
    suspend fun delete(id: String) {
        writeLock.withLock {
            dao.get(id)?.toNote()?.blocks?.forEach { block ->
                block.mediaFileName()?.let(media::delete)
            }
            dao.delete(id)
        }
    }
}
