package com.stackpad.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.stackpad.app.MainActivity
import com.stackpad.app.R

/** Builds and shows the notification for a checklist due date. */
object ReminderNotifications {
    const val EXTRA_NOTE_ID = "com.stackpad.app.extra.NOTE_ID"
    const val EXTRA_KEY = "com.stackpad.app.extra.KEY"
    const val EXTRA_NOTE_TITLE = "com.stackpad.app.extra.NOTE_TITLE"
    const val EXTRA_ITEM_TEXT = "com.stackpad.app.extra.ITEM_TEXT"

    private const val CHANNEL_ID = "due_reminders"

    /** True when the app may post notifications (permission granted and not switched off). */
    fun canNotify(context: Context): Boolean {
        val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permitted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    @SuppressLint("MissingPermission") // checked in canNotify
    fun show(context: Context, key: String, noteId: String, noteTitle: String, itemText: String) {
        if (!canNotify(context)) return
        ensureChannel(context)

        val open = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_NOTE_ID, noteId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentIntent = PendingIntent.getActivity(
            context,
            key.hashCode(),
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = itemText.ifBlank { context.getString(R.string.reminder_untitled_item) }
        val body = if (noteTitle.isBlank()) {
            context.getString(R.string.reminder_due_today)
        } else {
            context.getString(R.string.reminder_due_today_in, noteTitle)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(key.hashCode(), notification) }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = context.getString(R.string.reminder_channel_description) },
        )
    }
}
