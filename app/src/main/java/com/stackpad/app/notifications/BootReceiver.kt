package com.stackpad.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stackpad.app.StackpadApp

/** Alarms are cleared by a reboot or an app update, so recreate them. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val app = context.applicationContext as? StackpadApp ?: return
        val pending = goAsync()
        app.container.rescheduleReminders().invokeOnCompletion { pending.finish() }
    }
}
