package com.stackpad.app.domain.share

/** Colors of the exported page. [APP] follows the app's own light or dark theme. */
enum class ShareTheme { LIGHT, DARK, APP }

/** Text size of the exported page; headings and cards scale with it. */
enum class ShareTextSize(val scale: Float) {
    SMALL(0.9f),
    MEDIUM(1f),
    LARGE(1.2f),
}

/** PDF page size in points. */
enum class SharePageSize(val width: Int, val height: Int) {
    A4(595, 842),
    LETTER(612, 792),
}

/** What to leave out of an exported note. Each tick is its own switch. */
data class ShareCleanUp(
    /** Empty text blocks, which are blank lines in the note. */
    val emptyLines: Boolean = false,
    val emptyListItems: Boolean = false,
    /** Empty checklist items, and checklists left with nothing. */
    val emptyChecklists: Boolean = false,
    /** Blank table rows, and tables left with nothing. */
    val emptyTables: Boolean = false,
    /** Quote, prompt, code and link cards with nothing in them. */
    val emptyCards: Boolean = false,
) {
    companion object {
        val NONE = ShareCleanUp()
        val ALL = ShareCleanUp(true, true, true, true, true)
    }
}

/** Everything the person can choose when sharing a note as an image or a PDF. Defaults copy the note as it looks in the app. */
data class ShareOptions(
    // Look
    val theme: ShareTheme = ShareTheme.LIGHT,
    val noteTint: Boolean = true,
    val textSize: ShareTextSize = ShareTextSize.MEDIUM,
    val includePictures: Boolean = true,
    val hideCompletedTasks: Boolean = false,
    val pageSize: SharePageSize = SharePageSize.A4,
    val pageNumbers: Boolean = false,
    // Clean up
    val cleanUp: ShareCleanUp = ShareCleanUp.NONE,
    // Details
    val showTitle: Boolean = true,
    val showWordCount: Boolean = false,
    val showDates: Boolean = false,
    val showTags: Boolean = false,
    val showFooter: Boolean = false,
)
