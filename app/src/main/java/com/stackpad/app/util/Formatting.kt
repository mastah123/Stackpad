package com.stackpad.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())

/** `m:ss` for a duration in milliseconds. */
fun formatDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0) / 1000).toInt()
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

/** Locale-aware medium date, e.g. "Nov 6, 2026". */
fun formatDate(epochMillis: Long): String = dateFormatter.format(Instant.ofEpochMilli(epochMillis))
