package com.stackpad.app.data.model

/** Plain text of a block, used for note search and list previews. */
fun Block.searchableText(): String = when (this) {
    is TextBlock -> text
    is ListBlock -> items.joinToString("\n") { it.text }
    is ChecklistBlock -> (listOf(title) + items.map { it.text }).joinToString("\n")
    is SheetBlock -> cells.joinToString("\n") { row -> row.joinToString(" ") }
    is LinkBlock -> "$title $url"
    is PromptBlock -> text
    is ImageBlock, is AudioBlock -> ""
}

/** True when the block holds no user content (used to avoid saving empty notes). */
fun Block.isBlank(): Boolean = when (this) {
    is TextBlock -> text.isBlank()
    is ListBlock -> items.all { it.text.isBlank() }
    is ChecklistBlock -> title.isBlank() && items.all { it.text.isBlank() }
    is SheetBlock -> cells.all { row -> row.all { it.isBlank() } }
    is LinkBlock -> title.isBlank() && url.isBlank()
    is PromptBlock -> text.isBlank()
    is ImageBlock, is AudioBlock -> false
}

/** The media file this block owns, if any. */
fun Block.mediaFileName(): String? = when (this) {
    is ImageBlock -> fileName
    is AudioBlock -> fileName
    else -> null
}
