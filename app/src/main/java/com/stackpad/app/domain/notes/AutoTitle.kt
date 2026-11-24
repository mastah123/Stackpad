package com.stackpad.app.domain.notes

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Default title for a note with no title: "Note" and the date and time it was created. */
object AutoTitle {
    private const val PATTERN = "dd-MM-yyyy hh:mm a"

    /** e.g. `Note 24-11-2026 04:09 PM`. */
    fun of(createdAt: Long, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        "Note " + DateTimeFormatter.ofPattern(PATTERN, locale).withZone(zone).format(Instant.ofEpochMilli(createdAt))

    /** [title] when it has text, otherwise the automatic title. */
    fun orDefault(title: String, createdAt: Long, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
        if (title.isBlank()) of(createdAt, zone, locale) else title
}
