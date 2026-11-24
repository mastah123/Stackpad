package com.stackpad.app.domain.notes

/** Order of the tag list in the side menu. */
enum class TagSort { NAME, COUNT }

/** Filters and orders the tag list shown in the side menu. */
object TagList {
    /** Tags containing [filter] (ignoring case and edges), ordered by [sort]. Ties fall back to name. */
    fun visible(tags: List<TagCount>, filter: String, sort: TagSort): List<TagCount> {
        val needle = filter.trim().lowercase()
        val matching = tags.filter { needle.isEmpty() || it.tag.lowercase().contains(needle) }
        return when (sort) {
            TagSort.NAME -> matching.sortedBy { it.tag.lowercase() }
            TagSort.COUNT -> matching.sortedWith(compareByDescending<TagCount> { it.count }.thenBy { it.tag.lowercase() })
        }
    }
}
