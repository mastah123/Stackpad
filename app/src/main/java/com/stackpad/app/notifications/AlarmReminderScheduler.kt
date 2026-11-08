package com.stackpad.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.stackpad.app.domain.reminders.ReminderScheduler
import com.stackpad.app.domain.reminders.ReminderSpec

/**
 * Schedules reminders with [AlarmManager]. Alarms are inexact (they may arrive a little late
 * when the device is idle), which avoids needing the exact-alarm permission.
 */
class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {
    private val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(spec: ReminderSpec) {
        val intent = intentFor(spec.key)
            .putExtra(ReminderNotifications.EXTRA_NOTE_ID, spec.noteId)
            .putExtra(ReminderNotifications.EXTRA_KEY, spec.key)
            .putExtra(ReminderNotifications.EXTRA_NOTE_TITLE, spec.noteTitle)
            .putExtra(ReminderNotifications.EXTRA_ITEM_TEXT, spec.itemText)
        val pending = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        runCatching { alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, spec.triggerAt, pending) }
    }

    override fun cancel(key: String) {
        val pending = PendingIntent.getBroadcast(
            context, 0, intentFor(key), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarms.cancel(pending)
        pending.cancel()
    }

    // The key goes into the data Uri, so every reminder gets its own PendingIntent identity.
    private fun intentFor(key: String): Intent =
        Intent(context, ReminderReceiver::class.java).setData(Uri.parse("stackpad://reminder/${Uri.encode(key)}"))
}
