package com.stackpad.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.stackpad.app.MainActivity
import com.stackpad.app.R

/** A home-screen shortcut that opens one note. */
object NoteShortcut {
    private const val LABEL_LENGTH = 25

    fun isSupported(context: Context): Boolean = ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    /** Asks the launcher to pin a shortcut to [noteId]. Returns false when the launcher cannot do it. */
    fun pin(context: Context, noteId: String, title: String): Boolean {
        if (!isSupported(context)) return false
        val intent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(MainActivity.EXTRA_NOTE_ID, noteId)
        val info = ShortcutInfoCompat.Builder(context, "note-$noteId")
            .setShortLabel(title.take(LABEL_LENGTH))
            .setLongLabel(title)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }
}
