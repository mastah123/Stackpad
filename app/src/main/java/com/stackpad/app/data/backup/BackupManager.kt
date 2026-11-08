package com.stackpad.app.data.backup

import android.content.Context
import com.stackpad.app.data.NoteRepository
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.mediaFileName
import com.stackpad.app.domain.backup.BackupRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Writes every note and its media to a zip, and merges such a zip back in.
 * Layout: `backup.json` plus `media/<file>`.
 */
class BackupManager(
    private val context: Context,
    private val repository: NoteRepository,
    private val media: MediaStorage,
    private val appVersion: String,
) {

    /** Writes a backup to [out]. Returns how many notes were included. Throws [BackupException]. */
    suspend fun export(out: OutputStream): Int = withContext(Dispatchers.IO) {
        try {
            val backup = repository.snapshotForBackup(appVersion)
            ZipOutputStream(out.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(BackupRules.JSON_ENTRY))
                zip.write(backupJson.encodeToString(BackupFile.serializer(), backup).toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                val names = backup.notes.flatMap { note -> note.blocks.mapNotNull { it.mediaFileName() } }.distinct()
                for (name in names) {
                    if (!BackupRules.isSafeMediaName(name)) continue
                    val input = media.open(name) ?: continue
                    input.use {
                        zip.putNextEntry(ZipEntry(BackupRules.mediaEntryName(name)))
                        it.copyTo(zip)
                        zip.closeEntry()
                    }
                }
            }
            backup.notes.size
        } catch (e: IOException) {
            throw BackupException(BackupError.IO, e)
        }
    }

    /** Merges the backup in [input] into the app. Throws [BackupException]; nothing changes then. */
    suspend fun restore(input: InputStream): RestoreSummary = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "restore-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val backup = extract(input, workDir)
            repository.mergeBackup(backup) { name ->
                val extracted = File(workDir, name)
                if (extracted.isFile) media.install(name, extracted)
            }
        } catch (e: IOException) {
            throw BackupException(BackupError.IO, e)
        } finally {
            workDir.deleteRecursively()
        }
    }

    /** Reads the zip into [workDir] (enforcing size limits) and returns the parsed `backup.json`. */
    private fun extract(input: InputStream, workDir: File): BackupFile {
        var json: String? = null
        var entries = 0
        var totalMedia = 0L
        try {
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (++entries > BackupRules.MAX_ENTRIES) throw BackupException(BackupError.TOO_LARGE)
                    if (entry.isDirectory) continue
                    when {
                        entry.name == BackupRules.JSON_ENTRY -> {
                            val bytes = readLimited(zip, BackupRules.MAX_JSON_BYTES)
                            json = bytes.toString(Charsets.UTF_8)
                        }
                        else -> {
                            val name = BackupRules.mediaNameOf(entry.name) ?: continue
                            val target = File(workDir, name)
                            val size = copyLimited(zip, target, BackupRules.MAX_MEDIA_BYTES)
                            totalMedia += size
                            if (totalMedia > BackupRules.MAX_TOTAL_MEDIA_BYTES) throw BackupException(BackupError.TOO_LARGE)
                        }
                    }
                }
            }
        } catch (e: ZipException) {
            throw BackupException(BackupError.NOT_A_BACKUP, e)
        }

        val text = json ?: throw BackupException(BackupError.NOT_A_BACKUP)
        val backup = try {
            backupJson.decodeFromString(BackupFile.serializer(), text)
        } catch (e: SerializationException) {
            throw BackupException(BackupError.UNREADABLE, e)
        } catch (e: IllegalArgumentException) {
            throw BackupException(BackupError.UNREADABLE, e)
        }
        if (!BackupRules.isSupportedVersion(backup.formatVersion)) {
            throw BackupException(BackupError.UNSUPPORTED_VERSION)
        }
        return backup
    }

    private fun readLimited(input: InputStream, limit: Long): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            total += n
            if (total > limit) throw BackupException(BackupError.TOO_LARGE)
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }

    private fun copyLimited(input: InputStream, target: File, limit: Long): Long {
        var total = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        target.outputStream().use { out ->
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                total += n
                if (total > limit) throw BackupException(BackupError.TOO_LARGE)
                out.write(buffer, 0, n)
            }
        }
        return total
    }

    private companion object {
        const val BUFFER_SIZE = 16 * 1024
    }
}
