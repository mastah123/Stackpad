package com.stackpad.app.domain

import com.stackpad.app.domain.notes.MenuCounts
import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.domain.notes.TagList
import com.stackpad.app.domain.notes.TagSort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SideMenuTest {
    private val tags = listOf(TagCount("work", 5), TagCount("Home", 2), TagCount("homework", 5), TagCount("misc", 1))

    @Test fun countsAreDerivedFromPinnedFlags() {
        assertEquals(MenuCounts(all = 3, pinned = 2, trashed = 4), MenuCounts.of(listOf(true, false, true), 4))
        assertEquals(MenuCounts(), MenuCounts.of(emptyList(), 0))
    }

    @Test fun nameSortIgnoresCase() {
        assertEquals(listOf("Home", "homework", "misc", "work"), TagList.visible(tags, "", TagSort.NAME).map { it.tag })
    }

    @Test fun countSortIsDescendingThenName() {
        assertEquals(listOf("homework", "work", "Home", "misc"), TagList.visible(tags, "", TagSort.COUNT).map { it.tag })
    }

    @Test fun filterMatchesSubstringIgnoringCaseAndSpaces() {
        assertEquals(listOf("Home", "homework"), TagList.visible(tags, "  OM", TagSort.NAME).map { it.tag })
        assertTrue(TagList.visible(tags, "zzz", TagSort.NAME).isEmpty())
    }

    private fun note(id: String, pinned: Boolean) =
        com.stackpad.app.data.model.NoteSummary(id = id, title = id, preview = "", pinned = pinned, createdAt = 0, updatedAt = 0)
}

class AutoTitleAndRemindersTest {
    @Test fun autoTitleUsesCreationTime() {
        // 2026-11-24 08:09 UTC
        val t = java.time.ZonedDateTime.of(2026, 11, 24, 16, 9, 0, 0, java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("Note 24-11-2026 04:09 PM", com.stackpad.app.domain.notes.AutoTitle.of(t, java.time.ZoneOffset.UTC, java.util.Locale.US))
        assertEquals("Mine", com.stackpad.app.domain.notes.AutoTitle.orDefault("Mine", t, java.time.ZoneOffset.UTC, java.util.Locale.US))
        assertEquals("Note 24-11-2026 04:09 PM", com.stackpad.app.domain.notes.AutoTitle.orDefault("  ", t, java.time.ZoneOffset.UTC, java.util.Locale.US))
    }

    @Test fun remindersListUncheckedDatedItemsEarliestFirst() {
        val items = listOf(
            com.stackpad.app.data.model.ChecklistItem("1", "late", false, "2026-12-01"),
            com.stackpad.app.data.model.ChecklistItem("2", "done", true, "2026-11-01"),
            com.stackpad.app.data.model.ChecklistItem("3", "none", false, null),
            com.stackpad.app.data.model.ChecklistItem("4", "soon", false, "2026-11-10"),
        )
        val block = com.stackpad.app.data.model.ChecklistBlock(id = "b", items = items)
        val entries = com.stackpad.app.domain.reminders.ReminderList.sorted(
            com.stackpad.app.domain.reminders.ReminderList.entriesOf("n", "Note", listOf(block)),
        )
        assertEquals(listOf("soon", "late"), entries.map { it.itemText })
    }
}
