package com.stackpad.app.domain.text

import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan

/**
 * Line headings inside a multi-line text block. A heading is a [SpanType.H1]..[SpanType.H3] span
 * that always covers its whole line; every line has at most one. Empty lines cannot be headings.
 */
object HeadingSpans {
    private val headingTypes = listOf(SpanType.H1, SpanType.H2, SpanType.H3)

    fun isHeading(type: SpanType): Boolean = type in headingTypes

    fun typeOf(level: TextLevel): SpanType? = when (level) {
        TextLevel.PARAGRAPH -> null
        TextLevel.H1 -> SpanType.H1
        TextLevel.H2 -> SpanType.H2
        TextLevel.H3 -> SpanType.H3
    }

    fun levelOf(type: SpanType): TextLevel = when (type) {
        SpanType.H1 -> TextLevel.H1
        SpanType.H2 -> TextLevel.H2
        SpanType.H3 -> TextLevel.H3
        else -> TextLevel.PARAGRAPH
    }

    /** `[start, end)` of every line (the line break itself is not part of a line). */
    fun lineBounds(text: String): List<Pair<Int, Int>> {
        val lines = ArrayList<Pair<Int, Int>>()
        var start = 0
        while (true) {
            val newline = text.indexOf('\n', start)
            val end = if (newline < 0) text.length else newline
            lines += start to end
            if (newline < 0) break
            start = end + 1
        }
        return lines
    }

    /** Index of the line holding [offset]; the end of a line (before its break) belongs to that line. */
    fun lineIndexAt(text: String, offset: Int): Int {
        val safe = offset.coerceIn(0, text.length)
        val bounds = lineBounds(text)
        return bounds.indexOfFirst { safe in it.first..it.second }.coerceAtLeast(0)
    }

    /** The heading level of each line: the heading span that covers most of it, else a paragraph. */
    fun lineLevels(text: String, spans: List<TextSpan>): List<TextLevel> {
        val headings = spans.filter { isHeading(it.type) }
        return lineBounds(text).map { (start, end) ->
            val best = headings
                .map { it to (minOf(end, it.end) - maxOf(start, it.start)) }
                .filter { it.second > 0 }
                .maxWithOrNull(compareBy({ it.second }, { -it.first.type.ordinal }))
            best?.let { levelOf(it.first.type) } ?: TextLevel.PARAGRAPH
        }
    }

    /** [spans] with the heading spans rebuilt so each line with a level has exactly one, covering the whole line. */
    fun withLevels(text: String, spans: List<TextSpan>, levels: List<TextLevel>): List<TextSpan> {
        val others = spans.filterNot { isHeading(it.type) }
        val headings = lineBounds(text).mapIndexedNotNull { index, (start, end) ->
            val type = typeOf(levels.getOrElse(index) { TextLevel.PARAGRAPH })
            if (type != null && end > start) TextSpan(start, end, type) else null
        }
        return others + headings
    }

    /** Makes every heading span cover its whole line (typed text joins the heading) and drops stray ones. */
    fun normalize(text: String, spans: List<TextSpan>): List<TextSpan> =
        if (spans.none { isHeading(it.type) }) spans else withLevels(text, spans, lineLevels(text, spans))

    /** The heading level of the line holding [offset]. */
    fun levelAt(text: String, spans: List<TextSpan>, offset: Int): TextLevel =
        lineLevels(text, spans).getOrElse(lineIndexAt(text, offset)) { TextLevel.PARAGRAPH }

    /** Line indexes touched by the selection `[from, to]`. */
    private fun touchedLines(text: String, from: Int, to: Int): IntRange {
        val first = lineIndexAt(text, from)
        val last = lineIndexAt(text, if (to > from) to - 1 else to)
        return first..maxOf(first, last)
    }

    /**
     * Toggles [level] on the lines touched by the selection: when every non-empty touched line
     * already has it the headings are removed, otherwise all of them get it.
     */
    fun toggleLevel(text: String, spans: List<TextSpan>, from: Int, to: Int, level: TextLevel): List<TextSpan> {
        val levels = lineLevels(text, spans).toMutableList()
        val bounds = lineBounds(text)
        val touched = touchedLines(text, from, to).filter { bounds[it].second > bounds[it].first }
        if (touched.isEmpty()) return spans
        val remove = touched.all { levels[it] == level }
        touched.forEach { levels[it] = if (remove) TextLevel.PARAGRAPH else level }
        return withLevels(text, spans, levels)
    }

    /** The level shared by all touched non-empty lines, or null when they differ or there are none. */
    fun commonLevel(text: String, spans: List<TextSpan>, from: Int, to: Int): TextLevel? {
        val levels = lineLevels(text, spans)
        val bounds = lineBounds(text)
        val shared = touchedLines(text, from, to)
            .filter { bounds[it].second > bounds[it].first }
            .map { levels[it] }
            .distinct()
        return shared.singleOrNull()
    }
}
