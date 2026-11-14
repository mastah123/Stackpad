package com.stackpad.app.domain

import com.stackpad.app.domain.search.SearchQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryTest {
    @Test fun splitsAndDeduplicatesWords() {
        assertEquals(listOf("buy", "MILK"), SearchQuery.words("  buy   MILK  Buy "))
        assertEquals(emptyList<String>(), SearchQuery.words("   "))
    }

    @Test fun capsTheNumberOfWords() {
        assertEquals(SearchQuery.MAX_WORDS, SearchQuery.words("a b c d e f g h i j k").size)
    }

    @Test fun allWordsMustMatchInAnyOrder() {
        val w = SearchQuery.words("milk buy")
        assertTrue(SearchQuery.matches(w, "Shopping", "Buy some milk", emptyList()))
        assertFalse(SearchQuery.matches(w, "Shopping", "Buy some eggs", emptyList()))
    }

    @Test fun wordsMaySpanTitleContentAndTags() {
        val w = SearchQuery.words("plan urgent trip")
        assertTrue(SearchQuery.matches(w, "Trip", "make a plan", listOf("urgent")))
    }

    @Test fun matchingIgnoresCase() {
        assertTrue(SearchQuery.matches(SearchQuery.words("MILK"), "", "some milk", emptyList()))
    }

    @Test fun blankQueryMatchesEverything() {
        assertTrue(SearchQuery.matches(emptyList(), "", "", emptyList()))
    }

    @Test fun rangesFindAllOccurrences() {
        assertEquals(listOf(0..1, 6..7), SearchQuery.ranges("Ab cd Ab", listOf("ab")))
    }

    @Test fun rangesMergeOverlapsAndTouching() {
        // "foo" at 0..2 and "oob" at 1..3 overlap; "ar" at 4..5 touches 3.
        assertEquals(listOf(0..5), SearchQuery.ranges("foobar", listOf("foo", "oob", "ar")))
    }

    @Test fun rangesEmptyWhenNothingMatches() {
        assertEquals(emptyList<IntRange>(), SearchQuery.ranges("hello", listOf("xyz")))
    }

    @Test fun shortContentIsKeptWhole() {
        assertEquals("a b c", SearchQuery.snippet("a\n b\tc", listOf("c")))
    }

    @Test fun snippetStartsAtTheBeginningWhenTheMatchIsEarly() {
        val text = "needle " + "x".repeat(300)
        val snip = SearchQuery.snippet(text, listOf("needle"))
        assertTrue(snip.startsWith("needle"))
        assertEquals(SearchQuery.SNIPPET_LENGTH, snip.length)
    }

    @Test fun snippetWindowsAroundALateMatch() {
        val text = "a".repeat(300) + " needle " + "b".repeat(300)
        val snip = SearchQuery.snippet(text, listOf("needle"))
        assertTrue(snip.contains("needle"))
        assertTrue(snip.startsWith("…"))
        assertTrue(snip.endsWith("…"))
    }

    @Test fun snippetWithoutMatchFallsBackToTheStart() {
        val text = "z".repeat(300)
        assertEquals("z".repeat(SearchQuery.SNIPPET_LENGTH), SearchQuery.snippet(text, listOf("needle")))
    }
}
