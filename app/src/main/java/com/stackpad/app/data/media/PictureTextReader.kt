package com.stackpad.app.data.media

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/** Reads the text in a picture on the phone (Latin alphabet), with Google's ML Kit; nothing is uploaded. */
class PictureTextReader {
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    /** The text found (empty when there is none), or null when the picture or the recogniser failed. */
    suspend fun read(file: File): String? {
        val bitmap = withContext(Dispatchers.IO) { BitmapLoader.load(file, MIN_SIDE) } ?: return null
        return try {
            suspendCancellableCoroutine { continuation ->
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { continuation.resume(it.text) }
                    .addOnFailureListener { continuation.resume(null) }
            }
        } catch (e: Exception) {
            null
        } finally {
            bitmap.recycle()
        }
    }

    private companion object {
        const val MIN_SIDE = 2048
    }
}
