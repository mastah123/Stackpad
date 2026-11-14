package com.stackpad.app.domain.search

/** Word-based search over a note's title, content and tags. Pure, so it is unit tested. */
object SearchQuery {
    const val MAX_WORDS = 8
    const val SNIPPET_LENGTH = 120
    private const val ELLIPSIS = "…"

    /** The words of a query (split on spaces, de-duplicated, capped). A blank query has none. */
    fun words(query: String): List<String> =
        query.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.take(MAX_WORDS)

    /** True when every word appears (ignoring case) in the title, the content or the tags. */
    fun matches(words: List<String>, title: String, content: String, tags: List<String>): Boolean {
        if (words.isEmpty()) return true
        val haystack = buildString {
            append(title).append('\n').append(content).append('\n').append(tags.joinToString(" "))
        }
        return words.all { haystack.contains(it, ignoreCase = true) }
    }

    /** Where the words occur in [text], as sorted, non-overlapping ranges (for highlighting). */
    fun ranges(text: String, words: List<String>): List<IntRange> {
        val found = ArrayList<IntRange>()
        for (word in words) {
            var from = 0
            while (true) {
                val at = text.indexOf(word, from, ignoreCase = true)
                if (at < 0) break
                found += at until at + word.length
                from = at + word.length
            }
        }
        if (found.isEmpty()) return emptyList()
        found.sortBy { it.first }
        val merged = ArrayList<IntRange>()
        var current = found[0]
        for (range in found.drop(1)) {
            current = if (range.first <= current.last + 1) {
                current.first..maxOf(current.last, range.last)
            } else {
                merged += current
                range
            }
        }
        merged += current
        return merged
    }

    /**
     * A short piece of [content] to show under a search hit: the start of the note when a word is
     * visible there, otherwise a window around the first match.
     */
    fun snippet(content: String, words: List<String>, maxLength: Int = SNIPPET_LENGTH): String {
        val flat = content.replace(Regex("\\s+"), " ").trim()
        if (flat.length <= maxLength) return flat
        val first = ranges(flat, words).firstOrNull()?.first ?: return flat.take(maxLength)
        if (first + 1 <= maxLength - 10) return flat.take(maxLength)
        val start = (first - maxLength / 4).coerceAtLeast(0)
        val end = (start + maxLength).coerceAtMost(flat.length)
        val body = flat.substring(start, end).trim()
        return (if (start > 0) ELLIPSIS else "") + body + (if (end < flat.length) ELLIPSIS else "")
    }
}
