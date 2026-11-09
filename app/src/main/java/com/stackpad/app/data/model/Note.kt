package com.stackpad.app.data.model

/** A full note, as edited in the editor. */
data class Note(
    val id: String,
    val title: String,
    val blocks: List<Block>,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val folderId: String? = null,
    val tags: List<String> = emptyList(),
    /** Index into the note color palette; 0 means no color. */
    val color: Int = 0,
    /** When the note was moved to the trash, or null for a normal note. */
    val trashedAt: Long? = null,
    /** True when opening the note requires the phone's screen lock. */
    val locked: Boolean = false,
)

/** Lightweight note projection shown in the note list and the trash. */
data class NoteSummary(
    val id: String,
    val title: String,
    val preview: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val folderId: String? = null,
    val tags: List<String> = emptyList(),
    val color: Int = 0,
    val trashedAt: Long? = null,
    /** True for a locked note: the preview is empty. */
    val locked: Boolean = false,
)

/** A user-created folder that notes can be filed in. A note is in at most one folder. */
data class Folder(val id: String, val name: String)
