package com.stackpad.app.data.importing

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.domain.importing.DocxParser
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.ImportedNote
import com.stackpad.app.domain.importing.PlainTextParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.util.UUID

/** Imports Word documents (.docx): headings, bold/italic/underline, lists, tables and pictures. */
class DocxImporter(
    private val context: Context,
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {

    suspend fun import(sources: List<ImportSource>): ImportSummary = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "import-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val installer = NoteInstaller(repository, media)
            for ((index, source) in sources.withIndex()) {
                if (ImportRules.extension(source.name) != "docx") continue
                installer.sawNote()
                val dir = File(workDir, "doc$index").apply { mkdirs() }
                try {
                    addDocument(installer, source, dir)
                } catch (e: SAXException) {
                    installer.unreadable()
                } catch (e: ImportException) {
                    if (e.error == ImportError.TOO_LARGE) throw e
                    installer.unreadable()
                }
            }
            installer.finish()
        } catch (e: IOException) {
            throw ImportException(ImportError.IO, e)
        } finally {
            workDir.deleteRecursively()
        }
    }

    private suspend fun addDocument(installer: NoteInstaller, source: ImportSource, dir: File) {
        val files = source.open().use { input ->
            ZipExtractor.extract(input, dir) { path ->
                when {
                    path in XML_PARTS -> ImportRules.MAX_XML_BYTES
                    path.startsWith("word/media/") && ImportRules.isPicture(path) -> ImportRules.MAX_PICTURE_BYTES
                    else -> null
                }
            }
        }
        val document = File(dir, DOCUMENT)
        if (DOCUMENT !in files) throw ImportException(ImportError.NOT_AN_EXPORT)

        val relationships = File(dir, RELATIONSHIPS).takeIf { it.exists() }?.inputStream()?.use(DocxParser::parseRelationships).orEmpty()
        val listKinds = File(dir, NUMBERING).takeIf { it.exists() }?.inputStream()?.use(DocxParser::parseNumbering).orEmpty()
        val title = File(dir, CORE).takeIf { it.exists() }?.inputStream()?.use(DocxParser::parseTitle)
        val blocks = document.inputStream().use { DocxParser.parse(it, relationships, listKinds) }

        val id = ImportRules.noteId(SOURCE, ImportRules.fileSourceId(source.name, document.readBytes()))
        if (installer.isKnown(id)) return
        val now = System.currentTimeMillis()
        val imported = ImportedNote(id, title ?: PlainTextParser.titleFromFileName(source.name), blocks, createdAt = now, updatedAt = now)
        installer.add(id, imported, installer.withPictures(emptyList(), blocks) { File(dir, it).takeIf(File::exists) })
    }

    private companion object {
        const val SOURCE = "docx"
        const val DOCUMENT = "word/document.xml"
        const val RELATIONSHIPS = "word/_rels/document.xml.rels"
        const val NUMBERING = "word/numbering.xml"
        const val CORE = "docProps/core.xml"
        val XML_PARTS = setOf(DOCUMENT, RELATIONSHIPS, NUMBERING, CORE)
    }
}
