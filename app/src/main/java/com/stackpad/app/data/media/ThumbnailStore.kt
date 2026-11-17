package com.stackpad.app.data.media

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Small JPEG copies of note pictures for the note list, so scrolling never decodes full photos.
 * Thumbnails live in the cache folder: the system may clear them, and they are simply made again.
 */
class ThumbnailStore(context: Context, private val media: MediaStorage) {

    private val directory: File by lazy { File(context.cacheDir, "thumbs").apply { mkdirs() } }

    /** The thumbnail for the picture [fileName], made on first use, or null when the picture is missing or unreadable. */
    suspend fun get(fileName: String): File? = withContext(Dispatchers.IO) {
        val source = media.file(fileName)
        if (!source.exists() || source.length() == 0L) return@withContext null
        val thumb = File(directory, source.nameWithoutExtension + ".jpg")
        if (thumb.length() > 0 && thumb.lastModified() >= source.lastModified()) return@withContext thumb
        runCatching { create(source, thumb) }.getOrNull()
    }

    private fun create(source: File, thumb: File): File? {
        val decoded = BitmapLoader.load(source, MAX_SIDE) ?: return null
        val scale = MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
        val result = if (scale < 1f) {
            Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt().coerceAtLeast(1), (decoded.height * scale).toInt().coerceAtLeast(1), true)
        } else {
            decoded
        }

        // Write to a temporary file first so a half-written thumbnail is never used.
        val temp = File(thumb.parentFile, thumb.name + ".tmp")
        try {
            temp.outputStream().use { check(result.compress(Bitmap.CompressFormat.JPEG, QUALITY, it)) }
            if (!temp.renameTo(thumb)) return null
        } finally {
            temp.delete()
            if (result !== decoded) result.recycle()
            decoded.recycle()
        }
        return thumb
    }

    private companion object {
        const val MAX_SIDE = 480
        const val QUALITY = 80
    }
}
