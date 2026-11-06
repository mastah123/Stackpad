package com.stackpad.app.data.model

import java.util.UUID

/** Creates new, empty blocks with fresh ids. */
object BlockFactory {
    const val DEFAULT_SHEET_ROWS = 4
    const val DEFAULT_SHEET_COLUMNS = 3

    fun newId(): String = UUID.randomUUID().toString()

    fun text(): TextBlock = TextBlock(id = newId())

    fun list(kind: ListKind): ListBlock =
        ListBlock(id = newId(), kind = kind, items = listOf(ListItem(newId())))

    fun checklist(): ChecklistBlock =
        ChecklistBlock(id = newId(), items = listOf(ChecklistItem(newId())))

    fun sheet(): SheetBlock = SheetBlock(
        id = newId(),
        cells = List(DEFAULT_SHEET_ROWS) { List(DEFAULT_SHEET_COLUMNS) { "" } },
    )

    fun link(): LinkBlock = LinkBlock(id = newId())

    fun prompt(): PromptBlock = PromptBlock(id = newId())

    fun image(fileName: String): ImageBlock = ImageBlock(id = newId(), fileName = fileName)

    fun audio(fileName: String, durationMs: Long, waveform: List<Int>): AudioBlock =
        AudioBlock(id = newId(), fileName = fileName, durationMs = durationMs, waveform = waveform)
}
