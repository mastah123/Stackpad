package com.stackpad.app.data.importing

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.KeepConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID

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
            val files = ZipExtractor.extract(input, workDir) { path ->
                when {
                    ImportRules.isJson(path) -> ImportRules.MAX_JSON_BYTES
                    ImportRules.isPicture(path) -> ImportRules.MAX_PICTURE_BYTES
                    else -> null
                }
            }
            val jsonFiles = files.filter(ImportRules::isJson).sorted()
            val pictureFiles = files.filter(ImportRules::isPicture).toSet()
            if (jsonFiles.isEmpty()) throw ImportException(ImportError.NOTHING_FOUND)

            val installer = NoteInstaller(repository, media)
            for (path in jsonFiles) {
                val parsed = runCatching { KeepJson.parse(File(workDir, path).readText(Charsets.UTF_8)) }
                if (parsed.isFailure) {
                    installer.unreadable()
                    continue
                }
                val keep = parsed.getOrNull() ?: continue // valid JSON, but not a Keep note
                installer.sawNote()

                if (keep.trashed) {
                    installer.trashed()
                    continue
                }
                val id = ImportRules.noteId(SOURCE, path)
                if (installer.isKnown(id)) continue

                val imported = KeepConverter.convert(keep, path, System.currentTimeMillis())
                installer.skippedAttachments(imported.skippedAttachments)
                val folder = path.substringBeforeLast('/', "")
                val pictures = imported.pictures.mapNotNull { attachment ->
                    ImportRules.resolvePicture(attachment, folder, pictureFiles)?.let { File(workDir, it) }
                }
                installer.add(id, imported, installer.withPictures(pictures, imported.blocks) { null })
            }
            installer.finish()
        } catch (e: IOException) {
            throw ImportException(ImportError.IO, e)
        } finally {
            workDir.deleteRecursively()
        }
    }

    private companion object {
        const val SOURCE = "keep"
    }
}
