package com.stackpad.app.domain

import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.text.SpanEditing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpanEditingTest {
    private val bold = SpanType.BOLD

    @Test
    fun insertionBeforeSpanShiftsIt() {
        val result = SpanEditing.adjustForEdit(listOf(TextSpan(2, 5, bold)), "hello world", "XXhello world")
        assertEquals(listOf(TextSpan(4, 7, bold)), result)
    }

    @Test
    fun insertionInsideSpanExtendsIt() {
        val result = SpanEditing.adjustForEdit(listOf(TextSpan(0, 5, bold)), "hello", "heXllo")
        assertEquals(listOf(TextSpan(0, 6, bold)), result)
    }

    @Test
    fun typingRightAfterSpanDoesNotExtendIt() {
        val result = SpanEditing.adjustForEdit(listOf(TextSpan(0, 5, bold)), "hello", "hello!")
        assertEquals(listOf(TextSpan(0, 5, bold)), result)
    }

    @Test
    fun deletingWholeSpanRemovesIt() {
        val result = SpanEditing.adjustForEdit(listOf(TextSpan(1, 3, bold)), "abcdef", "aef")
        assertTrue(result.isEmpty())
    }

    @Test
    fun deletionBeforeSpanShiftsItBack() {
        val result = SpanEditing.adjustForEdit(listOf(TextSpan(4, 6, bold)), "abcdef", "cdef")
        assertEquals(listOf(TextSpan(2, 4, bold)), result)
    }

    @Test
    fun unchangedTextKeepsSpans() {
        val spans = listOf(TextSpan(0, 2, bold))
        assertEquals(spans, SpanEditing.adjustForEdit(spans, "abc", "abc"))
    }

    @Test
    fun toggleAddsThenRemoves() {
        val added = SpanEditing.toggle(emptyList(), 0, 5, bold)
        assertEquals(listOf(TextSpan(0, 5, bold)), added)
        assertTrue(SpanEditing.toggle(added, 0, 5, bold).isEmpty())
    }

    @Test
    fun toggleMergesOverlappingRanges() {
        val result = SpanEditing.toggle(listOf(TextSpan(0, 3, bold)), 2, 6, bold)
        assertEquals(listOf(TextSpan(0, 6, bold)), result)
    }

    @Test
    fun toggleInsideSpanSplitsIt() {
        val result = SpanEditing.toggle(listOf(TextSpan(0, 10, bold)), 3, 6, bold)
        assertEquals(listOf(TextSpan(0, 3, bold), TextSpan(6, 10, bold)), result)
    }

    @Test
    fun differentTypesAreIndependent() {
        val spans = SpanEditing.toggle(listOf(TextSpan(0, 4, bold)), 0, 4, SpanType.ITALIC)
        assertTrue(SpanEditing.isFullyStyled(spans, 0, 4, bold))
        assertTrue(SpanEditing.isFullyStyled(spans, 0, 4, SpanType.ITALIC))
        assertFalse(SpanEditing.isFullyStyled(spans, 0, 4, SpanType.UNDERLINE))
    }

    @Test
    fun collapsedSelectionIsNeverStyled() {
        assertFalse(SpanEditing.isFullyStyled(listOf(TextSpan(0, 4, bold)), 2, 2, bold))
        assertEquals(emptyList<TextSpan>(), SpanEditing.toggle(emptyList(), 2, 2, bold))
    }
}
