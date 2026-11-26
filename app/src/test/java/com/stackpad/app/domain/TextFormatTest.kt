package com.stackpad.app.domain

import com.stackpad.app.data.model.LineSpacing
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextFont
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.text.FormatSpans
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TextFormatTest {
    @Test
    fun fontAppliesOnlyToSelectedLetters() {
        val spans = FormatSpans.setInline(emptyList(), 2, 5, FormatSpans.FONTS, SpanType.FONT_SERIF)
        assertEquals(listOf(TextSpan(2, 5, SpanType.FONT_SERIF)), spans)
    }

    @Test
    fun newFontReplacesOldOneInThatRange() {
        val first = FormatSpans.setInline(emptyList(), 0, 6, FormatSpans.FONTS, SpanType.FONT_SERIF)
        val second = FormatSpans.setInline(first, 2, 4, FormatSpans.FONTS, SpanType.FONT_MONO)
        assertEquals(
            listOf(
                TextSpan(0, 2, SpanType.FONT_SERIF),
                TextSpan(2, 4, SpanType.FONT_MONO),
                TextSpan(4, 6, SpanType.FONT_SERIF),
            ),
            second.sortedBy { it.start },
        )
    }

    @Test
    fun defaultFontAndNoHighlightRemoveSpans() {
        val spans = listOf(TextSpan(0, 4, SpanType.HIGHLIGHT_2), TextSpan(0, 4, SpanType.BOLD))
        val cleared = FormatSpans.setInline(spans, 0, 4, FormatSpans.HIGHLIGHTS, null)
        assertEquals(listOf(TextSpan(0, 4, SpanType.BOLD)), cleared)
    }

    @Test
    fun commonInlineReportsNoneOneOrMixed() {
        val spans = listOf(TextSpan(0, 3, SpanType.FONT_SERIF), TextSpan(3, 6, SpanType.FONT_MONO))
        assertEquals(SpanType.FONT_SERIF, FormatSpans.commonInline(spans, 0, 3, FormatSpans.FONTS))
        assertSame(FormatSpans.MIXED, FormatSpans.commonInline(spans, 0, 6, FormatSpans.FONTS))
        assertEquals(null, FormatSpans.commonInline(spans, 7, 9, FormatSpans.FONTS))
    }

    @Test
    fun lineSpacingCoversWholeTouchedLinesOnly() {
        val text = "one\ntwo\nthree"
        val spans = FormatSpans.setLines(text, emptyList(), 5, 6, SpanType.LINE_LOOSE)
        assertEquals(listOf(TextSpan(4, 7, SpanType.LINE_LOOSE)), spans)
        assertEquals(LineSpacing.LOOSE, FormatSpans.commonLine(text, spans, 5, 5))
        assertEquals(LineSpacing.NORMAL, FormatSpans.commonLine(text, spans, 0, 0))
        assertEquals(null, FormatSpans.commonLine(text, spans, 0, 6))
    }

    @Test
    fun normalSpacingRemovesLineSpan() {
        val text = "one\ntwo"
        val spans = FormatSpans.setLines(text, emptyList(), 0, 7, SpanType.LINE_TIGHT)
        assertEquals(2, spans.size)
        assertEquals(emptyList<TextSpan>(), FormatSpans.setLines(text, spans, 0, 7, null))
    }

    @Test
    fun typedTextJoinsItsLine() {
        val spans = FormatSpans.normalizeLines("hello!\nx", listOf(TextSpan(0, 5, SpanType.LINE_RELAXED)))
        assertEquals(listOf(TextSpan(0, 6, SpanType.LINE_RELAXED)), spans)
    }

    @Test
    fun fontAndHighlightMappingsRoundTrip() {
        TextFont.values().forEach { assertEquals(it, FormatSpans.fontOf(FormatSpans.fontType(it))) }
        (0..FormatSpans.HIGHLIGHT_COUNT).forEach { assertEquals(it, FormatSpans.highlightIndex(FormatSpans.highlightType(it))) }
        LineSpacing.values().forEach { assertEquals(it, FormatSpans.spacingOf(FormatSpans.lineType(it))) }
    }
}
