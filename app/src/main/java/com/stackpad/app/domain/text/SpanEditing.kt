package com.stackpad.app.domain.text

import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextSpan

/** Keeps inline formatting spans consistent while the user types, and toggles formatting. */
object SpanEditing {

    /** Where text changed: [prefix] unchanged leading chars; the changed region ends at [oldEnd] / [newEnd]. */
    data class Change(val prefix: Int, val oldEnd: Int, val newEnd: Int) {
        val removed: Int get() = oldEnd - prefix
        val inserted: Int get() = newEnd - prefix
    }

    /** Finds the differing middle part between the longest common prefix and suffix. */
    fun diff(oldText: String, newText: String): Change {
        val maxPrefix = minOf(oldText.length, newText.length)
        var prefix = 0
        while (prefix < maxPrefix && oldText[prefix] == newText[prefix]) prefix++

        val maxSuffix = maxPrefix - prefix
        var suffix = 0
        while (suffix < maxSuffix &&
            oldText[oldText.length - 1 - suffix] == newText[newText.length - 1 - suffix]
        ) {
            suffix++
        }
        return Change(prefix, oldText.length - suffix, newText.length - suffix)
    }

    /**
     * Shifts/trims [spans] so they still cover the same characters after the text changed
     * from [oldText] to [newText]. Text typed right after a span is not added to it
     * (see [applyEdit] for that).
     */
    fun adjustForEdit(spans: List<TextSpan>, oldText: String, newText: String): List<TextSpan> {
        if (spans.isEmpty() || oldText == newText) return spans

        val change = diff(oldText, newText)
        val delta = newText.length - oldText.length

        return spans.mapNotNull { span ->
            val (newStart, newEnd) = when {
                span.end <= change.prefix -> span.start to span.end
                span.start >= change.oldEnd -> (span.start + delta) to (span.end + delta)
                else -> {
                    val start = if (span.start < change.prefix) span.start else change.prefix
                    val end = if (span.end >= change.oldEnd) span.end + delta else change.prefix
                    start to end
                }
            }
            val start = newStart.coerceAtLeast(0)
            val end = newEnd.coerceAtMost(newText.length)
            if (end > start) span.copy(start = start, end = end) else null
        }
    }

    /**
     * Full edit handling: adjusts spans, then styles any newly typed or pasted text.
     * New text gets [typing] styles when given (set from the toolbar), otherwise it inherits the
     * styles of the character before it (or the first replaced character), like a word processor.
     */
    fun applyEdit(
        spans: List<TextSpan>,
        oldText: String,
        newText: String,
        typing: Set<SpanType>? = null,
    ): List<TextSpan> {
        if (oldText == newText) return spans
        val change = diff(oldText, newText)
        var result = adjustForEdit(spans, oldText, newText)
        if (change.inserted <= 0) return result

        val inherited = typing
            ?: stylesAt(spans, if (change.removed > 0) change.prefix else change.prefix - 1)
        for (type in SpanType.values()) {
            result = withCoverage(result, change.prefix, change.newEnd, type, type in inherited)
        }
        return result
    }

    /** Styles applied to the character at [index]. */
    fun stylesAt(spans: List<TextSpan>, index: Int): Set<SpanType> {
        if (index < 0) return emptySet()
        return spans.filter { it.start <= index && index < it.end }.map { it.type }.toSet()
    }

    /** True when every character in `[start, end)` already has [type]. */
    fun isFullyStyled(spans: List<TextSpan>, start: Int, end: Int, type: SpanType): Boolean {
        if (start >= end) return false
        val merged = merge(spans.filter { it.type == type }.map { it.start to it.end })
        return merged.any { it.first <= start && it.second >= end }
    }

    /** Applies [type] to `[start, end)`; if the whole range already has it, removes it instead. */
    fun toggle(spans: List<TextSpan>, start: Int, end: Int, type: SpanType): List<TextSpan> {
        if (start >= end) return spans
        return withCoverage(spans, start, end, type, on = !isFullyStyled(spans, start, end, type))
    }

    /** Makes `[start, end)` fully [type]-styled ([on] = true) or completely unstyled. */
    fun withCoverage(
        spans: List<TextSpan>,
        start: Int,
        end: Int,
        type: SpanType,
        on: Boolean,
    ): List<TextSpan> {
        if (start >= end) return spans
        val others = spans.filter { it.type != type }
        val same = spans.filter { it.type == type }.map { it.start to it.end }
        val updated = if (on) merge(same + (start to end)) else subtract(same, start, end)
        return (others + updated.map { (s, e) -> TextSpan(s, e, type) })
            .sortedWith(compareBy({ it.start }, { it.end }, { it.type.ordinal }))
    }

    private fun subtract(ranges: List<Pair<Int, Int>>, start: Int, end: Int): List<Pair<Int, Int>> =
        ranges.flatMap { (s, e) ->
            buildList {
                if (s < start) add(s to minOf(e, start))
                if (e > end) add(maxOf(s, end) to e)
            }
        }

    private fun merge(ranges: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val sorted = ranges.filter { it.second > it.first }.sortedBy { it.first }
        val merged = ArrayList<Pair<Int, Int>>()
        for (range in sorted) {
            val last = merged.lastOrNull()
            if (last != null && range.first <= last.second) {
                merged[merged.lastIndex] = last.first to maxOf(last.second, range.second)
            } else {
                merged.add(range)
            }
        }
        return merged
    }
}
