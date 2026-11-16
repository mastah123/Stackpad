package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextSpan

/** Text with bold, italic and underline ranges, as stored in a text block. */
data class StyledText(val text: String, val spans: List<TextSpan> = emptyList()) {
    val isBlank: Boolean get() = text.isBlank()
}

/** Collects pieces of text with their styles into one [StyledText]; neighbouring pieces with the same style merge. */
class StyledTextBuilder {
    private val text = StringBuilder()
    private val spans = ArrayList<TextSpan>()

    val length: Int get() = text.length

    fun append(piece: String, bold: Boolean = false, italic: Boolean = false, underline: Boolean = false) {
        if (piece.isEmpty()) return
        val start = text.length
        text.append(piece)
        val end = text.length
        if (bold) addSpan(start, end, SpanType.BOLD)
        if (italic) addSpan(start, end, SpanType.ITALIC)
        if (underline) addSpan(start, end, SpanType.UNDERLINE)
    }

    private fun addSpan(start: Int, end: Int, type: SpanType) {
        val last = spans.lastOrNull { it.type == type }
        if (last != null && last.end == start) {
            spans[spans.lastIndexOf(last)] = last.copy(end = end)
        } else {
            spans += TextSpan(start, end, type)
        }
    }

    /** The text with leading and trailing whitespace removed; spans are moved and clipped to match. */
    fun build(): StyledText {
        val raw = text.toString()
        val lead = raw.length - raw.trimStart().length
        val trimmed = raw.trim()
        val fixed = spans.mapNotNull { span ->
            val start = (span.start - lead).coerceIn(0, trimmed.length)
            val end = (span.end - lead).coerceIn(0, trimmed.length)
            if (end > start) TextSpan(start, end, span.type) else null
        }
        return StyledText(trimmed, fixed)
    }
}
