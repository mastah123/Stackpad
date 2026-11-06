package com.stackpad.app.domain.text

import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextSpan

/** Keeps inline formatting spans consistent while the user types, and toggles formatting. */
object SpanEditing {

    /**
     * Shifts/trims [spans] so they still cover the same characters after the text changed
     * from [oldText] to [newText]. The change is found as the differing middle part between
     * the longest common prefix and suffix. Typing right after a span does not extend it.
     */
    fun adjustForEdit(spans: List<TextSpan>, oldText: String, newText: String): List<TextSpan> {
        if (spans.isEmpty() || oldText == newText) return spans

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

        val oldChangeEnd = oldText.length - suffix
        val delta = newText.length - oldText.length

        return spans.mapNotNull { span ->
            val (newStart, newEnd) = when {
                span.end <= prefix -> span.start to span.end
                span.start >= oldChangeEnd -> (span.start + delta) to (span.end + delta)
                else -> {
                    val start = if (span.start < prefix) span.start else prefix
                    val end = if (span.end >= oldChangeEnd) span.end + delta else prefix
                    start to end
                }
            }
            val start = newStart.coerceAtLeast(0)
            val end = newEnd.coerceAtMost(newText.length)
            if (end > start) span.copy(start = start, end = end) else null
        }
    }

    /** True when every character in `[start, end)` already has [type]. */
    fun isFullyStyled(spans: List<TextSpan>, start: Int, end: Int, type: SpanType): Boolean {
        if (start >= end) return false
        val merged = merge(spans.filter { it.type == type }.map { it.start to it.end })
        return merged.any { it.first <= start && it.second >= end }
    }

    /**
     * Applies [type] to `[start, end)`; if the whole range already has it, removes it instead.
     */
    fun toggle(spans: List<TextSpan>, start: Int, end: Int, type: SpanType): List<TextSpan> {
        if (start >= end) return spans
        val others = spans.filter { it.type != type }
        val same = spans.filter { it.type == type }.map { it.start to it.end }

        val result = if (isFullyStyled(spans, start, end, type)) {
            same.flatMap { (s, e) ->
                buildList {
                    if (s < start) add(s to minOf(e, start))
                    if (e > end) add(maxOf(s, end) to e)
                }
            }
        } else {
            merge(same + (start to end))
        }
        return (others + result.map { (s, e) -> TextSpan(s, e, type) })
            .sortedWith(compareBy({ it.start }, { it.end }))
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
