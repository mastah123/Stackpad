package com.stackpad.app.domain.export

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
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
import com.stackpad.app.domain.blocks.BlockTone
import com.stackpad.app.domain.formula.SheetEvaluator
import com.stackpad.app.domain.share.ShareCleanUp
import com.stackpad.app.domain.share.ShareDetails
import com.stackpad.app.domain.share.ShareOptions
import com.stackpad.app.domain.text.ListMarkers
import com.stackpad.app.util.formatDuration
import java.time.ZoneId
import java.util.Locale

/**
 * Turns a note into [DocItem]s. With the default options everything the note shows is kept,
 * including empty lines, empty list items and empty cards, so the export looks like the note;
 * the [ShareCleanUp] ticks leave those out. Table formulas show their results.
 */
object DocItems {

    fun from(
        note: Note,
        options: ShareOptions = ShareOptions(),
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): List<DocItem> {
        val items = ArrayList<DocItem>()
        if (options.showTitle && note.title.isNotBlank()) items += DocItem.Heading(TextLevel.H1, note.title.trim())
        ShareDetails.lines(note, options, zone, locale).forEach { items += DocItem.Meta(it) }
        items += blocks(note.blocks, options.cleanUp, options.includePictures, options.hideCompletedTasks)
        if (options.showFooter) items += DocItem.Meta(ShareDetails.FOOTER, centered = true)
        return items
    }

    /** The items for [blocks] alone, without a title. Note previews use this with everything empty left out. */
    fun blocks(
        blocks: List<Block>,
        cleanUp: ShareCleanUp = ShareCleanUp.ALL,
        includePictures: Boolean = true,
        hideCompletedTasks: Boolean = false,
    ): List<DocItem> {
        val items = ArrayList<DocItem>()
        blocks.forEach { block ->
            when (block) {
                is TextBlock -> when {
                    block.text.isBlank() -> if (!cleanUp.emptyLines) items += DocItem.Spacer
                    block.level == TextLevel.PARAGRAPH -> items += DocItem.Paragraph(block.text, block.spans)
                    else -> items += DocItem.Heading(block.level, block.text, block.spans)
                }
                is ListBlock -> {
                    val shown = if (cleanUp.emptyListItems) block.items.filter { it.text.isNotBlank() } else block.items
                    val longest = shown.indices.maxOfOrNull { ListMarkers.label(block.kind, it).length } ?: 1
                    shown.forEachIndexed { index, item ->
                        items += DocItem.Row(ListMarkers.label(block.kind, index), item.text.trim(), longest)
                    }
                }
                is ChecklistBlock -> checklist(block, cleanUp, hideCompletedTasks)?.let { items += it }
                is ImageBlock -> if (includePictures) items += DocItem.Image(block.fileName)
                is SheetBlock -> table(block, cleanUp)?.let { items += it }
                is AudioBlock -> items += DocItem.Audio("Audio note ${formatDuration(block.durationMs)}")
                is LinkBlock -> card(cleanUp, block.title.isBlank() && block.url.isBlank()) {
                    items += DocItem.Link(block.title.trim(), block.url.trim())
                }
                is PromptBlock -> card(cleanUp, block.text.isBlank()) { items += DocItem.Code(block.text.trim(), BlockTone.PROMPT) }
                is CodeBlock -> card(cleanUp, block.text.isBlank()) { items += DocItem.Code(block.text.trimEnd()) }
                is QuoteBlock -> card(cleanUp, block.text.isBlank()) { items += DocItem.Quote(block.text.trim()) }
                is DividerBlock -> items += DocItem.Divider
            }
        }
        return items
    }

    private inline fun card(cleanUp: ShareCleanUp, empty: Boolean, add: () -> Unit) {
        if (!(empty && cleanUp.emptyCards)) add()
    }

    private fun checklist(block: ChecklistBlock, cleanUp: ShareCleanUp, hideCompleted: Boolean): DocItem.Checklist? {
        val filled = block.items.filter { it.text.isNotBlank() }
        // Everything is done and done tasks are hidden: nothing is left to show.
        if (hideCompleted && filled.isNotEmpty() && filled.all { it.checked }) return null

        var items = if (cleanUp.emptyChecklists) filled else block.items
        if (hideCompleted) items = items.filterNot { it.checked }
        val tasks = items.map { item ->
            val due = item.dueDate?.let { " ($it)" }.orEmpty()
            DocItem.Checklist.Task(item.text.trim() + due, item.checked)
        }
        if (cleanUp.emptyChecklists && tasks.isEmpty() && block.title.isBlank()) return null
        return DocItem.Checklist(block.title.trim(), tasks)
    }

    private fun table(block: SheetBlock, cleanUp: ShareCleanUp): DocItem.Table? {
        val evaluator = SheetEvaluator(block.cells)
        val rows = block.cells.indices.map { r -> block.cells[r].indices.map { c -> evaluator.display(r, c).trim() } }
        val shown = if (cleanUp.emptyTables) rows.filter { row -> row.any { it.isNotEmpty() } } else rows
        return shown.takeIf { it.isNotEmpty() }?.let { DocItem.Table(it) }
    }
}
