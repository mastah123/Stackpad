package com.stackpad.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Fires when a due-date alarm goes off and shows the notification. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val key = intent.getStringExtra(ReminderNotifications.EXTRA_KEY) ?: return
        val noteId = intent.getStringExtra(ReminderNotifications.EXTRA_NOTE_ID) ?: return
        ReminderNotifications.show(
            context = context,
            key = key,
            noteId = noteId,
            noteTitle = intent.getStringExtra(ReminderNotifications.EXTRA_NOTE_TITLE).orEmpty(),
            itemText = intent.getStringExtra(ReminderNotifications.EXTRA_ITEM_TEXT).orEmpty(),
        )
    }
}
