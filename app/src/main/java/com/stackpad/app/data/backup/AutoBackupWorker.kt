package com.stackpad.app.data.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stackpad.app.StackpadApp

/** Background job that runs the automatic backup. A failed run is recorded and retried next time. */
class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as StackpadApp).container
        container.autoBackup.runScheduled()
        return Result.success()
    }
}
