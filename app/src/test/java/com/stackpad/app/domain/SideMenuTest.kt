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

    @Test fun sectionsSplitPinnedFromRecentKeepingOrder() {
        val s = com.stackpad.app.domain.notes.NoteSections.of(listOf(note("a", false), note("b", true), note("c", false), note("d", true)))
        assertEquals(listOf("b", "d"), s.pinned.map { it.id })
        assertEquals(listOf("a", "c"), s.recent.map { it.id })
        assertTrue(com.stackpad.app.domain.notes.NoteSections.of(emptyList()).isEmpty)
    }
}

