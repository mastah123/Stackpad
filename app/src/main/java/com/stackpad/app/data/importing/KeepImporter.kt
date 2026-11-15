package com.stackpad.app.data.importing

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.Note
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.KeepConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/** Why an import failed. */
enum class ImportError { NOT_AN_EXPORT, NOTHING_FOUND, TOO_LARGE, IO }

class ImportException(val error: ImportError, cause: Throwable? = null) : Exception(error.name, cause)

/** What an import did, for the message shown afterwards. */
data class ImportSummary(
    val imported: Int,
    /** Notes from an earlier import of the same file, left as they are. */
    val alreadyImported: Int,
    /** Notes that were in Keep's trash. */
    val skippedTrashed: Int,
    /** Files that looked like notes but could not be read. */
    val unreadable: Int,
    val pictures: Int,
    /** Audio and other attachments that cannot be imported. */
    val skippedAttachments: Int,
)

/**
 * Imports a Google Keep Takeout zip: text and list notes with their labels (as tags), colors,
 * pinned state, dates, links and pictures. Importing the same zip again adds nothing new.
 */
class KeepImporter(
    private val context: Context,
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {

    /** Throws [ImportException]; nothing is added then. */
    suspend fun import(input: InputStream): ImportSummary = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "import-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val files = extract(input, workDir)
            val jsonFiles = files.filter(ImportRules::isJson).sorted()
            val pictureFiles = files.filter(ImportRules::isPicture).toSet()
            if (jsonFiles.isEmpty()) throw ImportException(ImportError.NOTHING_FOUND)

            val notes = ArrayList<Note>()
            var existing = 0
            var trashed = 0
            var unreadable = 0
            var pictures = 0
            var skippedAttachments = 0
            var keepNotesSeen = 0

            for (path in jsonFiles) {
                val parsed = runCatching { KeepJson.parse(File(workDir, path).readText(Charsets.UTF_8)) }
                if (parsed.isFailure) {
                    unreadable++
                    continue
                }
                val keep = parsed.getOrNull() ?: continue // valid JSON, but not a Keep note
                keepNotesSeen++

                if (keep.trashed) {
                    trashed++
                    continue
                }
                val id = ImportRules.noteId(SOURCE, path)
                if (repository.exists(id)) {
                    existing++
                    continue
                }

                val imported = KeepConverter.convert(keep, path, System.currentTimeMillis())
                skippedAttachments += imported.skippedAttachments
                val folder = path.substringBeforeLast('/', "")
                val imageBlocks: List<Block> = imported.pictures.mapNotNull { attachment ->
                    val resolved = ImportRules.resolvePicture(attachment, folder, pictureFiles) ?: return@mapNotNull null
                    val stored = media.importFile(File(workDir, resolved)) ?: return@mapNotNull null
                    pictures++
                    ImageBlock(BlockFactory.newId(), stored)
                }
                val blocks = imageBlocks + imported.blocks
                // A note with nothing in it (for example only an audio clip) is not worth adding.
                if (blocks.isEmpty() && imported.title.isBlank()) continue

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

            if (keepNotesSeen == 0 && unreadable == 0) throw ImportException(ImportError.NOTHING_FOUND)
            val added = repository.importNotes(notes)
            ImportSummary(added, existing + (notes.size - added), trashed, unreadable, pictures, skippedAttachments)
        } catch (e: IOException) {
            throw ImportException(ImportError.IO, e)
        } finally {
            workDir.deleteRecursively()
        }
    }

    /** Extracts the notes (json) and pictures of the zip into [workDir] within size limits; returns their relative paths. */
    private fun extract(input: InputStream, workDir: File): List<String> {
        val extracted = ArrayList<String>()
        var entries = 0
        var total = 0L
        try {
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (++entries > ImportRules.MAX_ENTRIES) throw ImportException(ImportError.TOO_LARGE)
                    if (entry.isDirectory) continue
                    val path = ImportRules.safePath(entry.name) ?: continue
                    val limit = when {
                        ImportRules.isJson(path) -> ImportRules.MAX_JSON_BYTES
                        ImportRules.isPicture(path) -> ImportRules.MAX_PICTURE_BYTES
                        else -> continue
                    }
                    val target = File(workDir, path)
                    target.parentFile?.mkdirs()
                    val size = copyLimited(zip, target, limit)
                    total += size
                    if (total > ImportRules.MAX_TOTAL_BYTES) throw ImportException(ImportError.TOO_LARGE)
                    extracted += path
                }
            }
        } catch (e: ZipException) {
            throw ImportException(ImportError.NOT_AN_EXPORT, e)
        }
        return extracted
    }

    private fun copyLimited(input: InputStream, target: File, limit: Long): Long {
        var total = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        target.outputStream().use { out ->
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                total += n
                if (total > limit) throw ImportException(ImportError.TOO_LARGE)
                out.write(buffer, 0, n)
            }
        }
        return total
    }

    private companion object {
        const val SOURCE = "keep"
        const val BUFFER_SIZE = 16 * 1024
    }
}
