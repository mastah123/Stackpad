package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel

/**
 * Builds a note's blocks in reading order for every importer. Blank text is dropped, and
 * consecutive list items (or tasks) of the same kind join into one list (or checklist).
 */
class BlockAssembler {
    private val blocks = ArrayList<Block>()
    private var listKind: ListKind? = null
    private val listItems = ArrayList<ListItem>()
    private val tasks = ArrayList<ChecklistItem>()

    val isEmpty: Boolean get() = blocks.isEmpty() && listItems.isEmpty() && tasks.isEmpty()

    fun text(styled: StyledText, level: TextLevel = TextLevel.PARAGRAPH) {
        if (styled.isBlank) return
        flushLists()
        blocks += TextBlock(BlockFactory.newId(), level, styled.text, styled.spans)
    }

    fun text(plain: String, level: TextLevel = TextLevel.PARAGRAPH) = text(StyledText(plain.trim()), level)

    fun listItem(kind: ListKind, text: String) {
        if (text.isBlank()) return
        if (tasks.isNotEmpty() || (listKind != null && listKind != kind)) flushLists()
        listKind = kind
        listItems += ListItem(BlockFactory.newId(), text.trim())
    }

    fun task(text: String, checked: Boolean) {
        if (text.isBlank()) return
        if (listItems.isNotEmpty()) flushLists()
        tasks += ChecklistItem(BlockFactory.newId(), text.trim(), checked)
    }

    fun quote(text: String) {
        if (text.isBlank()) return
        flushLists()
        blocks += QuoteBlock(BlockFactory.newId(), text.trim())
    }

    fun code(text: String) {
        if (text.isBlank()) return
        flushLists()
        blocks += CodeBlock(BlockFactory.newId(), text.trimEnd())
    }

    fun divider() {
        flushLists()
        blocks += DividerBlock(BlockFactory.newId())
    }

    fun table(rows: List<List<String>>) {
        val kept = rows.filter { row -> row.any { it.isNotBlank() } }
        if (kept.isEmpty()) return
        val width = kept.maxOf { it.size }
        flushLists()
        blocks += SheetBlock(BlockFactory.newId(), kept.map { row -> row + List(width - row.size) { "" } })
    }

    /** An image whose file is named [ref] inside the source; the installer swaps it for a stored file. */
    fun image(ref: String) {
        flushLists()
        blocks += ImageBlock(BlockFactory.newId(), ref)
    }

    fun link(title: String, url: String) {
        if (url.isBlank()) return
        flushLists()
        blocks += LinkBlock(BlockFactory.newId(), title.trim().ifBlank { url.trim() }, url.trim())
    }

    fun build(): List<Block> {
        flushLists()
        return blocks.toList()
    }

    private fun flushLists() {
        val kind = listKind
        if (kind != null && listItems.isNotEmpty()) {
            blocks += ListBlock(BlockFactory.newId(), kind, listItems.toList())
        }
        if (tasks.isNotEmpty()) blocks += ChecklistBlock(BlockFactory.newId(), "", tasks.toList())
        listKind = null
        listItems.clear()
        tasks.clear()
    }
}
