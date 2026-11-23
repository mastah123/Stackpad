package com.stackpad.app.data.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File

/** Decodes pictures the way they are shown: scaled down to save memory and turned upright by their EXIF orientation. */
object BitmapLoader {

    /**
     * Decodes [file], halving its size while the longest side stays at least [minSide] pixels
     * (so the result is between [minSide] and twice that, or the full size when it is smaller).
     * Returns null when the file is missing or not a picture.
     */
    fun load(file: File, minSide: Int): Bitmap? =
        decode(file) { width, height -> maxOf(width, height) / minSide }

    /**
     * Like [load], but halves only while the upright picture stays at least [minWidth] pixels wide,
     * so a picture drawn at that width is never blurry.
     */
    fun loadFittingWidth(file: File, minWidth: Int): Bitmap? =
        decode(file) { width, height -> width / minWidth }

    /** [limit] gets the upright width and height and says how many times the picture could be halved (as a ratio to the wanted size). */
    private inline fun decode(file: File, limit: (width: Int, height: Int) -> Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val degrees = rotationDegrees(file)
        val sideways = degrees == 90f || degrees == 270f
        val width = if (sideways) bounds.outHeight else bounds.outWidth
        val height = if (sideways) bounds.outWidth else bounds.outHeight

        var sample = 1
        while (limit(width, height) / (sample * 2) >= 1) sample *= 2
        val decoded = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        if (degrees == 0f) return decoded
        val upright = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(degrees) }, true)
        if (upright !== decoded) decoded.recycle()
        return upright
    }

    private fun rotationDegrees(file: File): Float = runCatching {
        when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }.getOrDefault(0f)
}
