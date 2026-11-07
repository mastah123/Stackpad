package com.stackpad.app.domain.notes

/** A tag and how many notes use it. */
data class TagCount(val tag: String, val count: Int)

/** Tag rules: lowercase, no spaces or commas, short, and unique per note. */
object Tags {
    const val MAX_LENGTH = 24
    const val MAX_PER_NOTE = 10

    private const val SEPARATOR = "\n"
    private val whitespace = Regex("\\s+")

    /** Cleans user input into a tag, or returns null when nothing usable is left. */
    fun normalize(raw: String): String? {
        val cleaned = raw.trim()
            .trimStart('#')
            .lowercase()
            .replace(',', ' ')
            .replace(whitespace, "-")
            .take(MAX_LENGTH)
            .trim('-')
        return cleaned.ifEmpty { null }
    }

    /** Adds [raw] to [tags]. Invalid, duplicate, or over-limit input leaves the list unchanged. */
    fun add(tags: List<String>, raw: String): List<String> {
        val tag = normalize(raw) ?: return tags
        if (tag in tags || tags.size >= MAX_PER_NOTE) return tags
        return tags + tag
    }

    fun serialize(tags: List<String>): String = tags.joinToString(SEPARATOR)

    fun parse(stored: String): List<String> =
        if (stored.isEmpty()) emptyList() else stored.split(SEPARATOR).filter { it.isNotEmpty() }

    /** Tags across many notes with usage counts, most used first, then alphabetical. */
    fun count(perNote: List<List<String>>): List<TagCount> =
        perNote.flatten()
            .groupingBy { it }
            .eachCount()
            .map { TagCount(it.key, it.value) }
            .sortedWith(compareByDescending<TagCount> { it.count }.thenBy { it.tag })
}
