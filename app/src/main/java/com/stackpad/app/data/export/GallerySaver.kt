package com.stackpad.app.data.export

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Copies an exported picture into the phone's gallery, in the Pictures/Stackpad album. */
class GallerySaver(private val context: Context) {

    /** True when the picture is in the gallery. Android 9 and older need the storage permission first. */
    suspend fun savePng(source: File, displayName: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveWithMediaStore(source, displayName)
            else saveToPicturesFolder(source, displayName)
        }.getOrDefault(false)
    }

    private fun saveWithMediaStore(source: File, displayName: String): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } } ?: error("no stream")
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null) // do not leave a half-written picture behind
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun saveToPicturesFolder(source: File, displayName: String): Boolean {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM)
        if (!folder.isDirectory && !folder.mkdirs()) return false
        val target = File(folder, "$displayName.png")
        source.copyTo(target, overwrite = true)
        MediaScannerConnection.scanFile(context, arrayOf(target.path), arrayOf("image/png"), null)
        return true
    }

    private companion object {
        const val ALBUM = "Stackpad"
    }
}
