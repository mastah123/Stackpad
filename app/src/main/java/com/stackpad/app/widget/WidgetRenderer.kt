package com.stackpad.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.stackpad.app.MainActivity
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteSummary

/** What the widget shows. The note title is hidden while the app lock is on. */
sealed interface WidgetContent {
    data object Empty : WidgetContent
    data object Locked : WidgetContent
    data class Latest(val note: NoteSummary) : WidgetContent
}

/** Builds the widget's views and pushes them to every placed widget. */
object WidgetRenderer {
    private const val REQUEST_OPEN = 1
    private const val REQUEST_NEW = 2

    fun render(context: Context, content: WidgetContent): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_stackpad)
        val openIntent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        when (content) {
            WidgetContent.Empty -> {
                views.setTextViewText(R.id.widget_label, context.getString(R.string.app_name))
                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_empty))
            }
            WidgetContent.Locked -> {
                views.setTextViewText(R.id.widget_label, context.getString(R.string.app_name))
                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_locked))
            }
            is WidgetContent.Latest -> {
                views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_latest_label))
                // A locked note's title stays hidden, like its content.
                views.setTextViewText(
                    R.id.widget_title,
                    if (content.note.locked) {
                        context.getString(R.string.widget_locked_note)
                    } else {
                        content.note.title.ifBlank { content.note.preview.ifBlank { context.getString(R.string.note_untitled) } }
                    },
                )
                openIntent.putExtra(MainActivity.EXTRA_NOTE_ID, content.note.id)
            }
        }
        views.setOnClickPendingIntent(R.id.widget_root, activity(context, REQUEST_OPEN, openIntent))

        val newIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(MainActivity.EXTRA_NEW_NOTE, true)
        views.setOnClickPendingIntent(R.id.widget_new_note, activity(context, REQUEST_NEW, newIntent))
        return views
    }

    fun push(context: Context, content: WidgetContent) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, StackpadWidgetProvider::class.java))
        if (ids.isEmpty()) return
        manager.updateAppWidget(ids, render(context, content))
    }

    private fun activity(context: Context, requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
