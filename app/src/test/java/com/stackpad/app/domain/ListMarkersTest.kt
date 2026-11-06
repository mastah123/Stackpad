package com.stackpad.app.domain

import com.stackpad.app.data.model.ListKind
import com.stackpad.app.domain.text.ListMarkers
import org.junit.Assert.assertEquals
import org.junit.Test

class ListMarkersTest {
    @Test
    fun lettersRunAThroughZThenDouble() {
        assertEquals("a", ListMarkers.letters(0))
        assertEquals("z", ListMarkers.letters(25))
        assertEquals("aa", ListMarkers.letters(26))
        assertEquals("ab", ListMarkers.letters(27))
        assertEquals("zz", ListMarkers.letters(701))
        assertEquals("aaa", ListMarkers.letters(702))
    }

    @Test
    fun labelsMatchTheListKind() {
        assertEquals("•", ListMarkers.label(ListKind.BULLET, 5))
        assertEquals("1.", ListMarkers.label(ListKind.NUMBERED, 0))
        assertEquals("10.", ListMarkers.label(ListKind.NUMBERED, 9))
        assertEquals("c.", ListMarkers.label(ListKind.LETTERED, 2))
    }
}
