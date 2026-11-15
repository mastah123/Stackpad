package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block

/**
 * A note read from another app, in a shape that does not depend on that app's file format, so
 * every importer (Google Keep now, Markdown and others later) ends up with the same thing.
 *
 * @property sourceId stable text identifying the note in its source (e.g. the file path), used to
 * recognise it on a second import.
 * @property pictures file paths inside the source, shown at the top of the note.
 */
data class ImportedNote(
    val sourceId: String,
    val title: String,
    val blocks: List<Block>,
    val pictures: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val color: Int = 0,
    val pinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    /** Attachments that cannot be imported (audio, drawings). */
    val skippedAttachments: Int = 0,
)
