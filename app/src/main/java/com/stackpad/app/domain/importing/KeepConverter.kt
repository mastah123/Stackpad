package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.notes.Tags

/** Turns a [KeepNote] into an [ImportedNote]. */
object KeepConverter {

    /** Keep color name to Stackpad's color palette index (0 = no color). Greys and browns have no match. */
    private val colors = mapOf(
        "RED" to 1, "ORANGE" to 2, "YELLOW" to 3, "GREEN" to 4,
        "TEAL" to 5, "BLUE" to 6, "CERULEAN" to 6, "PURPLE" to 7, "PINK" to 8,
    )

    fun colorIndex(keepColor: String): Int = colors[keepColor.trim().uppercase()] ?: 0

    /** Keep stores microseconds since 1970; [fallback] is used when a note has no timestamp. */
    private fun millis(usec: Long, fallback: Long): Long = if (usec > 0) usec / 1000 else fallback

    fun convert(note: KeepNote, sourceId: String, now: Long): ImportedNote {
        val blocks = ArrayList<Block>()
        if (note.text.isNotBlank()) blocks += TextBlock(BlockFactory.newId(), text = note.text.trimEnd())

        val tasks = note.items.filter { it.text.isNotBlank() }
        if (tasks.isNotEmpty()) {
            blocks += ChecklistBlock(
                id = BlockFactory.newId(),
                title = "",
                items = tasks.map { ChecklistItem(BlockFactory.newId(), it.text.trim(), it.checked) },
            )
        }

        note.links.filter { it.url.isNotBlank() }.distinctBy { it.url.trim() }.forEach {
            blocks += LinkBlock(BlockFactory.newId(), it.title.trim().ifBlank { it.url.trim() }, it.url.trim())
        }

        val (pictures, other) = note.attachments.partition { it.mimeType.startsWith("image/") }
        val created = millis(note.createdUsec, millis(note.editedUsec, now))
        return ImportedNote(
            sourceId = sourceId,
            title = note.title.trim(),
            blocks = blocks,
            pictures = pictures.map { it.path },
            tags = note.labels.mapNotNull(Tags::normalize).distinct().take(Tags.MAX_PER_NOTE),
            color = colorIndex(note.color),
            pinned = note.pinned,
            createdAt = created,
            updatedAt = millis(note.editedUsec, created),
            skippedAttachments = other.size,
        )
    }
}
