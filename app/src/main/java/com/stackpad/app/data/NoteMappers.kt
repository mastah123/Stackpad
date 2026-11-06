package com.stackpad.app.data

import com.stackpad.app.data.db.NoteEntity
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.model.searchableText

private const val PREVIEW_LENGTH = 160

internal fun NoteEntity.toNote(): Note = Note(
    id = id,
    title = title,
    blocks = BlockJsonCodec.decode(blocksJson),
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun Note.toEntity(updatedAt: Long): NoteEntity = NoteEntity(
    id = id,
    title = title,
    blocksJson = BlockJsonCodec.encode(blocks),
    searchText = blocks.map { it.searchableText() }.filter { it.isNotBlank() }.joinToString("\n"),
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun NoteEntity.toSummary(): NoteSummary = NoteSummary(
    id = id,
    title = title,
    preview = searchText.replace(Regex("\\s+"), " ").trim().take(PREVIEW_LENGTH),
    pinned = pinned,
    updatedAt = updatedAt,
)
