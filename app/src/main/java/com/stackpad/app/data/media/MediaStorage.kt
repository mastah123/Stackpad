package com.stackpad.app.data.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/** Where a camera photo should be written. */
data class CameraTarget(val fileName: String, val uri: Uri)

/** Owns the app-private folder holding every image and audio file of every note. */
class MediaStorage(private val context: Context) {

    private val directory: File by lazy {
        File(context.filesDir, "media").apply { mkdirs() }
    }

    /** Resolves a stored file name; strips any path so names can never escape the folder. */
    fun file(fileName: String): File = File(directory, File(fileName).name)

    fun newImageName(): String = "img_${UUID.randomUUID()}.jpg"

    fun newAudioName(): String = "aud_${UUID.randomUUID()}.m4a"

    /** Creates an empty file and a content Uri the system camera app can write into. */
    fun createCameraTarget(): CameraTarget {
        val name = newImageName()
        val file = file(name)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return CameraTarget(fileName = name, uri = uri)
    }

    /** Copies a picked image into app storage. Returns the stored file name, or null on failure. */
    suspend fun importImage(source: Uri): String? = withContext(Dispatchers.IO) {
        val name = newImageName()
        try {
            val input = context.contentResolver.openInputStream(source)
                ?: return@withContext null
            input.use { src -> file(name).outputStream().use { dst -> src.copyTo(dst) } }
            name
        } catch (e: IOException) {
            delete(name)
            null
        } catch (e: SecurityException) {
            delete(name)
            null
        }
    }

    fun exists(fileName: String): Boolean = file(fileName).let { it.exists() && it.length() > 0 }

    fun delete(fileName: String) {
        file(fileName).delete()
    }

    /** Names of files in the media folder that were last modified more than [minAgeMs] ago. */
    fun listNames(minAgeMs: Long = 0L): List<String> {
        val cutoff = System.currentTimeMillis() - minAgeMs
        return directory.listFiles()?.filter { it.lastModified() <= cutoff }?.map { it.name }.orEmpty()
    }
}
