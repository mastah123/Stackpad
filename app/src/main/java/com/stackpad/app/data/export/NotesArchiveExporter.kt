package com.stackpad.app.data.export

import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.backup.BackupError
import com.stackpad.app.data.backup.BackupException
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.toNote
import com.stackpad.app.domain.backup.BackupRules
import com.stackpad.app.domain.export.ArchivePlanner
import com.stackpad.app.domain.export.ExportFormat
import com.stackpad.app.domain.export.NoteExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes every note as a Markdown file (one folder per Stackpad folder) plus a shared `media/`
 * folder with the pictures, all in one zip. Locked and trashed notes are left out.
 */
class NotesArchiveExporter(
    private val repository: NoteRepository,
    private val media: MediaStorage,
    private val appVersion: String,
) {
    data class Result(val exported: Int, val skippedLocked: Int)

    suspend fun export(out: OutputStream): Result = withContext(Dispatchers.IO) {
        try {
            val backup = repository.snapshotForBackup(appVersion)
            val folders = backup.folders.associate { it.id to it.name }
            val notes = backup.notes.map { it.toNote(folderKnown = it.folderId in folders) }
            val plan = ArchivePlanner.plan(notes, folders)
            val pictures = LinkedHashSet<String>()

            ZipOutputStream(out.buffered()).use { zip ->
                for (item in plan.items) {
                    var text = NoteExporter.export(item.note, ExportFormat.MARKDOWN) { image ->
                        if (BackupRules.isSafeMediaName(image.fileName)) {
                            pictures += image.fileName
                            item.mediaPrefix + image.fileName
                        } else {
                            null
                        }
                    }
                    if (item.note.tags.isNotEmpty()) {
                        text += "\n\n" + item.note.tags.joinToString(" ") { "#$it" }
                    }
                    zip.putNextEntry(ZipEntry(item.path))
                    zip.write(text.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                for (name in pictures) {
                    val input = media.open(name) ?: continue
                    input.use {
                        zip.putNextEntry(ZipEntry("media/$name"))
                        it.copyTo(zip)
                        zip.closeEntry()
                    }
                }
            }
            Result(plan.items.size, plan.skippedLocked)
        } catch (e: IOException) {
            throw BackupException(BackupError.IO, e)
        }
    }
}
