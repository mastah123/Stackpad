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

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault())

/** Locale-aware date and time, e.g. "Nov 6, 2026, 9:31 PM". */
fun formatDateTime(epochMillis: Long): String = dateTimeFormatter.format(Instant.ofEpochMilli(epochMillis))

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())

/** Time of day for something edited today (e.g. "6:41 PM"), otherwise the date. Used on note rows. */
fun formatListStamp(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val zone = ZoneId.systemDefault()
    val day = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return if (day == today) timeFormatter.format(Instant.ofEpochMilli(epochMillis)) else formatDate(epochMillis)
}

private val stampFormatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")

/** Full date and time, e.g. `23-11-2026 06:29 PM`. Used on the note options sheet. */
fun formatStamp(epochMillis: Long): String =
    stampFormatter.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis))
