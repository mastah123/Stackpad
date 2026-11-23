package com.stackpad.app.domain.share

/** Turns [ShareOptions] into one short text (`theme=DARK;noteTint=0;...`) for storage, and back. Unknown or broken parts fall back to the defaults. */
object ShareOptionsCodec {

    fun encode(o: ShareOptions): String = listOf(
        "theme" to o.theme.name,
        "noteTint" to flag(o.noteTint),
        "textSize" to o.textSize.name,
        "pictures" to flag(o.includePictures),
        "hideDone" to flag(o.hideCompletedTasks),
        "pageSize" to o.pageSize.name,
        "pageNumbers" to flag(o.pageNumbers),
        "cleanLines" to flag(o.cleanUp.emptyLines),
        "cleanItems" to flag(o.cleanUp.emptyListItems),
        "cleanChecklists" to flag(o.cleanUp.emptyChecklists),
        "cleanTables" to flag(o.cleanUp.emptyTables),
        "cleanCards" to flag(o.cleanUp.emptyCards),
        "title" to flag(o.showTitle),
        "words" to flag(o.showWordCount),
        "dates" to flag(o.showDates),
        "tags" to flag(o.showTags),
        "footer" to flag(o.showFooter),
    ).joinToString(";") { (key, value) -> "$key=$value" }

    fun decode(text: String?): ShareOptions {
        val values = text.orEmpty().split(';').mapNotNull { part ->
            val at = part.indexOf('=')
            if (at <= 0) null else part.substring(0, at) to part.substring(at + 1)
        }.toMap()
        val d = ShareOptions()
        fun flag(key: String, default: Boolean) = when (values[key]) { "1" -> true; "0" -> false; else -> default }
        return ShareOptions(
            theme = choice(values, "theme", d.theme),
            noteTint = flag("noteTint", d.noteTint),
            textSize = choice(values, "textSize", d.textSize),
            includePictures = flag("pictures", d.includePictures),
            hideCompletedTasks = flag("hideDone", d.hideCompletedTasks),
            pageSize = choice(values, "pageSize", d.pageSize),
            pageNumbers = flag("pageNumbers", d.pageNumbers),
            cleanUp = ShareCleanUp(
                emptyLines = flag("cleanLines", d.cleanUp.emptyLines),
                emptyListItems = flag("cleanItems", d.cleanUp.emptyListItems),
                emptyChecklists = flag("cleanChecklists", d.cleanUp.emptyChecklists),
                emptyTables = flag("cleanTables", d.cleanUp.emptyTables),
                emptyCards = flag("cleanCards", d.cleanUp.emptyCards),
            ),
            showTitle = flag("title", d.showTitle),
            showWordCount = flag("words", d.showWordCount),
            showDates = flag("dates", d.showDates),
            showTags = flag("tags", d.showTags),
            showFooter = flag("footer", d.showFooter),
        )
    }

    private fun flag(value: Boolean) = if (value) "1" else "0"

    private inline fun <reified E : Enum<E>> choice(values: Map<String, String>, key: String, default: E): E =
        enumValues<E>().firstOrNull { it.name == values[key] } ?: default
}
