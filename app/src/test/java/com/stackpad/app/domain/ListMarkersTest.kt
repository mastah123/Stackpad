package com.stackpad.app.domain

import com.stackpad.app.data.model.ListKind
import com.stackpad.app.domain.text.ListMarkers
import com.stackpad.app.domain.text.isBullet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListMarkersTest {
    private fun labels(kind: ListKind, n: Int) = (0 until n).map { ListMarkers.label(kind, it) }

    @Test fun bulletsUseTheirSymbol() {
        assertEquals(listOf("•"), labels(ListKind.BULLET, 1))
        assertEquals(listOf("○"), labels(ListKind.BULLET_HOLLOW, 1))
        assertEquals(listOf("▪"), labels(ListKind.BULLET_SQUARE, 1))
        assertTrue(ListKind.BULLET_HOLLOW.isBullet)
        assertFalse(ListKind.NUMBERED.isBullet)
    }

    @Test fun numbersAndLetters() {
        assertEquals(listOf("1.", "2.", "3."), labels(ListKind.NUMBERED, 3))
        assertEquals(listOf("A.", "B.", "C."), labels(ListKind.UPPER_ALPHA, 3))
        assertEquals(listOf("a.", "b."), labels(ListKind.LETTERED, 2))
        assertEquals("aa.", ListMarkers.label(ListKind.LETTERED, 26))
        assertEquals("AA.", ListMarkers.label(ListKind.UPPER_ALPHA, 26))
    }

    @Test fun romanNumerals() {
        assertEquals(listOf("i.", "ii.", "iii.", "iv.", "v.", "ix."), listOf(0, 1, 2, 3, 4, 8).map { ListMarkers.label(ListKind.LOWER_ROMAN, it) })
        assertEquals("XIV.", ListMarkers.label(ListKind.UPPER_ROMAN, 13))
        assertEquals("MCMXCIV.", ListMarkers.label(ListKind.UPPER_ROMAN, 1993))
        assertEquals("4000", ListMarkers.roman(4000))
    }

    @Test fun greekLetters() {
        assertEquals(listOf("α.", "β.", "γ."), labels(ListKind.GREEK, 3))
        assertEquals("ω.", ListMarkers.label(ListKind.GREEK, 23))
        assertEquals("αα.", ListMarkers.label(ListKind.GREEK, 24))
    }

    @Test fun lettersRunAThroughZThenDouble() {
        assertEquals("a", ListMarkers.letters(0))
        assertEquals("z", ListMarkers.letters(25))
        assertEquals("aa", ListMarkers.letters(26))
        assertEquals("ab", ListMarkers.letters(27))
        assertEquals("zz", ListMarkers.letters(701))
        assertEquals("aaa", ListMarkers.letters(702))
    }

    @Test fun labelsMatchTheListKind() {
        assertEquals("•", ListMarkers.label(ListKind.BULLET, 5))
        assertEquals("1.", ListMarkers.label(ListKind.NUMBERED, 0))
        assertEquals("10.", ListMarkers.label(ListKind.NUMBERED, 9))
        assertEquals("c.", ListMarkers.label(ListKind.LETTERED, 2))
    }

    @Test fun checkboxMarkerShowsTheTickState() {
        assertEquals("[ ]", ListMarkers.label(ListKind.CHECKBOX, 0))
        assertEquals("[x]", ListMarkers.label(ListKind.CHECKBOX, 3, checked = true))
        assertFalse(ListKind.CHECKBOX.isBullet)
    }
}
