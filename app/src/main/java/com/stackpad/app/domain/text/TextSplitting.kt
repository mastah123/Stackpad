package com.stackpad.app.domain.text

import com.stackpad.app.data.model.TextSpan

/** One line produced by splitting text, with its formatting spans re-based to the line start. */
data class TextLine(val text: String, val spans: List<TextSpan>)

/** The lines of a split, and which line / offset the caret ends up in. */
data class SplitResult(val lines: List<TextLine>, val caretLine: Int, val caretOffset: Int)

/**
 * Splits text into lines, with each line's formatting spans re-based to the line start.
 */
object TextSplitting {

    fun splitLines(text: String, spans: List<TextSpan>, caret: Int): SplitResult {
        val safeCaret = caret.coerceIn(0, text.length)
        val lines = ArrayList<TextLine>()
        var caretLine = 0
        var caretOffset = 0
        var start = 0
        while (true) {
            val newline = text.indexOf('\n', start)
            val end = if (newline < 0) text.length else newline
            val lineSpans = spans.mapNotNull { span ->
                val from = maxOf(span.start, start)
                val to = minOf(span.end, end)
                if (to > from) span.copy(start = from - start, end = to - start) else null
            }
            lines.add(TextLine(text.substring(start, end), lineSpans))
            if (safeCaret in start..end) {
                caretLine = lines.lastIndex
                caretOffset = safeCaret - start
            }
            if (newline < 0) break
            start = end + 1
        }
        return SplitResult(lines, caretLine, caretOffset)
    }
}
