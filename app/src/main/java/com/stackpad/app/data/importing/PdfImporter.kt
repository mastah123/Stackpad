package com.stackpad.app.data.importing

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.ImportedNote
import com.stackpad.app.domain.importing.PdfTextStructurer
import com.stackpad.app.domain.importing.PlainTextParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Imports PDFs as notes. A PDF with text becomes headings, paragraphs and lists (PDFs store no
 * structure, so this is a best effort); a PDF without text, such as a scan, becomes a note with
 * one picture per page (the first [PdfPageRenderer.MAX_PAGES] pages).
 */
class PdfImporter(
    private val context: Context,
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {
    private val extractor = PdfTextExtractor(context)

    suspend fun import(sources: List<ImportSource>): ImportSummary = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "import-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val installer = NoteInstaller(repository, media)
            for ((index, source) in sources.withIndex()) {
                if (ImportRules.extension(source.name) != "pdf") continue
                installer.sawNote()
                val dir = File(workDir, "pdf$index").apply { mkdirs() }
                try {
                    addPdf(installer, source, dir)
                } catch (e: ImportException) {
                    if (e.error == ImportError.TOO_LARGE) throw e
                    installer.unreadable()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    installer.unreadable() // damaged, password-protected or unsupported PDF
                } catch (e: OutOfMemoryError) {
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

    private suspend fun addPdf(installer: NoteInstaller, source: ImportSource, dir: File) {
        val file = File(dir, "source.pdf")
        source.open().use { ZipExtractor.copyLimited(it, file, ImportRules.MAX_PICTURE_BYTES) }
        val head = ByteArray(minOf(file.length(), HASHED_BYTES.toLong()).toInt())
        DataInputStream(file.inputStream()).use { it.readFully(head) }
        val id = ImportRules.noteId(SOURCE, ImportRules.fileSourceId(source.name, head + file.length().toString().toByteArray()))
        if (installer.isKnown(id)) return

        val title = PlainTextParser.titleFromFileName(source.name)
        val now = System.currentTimeMillis()
        val pages = extractor.pages(file)
        if (PdfTextStructurer.hasText(pages)) {
            val blocks = PdfTextStructurer.blocks(pages)
            installer.add(id, ImportedNote(id, title, blocks, createdAt = now, updatedAt = now), blocks)
        } else {
            val (images, leftOut) = PdfPageRenderer.render(file, File(dir, "pages").apply { mkdirs() })
            installer.skippedAttachments(leftOut)
            installer.add(id, ImportedNote(id, title, emptyList(), createdAt = now, updatedAt = now), installer.withPictures(images, emptyList()) { null })
        }
    }

    private companion object {
        const val SOURCE = "pdf"
        const val HASHED_BYTES = 1024 * 1024
    }
}
