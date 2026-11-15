package com.stackpad.app.domain.export

import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.domain.blocks.BlockTone
import com.stackpad.app.data.model.TextSpan

/** One drawable piece of a note when it is rendered as an image or a PDF page. */
sealed interface DocItem {
    /** Note title or a heading level of a text block. */
    data class Heading(val level: TextLevel, val text: String, val spans: List<TextSpan> = emptyList()) : DocItem

    data class Paragraph(val text: String, val spans: List<TextSpan> = emptyList()) : DocItem

    /** A list row. [marker] is `•`, `1.` or `a.`. */
    data class Row(val marker: String, val text: String) : DocItem

    /** A whole checklist, drawn as one green card with real boxes. */
    data class Checklist(val title: String, val tasks: List<Task>) : DocItem {
        data class Task(val text: String, val done: Boolean)
    }

    data class Quote(val text: String) : DocItem

    /** Prompt and code blocks on a tinted card; code is drawn monospaced. */
    data class Code(val text: String, val tone: BlockTone = BlockTone.CODE) : DocItem

    data class Link(val title: String, val url: String) : DocItem

    data class Image(val fileName: String, val caption: String) : DocItem

    data class Table(val rows: List<List<String>>) : DocItem

    data class Audio(val label: String) : DocItem

    data object Divider : DocItem
}
