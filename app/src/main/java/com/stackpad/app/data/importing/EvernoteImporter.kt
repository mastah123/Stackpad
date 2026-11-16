package com.stackpad.app.data.importing

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.domain.importing.EnexParser
import com.stackpad.app.domain.importing.EnmlConverter
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.ImportedNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.util.UUID

/** Imports Evernote exports (.enex): text, checkboxes, lists, tables, tags, dates and pictures. */
class EvernoteImporter(
    private val context: Context,
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {

    suspend fun import(sources: List<ImportSource>): ImportSummary = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "import-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val installer = NoteInstaller(repository, media)
            for (source in sources) {
                if (ImportRules.extension(source.name) != "enex") continue
                try {
                    source.open().use { input ->
                        EnexParser.parse(input) { note -> addNote(installer, note, source.name, workDir) }
                    }
                } catch (e: SAXException) {
                    throw ImportException(ImportError.NOT_AN_EXPORT, e)
                }
            }
            installer.finish()
        } catch (e: IOException) {
            throw ImportException(ImportError.IO, e)
        } finally {
            workDir.deleteRecursively()
        }
    }

    private fun addNote(installer: NoteInstaller, note: EnexParser.Note, sourceName: String, workDir: File) {
        installer.sawNote()
        val id = ImportRules.noteId(SOURCE, "$sourceName:${note.title}:${note.createdAt}")
        // The parser calls back without suspending; this runs on an IO thread, so blocking here is fine.
        if (runBlocking { installer.isKnown(id) }) return

        val files = HashMap<String, File>()
        var otherAttachments = 0
        note.resources.forEach { resource ->
            if (resource.mime.startsWith("image/")) {
                files[EnmlConverter.RESOURCE_PREFIX + resource.hash] = File(workDir, resource.hash).also { it.writeBytes(resource.data) }
            } else {
                otherAttachments++
            }
        }
        installer.skippedAttachments(otherAttachments)

        val now = System.currentTimeMillis()
        val imported = ImportedNote(
            sourceId = id,
            title = note.title,
            blocks = note.blocks,
            tags = note.tags,
            createdAt = note.createdAt.takeIf { it > 0 } ?: now,
            updatedAt = note.updatedAt.takeIf { it > 0 } ?: now,
        )
        installer.add(id, imported, installer.withPictures(emptyList(), note.blocks) { files[it] })
    }

    private companion object {
        const val SOURCE = "evernote"
    }
}
