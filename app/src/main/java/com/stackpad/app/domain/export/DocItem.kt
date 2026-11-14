package com.stackpad.app.domain.export

import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan

/** One drawable piece of a note when it is rendered as an image or a PDF page. */
sealed interface DocItem {
    /** Note title or a heading level of a text block. */
    data class Heading(val level: TextLevel, val text: String, val spans: List<TextSpan> = emptyList()) : DocItem

    data class Paragraph(val text: String, val spans: List<TextSpan> = emptyList()) : DocItem

    /** A list or checklist row. [marker] is `•`, `1.`, `a.`, `☐` or `☑`. */
    data class Row(val marker: String, val text: String, val done: Boolean = false) : DocItem

    data class Quote(val text: String) : DocItem

    /** Prompt and code blocks, drawn monospaced on a tinted background. */
    data class Code(val text: String) : DocItem

    data class Link(val title: String, val url: String) : DocItem

    data class Image(val fileName: String, val caption: String) : DocItem

    data class Table(val rows: List<List<String>>) : DocItem

    data class Audio(val label: String) : DocItem

    data object Divider : DocItem
}
