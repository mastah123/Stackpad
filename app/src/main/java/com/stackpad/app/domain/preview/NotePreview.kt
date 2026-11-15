package com.stackpad.app.domain.preview

import com.stackpad.app.data.model.Block
import com.stackpad.app.domain.export.DocItem
import com.stackpad.app.domain.export.DocItems

/**
 * The first part of a note as formatted items for a note card: headings, styled text, lists,
 * checklists, a picture, a table and so on, cut down so a card stays small.
 */
object NotePreview {
    const val MAX_TABLE_ROWS = 3
    const val MAX_TASKS = 4

    fun from(blocks: List<Block>, maxItems: Int): List<DocItem> =
        DocItems.blocks(blocks).take(maxItems.coerceAtLeast(0)).map { item ->
            when (item) {
                is DocItem.Table -> DocItem.Table(item.rows.take(MAX_TABLE_ROWS))
                is DocItem.Checklist -> DocItem.Checklist(item.title, item.tasks.take(MAX_TASKS))
                else -> item
            }
        }
}
