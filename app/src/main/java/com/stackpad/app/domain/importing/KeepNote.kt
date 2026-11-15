package com.stackpad.app.domain.importing

/** What a Google Keep Takeout note file contains, with Keep's own field names and values. */
data class KeepNote(
    val title: String = "",
    val text: String = "",
    val items: List<Item> = emptyList(),
    val trashed: Boolean = false,
    val archived: Boolean = false,
    val pinned: Boolean = false,
    /** Keep's color name, e.g. "RED" or "DEFAULT". */
    val color: String = "DEFAULT",
    val labels: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val links: List<Link> = emptyList(),
    val createdUsec: Long = 0,
    val editedUsec: Long = 0,
) {
    data class Item(val text: String, val checked: Boolean)
    data class Attachment(val path: String, val mimeType: String)
    data class Link(val title: String, val url: String)
}
