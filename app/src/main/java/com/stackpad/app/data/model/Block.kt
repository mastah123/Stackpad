package com.stackpad.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Heading level of a text block. */
@Serializable
enum class TextLevel { PARAGRAPH, H1, H2, H3 }

/**
 * Inline formatting inside a text block. H1 to H3 are line headings: a heading span always covers
 * one whole line, and a line has at most one.
 */
@Serializable
enum class SpanType {
    BOLD, ITALIC, UNDERLINE, H1, H2, H3,
    /** Per-letter font and highlight, and per-line spacing (see FormatSpans). */
    FONT_SERIF, FONT_MONO, FONT_HANDWRITING,
    HIGHLIGHT_1, HIGHLIGHT_2, HIGHLIGHT_3, HIGHLIGHT_4, HIGHLIGHT_5, HIGHLIGHT_6,
    LINE_TIGHT, LINE_RELAXED, LINE_LOOSE,
}

/** A formatted range `[start, end)` inside a text block. */
@Serializable
data class TextSpan(val start: Int, val end: Int, val type: SpanType)

/** Marker style of a list block. */
@Serializable
enum class ListKind {
    BULLET, NUMBERED, LETTERED,
    BULLET_HOLLOW, BULLET_SQUARE, UPPER_ALPHA, UPPER_ROMAN, LOWER_ROMAN, GREEK,
    /** Items with a tick box that is checked by tapping it; Enter adds another item. */
    CHECKBOX,
}

@Serializable
data class ListItem(val id: String, val text: String = "", val checked: Boolean = false)

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String = "",
    val checked: Boolean = false,
    /** Due date as an ISO local date (`2026-11-10`), or null. A reminder fires at 9:00 that day. */
    val dueDate: String? = null,
)

/**
 * One building block of a note. A note is an ordered list of blocks.
 * Blocks are immutable; edits create copies.
 */
@Serializable
sealed interface Block {
    val id: String
}

@Serializable
@SerialName("text")
data class TextBlock(
    override val id: String,
    /** Only read from older notes; headings are now line spans (see [SpanType.H1]). Always PARAGRAPH once opened. */
    val level: TextLevel = TextLevel.PARAGRAPH,
    val text: String = "",
    val spans: List<TextSpan> = emptyList(),
    /** Size of the whole block (font, highlight and line spacing are per letter and per line, in [spans]). */
    val size: TextSize = TextSize.NORMAL,
) : Block

@Serializable
@SerialName("list")
data class ListBlock(
    override val id: String,
    val kind: ListKind,
    val items: List<ListItem>,
) : Block

@Serializable
@SerialName("checklist")
data class ChecklistBlock(
    override val id: String,
    val title: String = "",
    val items: List<ChecklistItem>,
    /** When on, done items drop to the bottom as they are ticked. */
    val autoSort: Boolean = false,
    /** When on, done items are folded away (they stay in the note). */
    val hideCompleted: Boolean = false,
) : Block

@Serializable
@SerialName("image")
data class ImageBlock(
    override val id: String,
    /** File name inside the app's media folder. */
    val fileName: String,
    /** No longer shown or editable; kept only so older notes and backups still load. */
    val caption: String = "",
    /** The picture as it was before the first edit, kept so the edit can be undone later; null when never edited. */
    val originalFileName: String? = null,
) : Block

@Serializable
@SerialName("sheet")
data class SheetBlock(
    override val id: String,
    /** Row-major raw cell contents. Raw text starting with `=` is a formula. */
    val cells: List<List<String>>,
    /** Column widths in dp, one per column; 0 or a missing entry means the default width. */
    val columnWidths: List<Int> = emptyList(),
    /** Row heights in dp, one per row; 0 or a missing entry means the default height. */
    val rowHeights: List<Int> = emptyList(),
) : Block

@Serializable
@SerialName("audio")
data class AudioBlock(
    override val id: String,
    /** File name inside the app's media folder. */
    val fileName: String,
    val durationMs: Long,
    /** Waveform bar heights, 0..100. */
    val waveform: List<Int>,
) : Block

@Serializable
@SerialName("link")
data class LinkBlock(
    override val id: String,
    val title: String = "",
    val url: String = "",
) : Block

@Serializable
@SerialName("prompt")
data class PromptBlock(
    override val id: String,
    val text: String = "",
) : Block

@Serializable
@SerialName("quote")
data class QuoteBlock(
    override val id: String,
    val text: String = "",
) : Block

@Serializable
@SerialName("code")
data class CodeBlock(
    override val id: String,
    val text: String = "",
) : Block

/** A horizontal rule that separates parts of a note. */
@Serializable
@SerialName("divider")
data class DividerBlock(
    override val id: String,
) : Block
