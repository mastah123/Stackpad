package com.stackpad.app.domain.share

/** Turns text shared into the app (from a browser, another app...) into the parts of a new note. */
object SharedContentPlanner {
    sealed interface Part {
        data class Text(val text: String) : Part
        data class Link(val url: String) : Part
    }

    data class Plan(val title: String, val parts: List<Part>)

    private const val MAX_TITLE = 60
    private val urlLine = Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)

    /**
     * A line that is only a web address becomes a link card; other lines stay text, grouped until a
     * blank line or a link. The [subject] is the title; without one the first short text line is.
     */
    fun plan(subject: String?, text: String?): Plan {
        val parts = ArrayList<Part>()
        val buffer = ArrayList<String>()
        fun flush() {
            if (buffer.isNotEmpty()) parts += Part.Text(buffer.joinToString("\n"))
            buffer.clear()
        }
        for (raw in text.orEmpty().lines()) {
            val line = raw.trim()
            when {
                line.isEmpty() -> flush()
                urlLine.matches(line) -> {
                    flush()
                    parts += Part.Link(line)
                }
                else -> buffer += line
            }
        }
        flush()

        val given = subject?.trim().orEmpty()
        if (given.isNotEmpty()) return Plan(given.take(MAX_TITLE), parts)

        val first = parts.firstOrNull() as? Part.Text ?: return Plan("", parts)
        val firstLine = first.text.lines().first()
        val title = firstLine.take(MAX_TITLE).trimEnd()
        val wholePartIsTitle = first.text == firstLine && firstLine.length <= MAX_TITLE
        return if (wholePartIsTitle && parts.size > 1) Plan(title, parts.drop(1)) else Plan(title, parts)
    }
}
