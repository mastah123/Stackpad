package com.stackpad.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Heading level of a text block. */
@Serializable
enum class TextLevel { PARAGRAPH, H1, H2, H3 }

/** Inline formatting that can be applied to a selection inside a text block. */
@Serializable
enum class SpanType { BOLD, ITALIC, UNDERLINE }

/** A formatted range `[start, end)` inside a text block. */
@Serializable
data class TextSpan(val start: Int, val end: Int, val type: SpanType)

/** Marker style of a list block. */
@Serializable
enum class ListKind { BULLET, NUMBERED, LETTERED }

@Serializable
data class ListItem(val id: String, val text: String = "")

@Serializable
data class ChecklistItem(val id: String, val text: String = "", val checked: Boolean = false)

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
    val level: TextLevel = TextLevel.PARAGRAPH,
    val text: String = "",
    val spans: List<TextSpan> = emptyList(),
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
) : Block

@Serializable
@SerialName("image")
data class ImageBlock(
    override val id: String,
    /** File name inside the app's media folder. */
    val fileName: String,
) : Block

@Serializable
@SerialName("sheet")
data class SheetBlock(
    override val id: String,
    /** Row-major raw cell contents. Raw text starting with `=` is a formula. */
    val cells: List<List<String>>,
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
