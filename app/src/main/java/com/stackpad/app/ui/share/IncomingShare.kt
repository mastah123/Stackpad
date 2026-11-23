package com.stackpad.app.ui.share

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

/** What another app shared to Stackpad. */
data class IncomingShare(val subject: String?, val text: String?, val pictures: List<Uri>)

private const val MAX_PICTURES = 20

/** The shared content of a SEND / SEND_MULTIPLE intent, or null for any other intent. */
fun Intent.toIncomingShare(): IncomingShare? {
    val pictures: List<Uri> = when (action) {
        Intent.ACTION_SEND ->
            listOfNotNull(IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java))
        Intent.ACTION_SEND_MULTIPLE ->
            IntentCompat.getParcelableArrayListExtra(this, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
        else -> return null
    }
    val subject = getStringExtra(Intent.EXTRA_SUBJECT) ?: getStringExtra(Intent.EXTRA_TITLE)
    val text = getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
    if (pictures.isEmpty() && text.isNullOrBlank() && subject.isNullOrBlank()) return null
    return IncomingShare(subject, text, pictures.take(MAX_PICTURES))
}
