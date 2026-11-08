package com.stackpad.app.domain.reminders

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Where a due date sits relative to today. */
enum class DueStatus { OVERDUE, TODAY, TOMORROW, LATER }

/**
 * Due dates are stored as ISO local dates (`2026-11-10`) so they do not shift with time zones.
 * Reminders fire at [REMINDER_HOUR]:00 local time on that day.
 */
object DueDates {
    const val REMINDER_HOUR = 9

    fun parse(iso: String?): LocalDate? =
        iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** Material date pickers report the chosen day as midnight UTC; this turns it into a date. */
    fun fromPickerMillis(utcMillis: Long): String =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()

    /** Inverse of [fromPickerMillis], used to preselect the saved date in the picker. */
    fun toPickerMillis(iso: String): Long? =
        parse(iso)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

    fun status(date: LocalDate, today: LocalDate): DueStatus = when {
        date.isBefore(today) -> DueStatus.OVERDUE
        date == today -> DueStatus.TODAY
        date == today.plusDays(1) -> DueStatus.TOMORROW
        else -> DueStatus.LATER
    }

    /** Short text such as `Nov 10`, with the year added when it is not the current year. */
    fun shortLabel(date: LocalDate, today: LocalDate, locale: Locale = Locale.getDefault()): String {
        val pattern = if (date.year == today.year) "MMM d" else "MMM d, yyyy"
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    /** Epoch millis of the reminder for [date] in [zone]. */
    fun reminderTime(date: LocalDate, zone: ZoneId): Long =
        date.atTime(REMINDER_HOUR, 0).atZone(zone).toInstant().toEpochMilli()
}
