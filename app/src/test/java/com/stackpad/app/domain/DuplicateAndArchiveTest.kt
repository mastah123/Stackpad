package com.stackpad.app.domain

import com.stackpad.app.domain.notes.AutoTitle
import com.stackpad.app.domain.notes.DuplicateTitle
import com.stackpad.app.domain.notes.MenuCounts
import org.junit.Assert.assertEquals
import org.junit.Test

class DuplicateAndArchiveTest {
    @Test fun duplicateKeepsTheTitleAndAddsCopy() {
        assertEquals("Groceries (copy)", DuplicateTitle.of("Groceries", 0L))
        assertEquals("Groceries (copy)", DuplicateTitle.of("Groceries  ", 0L))
    }

    @Test fun duplicateOfAnUntitledNoteGetsTheAutomaticTitle() {
        assertEquals(AutoTitle.of(1_000L), DuplicateTitle.of("  ", 1_000L))
    }

    @Test fun menuCountsIncludeTheArchive() {
        assertEquals(MenuCounts(all = 2, pinned = 1, trashed = 3, reminders = 4, archived = 5), MenuCounts.of(listOf(true, false), 3, 4, 5))
        assertEquals(0, MenuCounts.of(emptyList(), 0).archived)
    }
}
