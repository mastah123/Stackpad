package com.stackpad.app.data.model

/** A full note, as edited in the editor. */
data class Note(
    val id: String,
    val title: String,
    val blocks: List<Block>,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Lightweight note projection shown in the note list. */
data class NoteSummary(
    val id: String,
    val title: String,
    val preview: String,
    val pinned: Boolean,
    val updatedAt: Long,
)
