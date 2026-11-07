package com.stackpad.app.domain

import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.domain.notes.Tags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagsTest {
    @Test
    fun normalizeCleansInput() {
        assertEquals("work", Tags.normalize("  #Work "))
        assertEquals("to-do-list", Tags.normalize("To Do  list"))
        assertEquals("a-b", Tags.normalize("a,b"))
        assertEquals("x".repeat(Tags.MAX_LENGTH), Tags.normalize("x".repeat(60)))
    }

    @Test
    fun normalizeRejectsEmpty() {
        assertNull(Tags.normalize(""))
        assertNull(Tags.normalize("  # "))
        assertNull(Tags.normalize("---"))
    }

    @Test
    fun addSkipsDuplicatesAndInvalid() {
        val once = Tags.add(emptyList(), "Work")
        assertEquals(listOf("work"), once)
        assertEquals(once, Tags.add(once, "#WORK"))
        assertEquals(once, Tags.add(once, " "))
    }

    @Test
    fun addStopsAtLimit() {
        var tags = emptyList<String>()
        repeat(Tags.MAX_PER_NOTE + 3) { tags = Tags.add(tags, "t$it") }
        assertEquals(Tags.MAX_PER_NOTE, tags.size)
    }

    @Test
    fun serializeRoundTrips() {
        val tags = listOf("a", "b-c", "d")
        assertEquals(tags, Tags.parse(Tags.serialize(tags)))
        assertEquals(emptyList<String>(), Tags.parse(""))
    }

    @Test
    fun countSortsByUsageThenName() {
        val counts = Tags.count(listOf(listOf("b", "a"), listOf("b"), listOf("c", "a"), listOf("b")))
        assertEquals(listOf(TagCount("b", 3), TagCount("a", 2), TagCount("c", 1)), counts)
    }
}
