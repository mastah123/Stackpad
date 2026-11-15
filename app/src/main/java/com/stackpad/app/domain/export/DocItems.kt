package com.stackpad.app.domain.export

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.isBlank
import com.stackpad.app.domain.blocks.BlockTone
import com.stackpad.app.domain.formula.SheetEvaluator
import com.stackpad.app.domain.text.ListMarkers
import com.stackpad.app.util.formatDuration

/** Turns a note into [DocItem]s. Blank blocks are skipped; table formulas show their results. */
object DocItems {

    fun from(note: Note): List<DocItem> {
        val items = ArrayList<DocItem>()
        if (note.title.isNotBlank()) items += DocItem.Heading(TextLevel.H1, note.title.trim())
        items += blocks(note.blocks)
        return items
    }

    /** The items for [blocks] alone, without a title. Used by note previews too. */
    fun blocks(blocks: List<com.stackpad.app.data.model.Block>): List<DocItem> {
        val items = ArrayList<DocItem>()
        blocks.filterNot { it.isBlank() }.forEach { block ->
            when (block) {
                is TextBlock -> items += if (block.level == TextLevel.PARAGRAPH) {
                    DocItem.Paragraph(block.text, block.spans)
                } else {
                    DocItem.Heading(block.level, block.text, block.spans)
                }
                is ListBlock -> block.items.filter { it.text.isNotBlank() }.forEachIndexed { index, item ->
                    items += DocItem.Row(ListMarkers.label(block.kind, index), item.text.trim())
                }
                is ChecklistBlock -> {
                    val tasks = block.items.filter { it.text.isNotBlank() }.map { item ->
                        val due = item.dueDate?.let { " ($it)" }.orEmpty()
                        DocItem.Checklist.Task(item.text.trim() + due, item.checked)
                    }
                    if (tasks.isNotEmpty() || block.title.isNotBlank()) items += DocItem.Checklist(block.title.trim(), tasks)
                }
                is ImageBlock -> items += DocItem.Image(block.fileName, block.caption.trim())
                is SheetBlock -> tableRows(block).takeIf { it.isNotEmpty() }?.let { items += DocItem.Table(it) }
                is AudioBlock -> items += DocItem.Audio("Audio note ${formatDuration(block.durationMs)}")
                is LinkBlock -> items += DocItem.Link(block.title.trim(), block.url.trim())
                is PromptBlock -> items += DocItem.Code(block.text.trim(), BlockTone.PROMPT)
                is CodeBlock -> items += DocItem.Code(block.text.trimEnd())
                is QuoteBlock -> items += DocItem.Quote(block.text.trim())
                is DividerBlock -> items += DocItem.Divider
            }
        }
        return items
    }

    private fun tableRows(block: SheetBlock): List<List<String>> {
        val evaluator = SheetEvaluator(block.cells)
        return block.cells.indices
            .map { r -> block.cells[r].indices.map { c -> evaluator.display(r, c).trim() } }
            .filter { row -> row.any { it.isNotEmpty() } }
    }
}
