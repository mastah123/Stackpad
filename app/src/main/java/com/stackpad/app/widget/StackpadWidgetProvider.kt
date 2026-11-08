package com.stackpad.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.stackpad.app.StackpadApp

/** The home-screen widget: a new-note button and the most recently edited note. */
class StackpadWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        (context.applicationContext as StackpadApp).container.refreshWidget { pending.finish() }
    }
}
