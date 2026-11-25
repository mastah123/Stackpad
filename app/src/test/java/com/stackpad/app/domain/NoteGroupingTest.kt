package com.stackpad.app.domain

import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.domain.notes.ListOrder
import com.stackpad.app.domain.notes.NoteFilter
import com.stackpad.app.domain.notes.NoteGroup
import com.stackpad.app.domain.notes.NoteGrouping
import com.stackpad.app.domain.notes.NoteOrganizer
import com.stackpad.app.domain.notes.NoteSort
import com.stackpad.app.domain.notes.SectionLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class NoteGroupingTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 11, 25) // a Wednesday

    private fun millis(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun note(id: String, title: String = id, pinned: Boolean = false, edited: Long = 0, created: Long = 0) =
        NoteSummary(id = id, title = title, preview = "", pinned = pinned, createdAt = created, updatedAt = edited)

    private fun group(notes: List<NoteSummary>, order: ListOrder) =
        NoteGrouping.group(notes, order, zone, Locale.US, today)

    @Test fun defaultSplitsPinnedFromRecent() {
        val s = group(listOf(note("a"), note("b", pinned = true), note("c"), note("d", pinned = true)), ListOrder())
        assertEquals(listOf("pinned", "recent"), s.map { it.key })
        assertEquals(listOf("b", "d"), s[0].notes.map { it.id })
        assertEquals(listOf("a", "c"), s[1].notes.map { it.id })
        assertTrue(group(emptyList(), ListOrder()).isEmpty())
    }

    @Test fun noneIsOneSection() {
        val s = group(listOf(note("a"), note("b", pinned = true)), ListOrder(group = NoteGroup.NONE))
        assertEquals(1, s.size)
        assertEquals(SectionLabel.All, s[0].label)
    }

    @Test fun abcGroupsByFirstLetter() {
        val s = group(
            listOf(note("1", "apple"), note("2", "Avocado"), note("3", "banana"), note("4", "9 lives"), note("5", "")),
            ListOrder(group = NoteGroup.ABC),
        )
        assertEquals(listOf("A", "B", "#"), s.map { it.key })
        assertEquals(listOf("1", "2"), s[0].notes.map { it.id })
        assertEquals(listOf("4", "5"), s[2].notes.map { it.id })
    }

    @Test fun yearAndMonthUseTheSortDate() {
        val notes = listOf(
            note("a", edited = millis(2026, 11, 3), created = millis(2025, 1, 5)),
            note("b", edited = millis(2026, 11, 20), created = millis(2025, 6, 5)),
            note("c", edited = millis(2026, 2, 1), created = millis(2025, 6, 9)),
        )
        assertEquals(listOf("2026"), group(notes, ListOrder(group = NoteGroup.YEAR)).map { it.key })
        assertEquals(listOf("2025"), group(notes, ListOrder(sort = NoteSort.CREATED, group = NoteGroup.YEAR)).map { it.key })
        val months = group(notes, ListOrder(group = NoteGroup.MONTH))
        assertEquals(listOf(SectionLabel.Text("November 2026"), SectionLabel.Text("February 2026")), months.map { it.label })
        assertEquals(listOf("a", "b"), months[0].notes.map { it.id })
    }

    @Test fun weeksAreMondayToSunday() {
        val notes = listOf(
            note("a", edited = millis(2026, 11, 23)), // Monday of this week
            note("b", edited = millis(2026, 11, 22)), // Sunday, last week
            note("c", edited = millis(2026, 11, 10)),
        )
        val s = group(notes, ListOrder(group = NoteGroup.WEEK))
        assertEquals(listOf(SectionLabel.ThisWeek, SectionLabel.LastWeek, SectionLabel.Text("Nov 9 – Nov 15")), s.map { it.label })
    }

    @Test fun reversedFlipsTheFieldButPinnedStayOnTop() {
        val notes = listOf(note("old", edited = 1), note("new", edited = 3), note("pin", pinned = true, edited = 0))
        assertEquals(listOf("pin", "new", "old"), NoteOrganizer.organize(notes, NoteSort.LAST_EDITED, NoteFilter()).map { it.id })
        assertEquals(listOf("pin", "old", "new"), NoteOrganizer.organize(notes, NoteSort.LAST_EDITED, NoteFilter(), reversed = true).map { it.id })
    }

    @Test fun reversedTitleKeepsUntitledLast() {
        val notes = listOf(note("a", "apple"), note("z", "zebra"), note("x", ""))
        assertEquals(listOf("z", "a", "x"), NoteOrganizer.organize(notes, NoteSort.TITLE, NoteFilter(), reversed = true).map { it.id })
    }
}
