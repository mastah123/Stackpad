package com.stackpad.app.domain.history

/** When an earlier version of a note is kept, and how many are kept. */
object HistoryPolicy {
    /** At most one automatic version per this many milliseconds of editing. */
    const val INTERVAL_MS = 10 * 60 * 1000L

    const val MAX_VERSIONS = 20

    /**
     * True when the note's previous state should be kept as a version because it is about to be
     * overwritten: the content changed, it was not empty, and the newest version is old enough
     * (or there is none yet).
     */
    fun shouldCapture(latestCapturedAt: Long?, now: Long, contentChanged: Boolean, previousIsBlank: Boolean): Boolean =
        contentChanged && !previousIsBlank && (latestCapturedAt == null || now - latestCapturedAt >= INTERVAL_MS)

    /** Ids to delete so only the newest [max] remain; [idsNewestFirst] must be sorted newest first. */
    fun idsToTrim(idsNewestFirst: List<Long>, max: Int = MAX_VERSIONS): List<Long> = idsNewestFirst.drop(max)
}
