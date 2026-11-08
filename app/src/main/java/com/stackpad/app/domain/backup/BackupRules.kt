package com.stackpad.app.domain.backup

import java.time.Instant
import java.time.ZoneId

/** What to do with a note found in a backup. */
enum class MergeDecision { ADD, REPLACE, SKIP }

/** Format constants, limits and the merge rule for backup files. Pure, so it is unit tested. */
object BackupRules {
    const val FORMAT_VERSION = 1
    const val JSON_ENTRY = "backup.json"
    const val MEDIA_PREFIX = "media/"

    const val MAX_JSON_BYTES = 64L * 1024 * 1024
    const val MAX_MEDIA_BYTES = 200L * 1024 * 1024
    const val MAX_TOTAL_MEDIA_BYTES = 2L * 1024 * 1024 * 1024
    const val MAX_ENTRIES = 50_000
    private const val MAX_NAME_LENGTH = 100

    private val safeName = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")

    fun isSupportedVersion(version: Int): Boolean = version in 1..FORMAT_VERSION

    /** Only plain file names are accepted, so an entry can never write outside the media folder. */
    fun isSafeMediaName(name: String): Boolean =
        name.length <= MAX_NAME_LENGTH && safeName.matches(name) && !name.contains("..")

    fun mediaEntryName(fileName: String): String = MEDIA_PREFIX + fileName

    /** The media file name of a zip entry, or null if the entry is not media or its name is unsafe. */
    fun mediaNameOf(entryName: String): String? {
        if (!entryName.startsWith(MEDIA_PREFIX)) return null
        return entryName.removePrefix(MEDIA_PREFIX).takeIf(::isSafeMediaName)
    }

    /**
     * A note is added when it is new, replaces the local copy only when the backup's version is
     * strictly newer, and is otherwise skipped so restoring never overwrites newer local edits.
     */
    fun decide(existingUpdatedAt: Long?, incomingUpdatedAt: Long): MergeDecision = when {
        existingUpdatedAt == null -> MergeDecision.ADD
        incomingUpdatedAt > existingUpdatedAt -> MergeDecision.REPLACE
        else -> MergeDecision.SKIP
    }

    /** Suggested file name, e.g. `stackpad-backup-2026-11-08.zip`. */
    fun fileName(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        "stackpad-backup-${Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()}.zip"
}
