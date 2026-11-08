package com.stackpad.app.domain

import com.stackpad.app.domain.reminders.DueDates
import com.stackpad.app.domain.reminders.DueStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class DueDatesTest {
    private val today = LocalDate.of(2026, 11, 8)

    @Test
    fun parseAcceptsIsoDatesOnly() {
        assertEquals(LocalDate.of(2026, 11, 10), DueDates.parse("2026-11-10"))
        assertNull(DueDates.parse("11/10/2026"))
        assertNull(DueDates.parse(""))
        assertNull(DueDates.parse(null))
    }

    @Test
    fun pickerMillisRoundTrip() {
        val millis = DueDates.toPickerMillis("2026-11-10")!!
        assertEquals("2026-11-10", DueDates.fromPickerMillis(millis))
        assertEquals("2026-11-10", DueDates.fromPickerMillis(millis + 12 * 3_600_000L))
        assertNull(DueDates.toPickerMillis("nope"))
    }

    @Test
    fun statusRelativeToToday() {
        assertEquals(DueStatus.OVERDUE, DueDates.status(today.minusDays(1), today))
        assertEquals(DueStatus.TODAY, DueDates.status(today, today))
        assertEquals(DueStatus.TOMORROW, DueDates.status(today.plusDays(1), today))
        assertEquals(DueStatus.LATER, DueDates.status(today.plusDays(2), today))
    }

    @Test
    fun shortLabelAddsYearOnlyWhenDifferent() {
        assertEquals("Nov 20", DueDates.shortLabel(LocalDate.of(2026, 11, 20), today, Locale.US))
        assertEquals("Jan 5, 2027", DueDates.shortLabel(LocalDate.of(2027, 1, 5), today, Locale.US))
    }

    @Test
    fun reminderFiresAtNineLocalTime() {
        val zone = ZoneId.of("Asia/Manila")
        val expected = LocalDate.of(2026, 11, 10).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        assertEquals(expected, DueDates.reminderTime(LocalDate.of(2026, 11, 10), zone))
        val newYork = ZoneId.of("America/New_York")
        val other = LocalDate.of(2026, 11, 10).atTime(9, 0).atZone(newYork).toInstant().toEpochMilli()
        assertEquals(other, DueDates.reminderTime(LocalDate.of(2026, 11, 10), newYork))
    }
}
