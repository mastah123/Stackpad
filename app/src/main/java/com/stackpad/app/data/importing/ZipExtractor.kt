package com.stackpad.app.data.importing

import com.stackpad.app.domain.importing.ImportRules
import java.io.File
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/** Extracts the wanted entries of a zip into a work folder, with limits on count, size and paths. */
internal object ZipExtractor {
    private const val BUFFER_SIZE = 16 * 1024

    /**
     * [limitFor] gives the largest allowed size of an entry, or null to skip it.
     * Returns the relative paths of the extracted files.
     */
    fun extract(input: InputStream, workDir: File, limitFor: (String) -> Long?): List<String> {
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
                    val limit = limitFor(path) ?: continue
                    val target = File(workDir, path)
                    target.parentFile?.mkdirs()
                    total += copyLimited(zip, target, limit)
                    if (total > ImportRules.MAX_TOTAL_BYTES) throw ImportException(ImportError.TOO_LARGE)
                    extracted += path
                }
            }
        } catch (e: ZipException) {
            throw ImportException(ImportError.NOT_AN_EXPORT, e)
        }
        return extracted
    }

    /** Copies one stream to a file, stopping with TOO_LARGE past [limit] bytes. */
    fun copyLimited(input: InputStream, target: File, limit: Long): Long {
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
}
