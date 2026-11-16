package com.stackpad.app.data.importing

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.ImportedNote
import com.stackpad.app.domain.importing.MarkdownParser
import com.stackpad.app.domain.importing.PlainTextParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Imports Markdown (.md, also Obsidian and Notion exports) and plain text (.txt) files, picked
 * one by one or inside a .zip. Pictures a note links to are imported when they are in the zip.
 */
class TextFileImporter(
    private val context: Context,
    private val repository: NoteRepository,
    private val media: MediaStorage,
) {

    suspend fun import(sources: List<ImportSource>): ImportSummary = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "import-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val installer = NoteInstaller(repository, media)
            for ((index, source) in sources.withIndex()) {
                val name = source.name
                if (ImportRules.extension(name) == "zip") {
                    importZip(source, File(workDir, "zip$index").apply { mkdirs() }, installer)
                } else if (ImportRules.isMarkdown(name) || ImportRules.isText(name)) {
                    val bytes = source.open().use { it.readBytes() }
                    addFile(installer, ImportRules.noteId(SOURCE, ImportRules.fileSourceId(name, bytes)), name, bytes, null, emptySet())
                }
            }
            installer.finish()
        } catch (e: IOException) {
            throw ImportException(ImportError.IO, e)
        } finally {
            workDir.deleteRecursively()
        }
    }

    private suspend fun importZip(source: ImportSource, dir: File, installer: NoteInstaller) {
        val files = source.open().use { input ->
            ZipExtractor.extract(input, dir) { path ->
                when {
                    ImportRules.isMarkdown(path) || ImportRules.isText(path) -> ImportRules.MAX_JSON_BYTES
                    ImportRules.isPicture(path) -> ImportRules.MAX_PICTURE_BYTES
                    else -> null
                }
            }
        }
        val pictures = files.filter(ImportRules::isPicture).toSet()
        for (path in files.filter { ImportRules.isMarkdown(it) || ImportRules.isText(it) }.sorted()) {
            val bytes = File(dir, path).readBytes()
            val id = ImportRules.noteId(SOURCE, "${source.name}!$path")
            addFile(installer, id, path.substringAfterLast('/'), bytes, dir, pictures, path.substringBeforeLast('/', ""))
        }
    }

    private suspend fun addFile(
        installer: NoteInstaller,
        id: String,
        fileName: String,
        bytes: ByteArray,
        pictureRoot: File?,
        pictures: Set<String>,
        folder: String = "",
    ) {
        installer.sawNote()
        if (installer.isKnown(id)) return
        val text = bytes.toString(Charsets.UTF_8).removePrefix("﻿")
        val now = System.currentTimeMillis()
        val imported = if (ImportRules.isMarkdown(fileName)) {
            val parsed = MarkdownParser.parse(text)
            ImportedNote(id, parsed.title.orEmpty().ifBlank { PlainTextParser.titleFromFileName(fileName) }, parsed.blocks, tags = parsed.tags, createdAt = now, updatedAt = now)
        } else {
            ImportedNote(id, PlainTextParser.titleFromFileName(fileName), PlainTextParser.parse(text), createdAt = now, updatedAt = now)
        }
        val blocks = installer.withPictures(emptyList(), imported.blocks) { reference ->
            val root = pictureRoot ?: return@withPictures null
            ImportRules.resolvePicture(ImportRules.decodeReference(reference), folder, pictures)?.let { File(root, it) }
        }
        installer.add(id, imported, blocks)
    }

    private companion object {
        const val SOURCE = "text"
    }
}
