package com.stackpad.app.data.media

import android.graphics.Bitmap
import com.stackpad.app.domain.image.ImageEdit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Writes an edited copy of a stored picture as a new file; the source file is never touched. */
class ImageEditSaver(private val media: MediaStorage) {

    /** Returns the new file's name, or null when the picture could not be read, was too big for memory, or could not be written. */
    suspend fun save(fileName: String, edit: ImageEdit): String? = withContext(Dispatchers.IO) {
        var source: Bitmap? = null
        var result: Bitmap? = null
        var temp: File? = null
        try {
            source = BitmapLoader.load(media.file(fileName), MAX_SIDE) ?: return@withContext null
            result = ImageTransformer.apply(source, edit)
            val name = media.newImageName()
            val target = media.file(name)
            temp = File(target.parentFile, "$name.tmp")
            temp.outputStream().use { check(result.compress(Bitmap.CompressFormat.JPEG, edit.size.jpegQuality, it)) }
            if (!temp.renameTo(target)) return@withContext null
            name
        } catch (e: IOException) {
            null
        } catch (e: IllegalStateException) {
            null
        } catch (e: OutOfMemoryError) {
            null
        } finally {
            temp?.delete()
            if (result !== source) result?.recycle()
            source?.recycle()
        }
    }

    private companion object {
        /** Pictures bigger than this are halved while editing, which keeps memory use safe. */
        const val MAX_SIDE = 4096
    }
}
