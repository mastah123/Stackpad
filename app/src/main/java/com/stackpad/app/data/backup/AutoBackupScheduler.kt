package com.stackpad.app.data.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.stackpad.app.domain.backup.BackupFrequency
import java.util.concurrent.TimeUnit

/** Starts and stops the repeating background job for automatic backups. */
object AutoBackupScheduler {
    private const val WORK_NAME = "stackpad-auto-backup"

    fun schedule(context: Context, frequency: BackupFrequency) {
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(frequency.days, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
