package com.stackpad.app.domain.text

import com.stackpad.app.data.model.LineSpacing
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextFont
import com.stackpad.app.data.model.TextSpan

/**
 * Per-letter font and highlight, and per-line spacing, stored as spans next to bold and italic.
 * Font and highlight are "pick one" groups: choosing one replaces the other of the same group.
 */
object FormatSpans {
    const val HIGHLIGHT_COUNT = 6

    val FONTS = listOf(SpanType.FONT_SERIF, SpanType.FONT_MONO, SpanType.FONT_HANDWRITING)
    val HIGHLIGHTS = listOf(
        SpanType.HIGHLIGHT_1, SpanType.HIGHLIGHT_2, SpanType.HIGHLIGHT_3,
        SpanType.HIGHLIGHT_4, SpanType.HIGHLIGHT_5, SpanType.HIGHLIGHT_6,
    )
    val LINES = listOf(SpanType.LINE_TIGHT, SpanType.LINE_RELAXED, SpanType.LINE_LOOSE)

    fun isLine(type: SpanType): Boolean = type in LINES

    fun fontType(font: TextFont): SpanType? = when (font) {
        TextFont.DEFAULT -> null
        TextFont.SERIF -> SpanType.FONT_SERIF
        TextFont.MONO -> SpanType.FONT_MONO
        TextFont.HANDWRITING -> SpanType.FONT_HANDWRITING
    }

    fun fontOf(type: SpanType?): TextFont = when (type) {
        SpanType.FONT_SERIF -> TextFont.SERIF
        SpanType.FONT_MONO -> TextFont.MONO
        SpanType.FONT_HANDWRITING -> TextFont.HANDWRITING
        else -> TextFont.DEFAULT
    }

    /** [index] is 1..[HIGHLIGHT_COUNT]; 0 (or anything else) means no highlight. */
    fun highlightType(index: Int): SpanType? = HIGHLIGHTS.getOrNull(index - 1)

    fun highlightIndex(type: SpanType?): Int = HIGHLIGHTS.indexOf(type) + 1

    fun lineType(spacing: LineSpacing): SpanType? = when (spacing) {
        LineSpacing.TIGHT -> SpanType.LINE_TIGHT
        LineSpacing.NORMAL -> null
        LineSpacing.RELAXED -> SpanType.LINE_RELAXED
        LineSpacing.LOOSE -> SpanType.LINE_LOOSE
    }

    fun spacingOf(type: SpanType?): LineSpacing = when (type) {
        SpanType.LINE_TIGHT -> LineSpacing.TIGHT
        SpanType.LINE_RELAXED -> LineSpacing.RELAXED
        SpanType.LINE_LOOSE -> LineSpacing.LOOSE
        else -> LineSpacing.NORMAL
    }

    /** Makes `[start, end)` carry exactly [chosen] from [group] (null = none of them). */
    fun setInline(spans: List<TextSpan>, start: Int, end: Int, group: List<SpanType>, chosen: SpanType?): List<TextSpan> {
        if (start >= end) return spans
        var result = spans
        group.forEach { result = SpanEditing.withCoverage(result, start, end, it, on = false) }
        if (chosen != null) result = SpanEditing.withCoverage(result, start, end, chosen, on = true)
        return result
    }

    /**
     * What `[start, end)` has from [group]: the one type covering all of it, `null` for none of
     * the range, or [MIXED] when it differs along the range.
     */
    fun commonInline(spans: List<TextSpan>, start: Int, end: Int, group: List<SpanType>): Any? {
        if (start >= end) return null
        val full = group.filter { SpanEditing.isFullyStyled(spans, start, end, it) }
        if (full.size == 1) return full.single()
        val touched = spans.any { it.type in group && it.start < end && it.end > start }
        return if (touched) MIXED else null
    }

    /** Marker returned by [commonInline] when the range holds more than one choice. */
    val MIXED = Any()

    /** The spacing type of each line: the line span covering most of it, or null. */
    fun lineTypes(text: String, spans: List<TextSpan>): List<SpanType?> {
        val lines = spans.filter { isLine(it.type) }
        return HeadingSpans.lineBounds(text).map { (start, end) ->
            lines.map { it to (minOf(end, it.end) - maxOf(start, it.start)) }
                .filter { it.second > 0 }
                .maxByOrNull { it.second }?.first?.type
        }
    }

    private fun withLineTypes(text: String, spans: List<TextSpan>, types: List<SpanType?>): List<TextSpan> {
        val others = spans.filterNot { isLine(it.type) }
        val lines = HeadingSpans.lineBounds(text).mapIndexedNotNull { index, (start, end) ->
            val type = types.getOrNull(index)
            if (type != null && end > start) TextSpan(start, end, type) else null
        }
        return others + lines
    }

    /** Makes every spacing span cover its whole line (typed text joins it) and drops stray ones. */
    fun normalizeLines(text: String, spans: List<TextSpan>): List<TextSpan> =
        if (spans.none { isLine(it.type) }) spans else withLineTypes(text, spans, lineTypes(text, spans))

    /** Line indexes touched by the selection `[from, to]`, skipping empty lines. */
    private fun touched(text: String, from: Int, to: Int): List<Int> {
        val bounds = HeadingSpans.lineBounds(text)
        val first = HeadingSpans.lineIndexAt(text, from)
        val last = maxOf(first, HeadingSpans.lineIndexAt(text, if (to > from) to - 1 else to))
        return (first..last).filter { bounds[it].second > bounds[it].first }
    }

    /** Gives every non-empty line the selection touches the spacing [chosen] (null = normal). */
    fun setLines(text: String, spans: List<TextSpan>, from: Int, to: Int, chosen: SpanType?): List<TextSpan> {
        val lines = touched(text, from, to)
        if (lines.isEmpty()) return spans
        val types = lineTypes(text, spans).toMutableList()
        lines.forEach { types[it] = chosen }
        return withLineTypes(text, spans, types)
    }

    /** The spacing shared by the touched lines, or null when they differ (or there are none). */
    fun commonLine(text: String, spans: List<TextSpan>, from: Int, to: Int): LineSpacing? {
        val lines = touched(text, from, to)
        if (lines.isEmpty()) return LineSpacing.NORMAL
        val types = lineTypes(text, spans)
        return lines.map { spacingOf(types[it]) }.distinct().singleOrNull()
    }
}
