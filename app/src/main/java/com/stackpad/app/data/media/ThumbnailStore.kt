package com.stackpad.app.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeFile(source.path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        val scale = MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            postRotate(rotationDegrees(source))
        }
        val result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

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

    private fun rotationDegrees(file: File): Float = runCatching {
        when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }.getOrDefault(0f)

    private companion object {
        const val MAX_SIDE = 480
        const val QUALITY = 80
    }
}
