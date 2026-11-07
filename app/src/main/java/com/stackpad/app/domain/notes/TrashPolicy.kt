package com.stackpad.app.domain.notes

import java.util.concurrent.TimeUnit

/** How long deleted notes stay in the trash before they are erased for good. */
object TrashPolicy {
    const val RETENTION_DAYS = 30
    private val dayMs = TimeUnit.DAYS.toMillis(1)

    /** Notes trashed at or before this moment are expired. */
    fun cutoff(now: Long): Long = now - RETENTION_DAYS * dayMs

    /** Whole days left before the note is erased (rounded up, never negative). */
    fun daysLeft(trashedAt: Long, now: Long): Int {
        val remaining = trashedAt + RETENTION_DAYS * dayMs - now
        if (remaining <= 0) return 0
        return ((remaining + dayMs - 1) / dayMs).toInt()
    }
}
