package com.stackpad.app.data.importing

import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.Note
import com.stackpad.app.domain.importing.ImportedNote
import java.io.File

/**
 * Collects the notes of one import run and adds them to the app together at the end. Every
 * importer uses it, so skipping notes that were already imported, storing pictures and building
 * the summary happen in one place.
 */
internal class NoteInstaller(
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {
    private val notes = ArrayList<Note>()
    private var existing = 0
    private var unreadable = 0
    private var pictures = 0
    private var skipped = 0
    private var trashed = 0

    /** True when a file or note was recognised, even if it ended up empty; decides "nothing found". */
    var seen = 0
        private set

    fun sawNote() { seen++ }
    fun unreadable() { unreadable++; seen++ }
    fun trashed() { trashed++ }
    fun skippedAttachments(count: Int) { skipped += count }

    suspend fun isKnown(id: String): Boolean = repository.exists(id).also { if (it) existing++ }

    /** Copies a picture into app storage; returns its stored name, or null (counted as skipped). */
    fun storePicture(file: File): String? {
        val stored = media.importFile(file)
        if (stored == null) skipped++ else pictures++
        return stored
    }

    /** Pictures first as image blocks, then [blocks] with their image placeholders swapped for stored pictures. */
    fun withPictures(leading: List<File>, blocks: List<Block>, resolve: (String) -> File?): List<Block> {
        val first = leading.mapNotNull { storePicture(it) }.map { ImageBlock(BlockFactory.newId(), it) }
        val rest = blocks.mapNotNull { block ->
            if (block !is ImageBlock) return@mapNotNull block
            val file = resolve(block.fileName)
            val stored = file?.let { storePicture(it) }
            if (stored == null) {
                if (file == null) skipped++
                null
            } else {
                block.copy(fileName = stored)
            }
        }
        return first + rest
    }

    /** A note with nothing in it (for example only an audio clip) is not worth adding. */
    fun add(id: String, imported: ImportedNote, blocks: List<Block>) {
        if (blocks.isEmpty() && imported.title.isBlank()) return
        notes += Note(
            id = id,
            title = imported.title,
            blocks = blocks,
            pinned = imported.pinned,
            createdAt = imported.createdAt,
            updatedAt = imported.updatedAt,
            tags = imported.tags,
            color = imported.color,
        )
    }

    /** Adds everything collected; throws NOTHING_FOUND when no note was recognised at all. */
    suspend fun finish(): ImportSummary {
        if (seen == 0) throw ImportException(ImportError.NOTHING_FOUND)
        val added = repository.importNotes(notes)
        return ImportSummary(added, existing + (notes.size - added), trashed, unreadable, pictures, skipped)
    }
}
