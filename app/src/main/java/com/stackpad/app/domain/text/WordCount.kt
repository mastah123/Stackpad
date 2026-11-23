package com.stackpad.app.domain.text

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.TextBlock

/** Counts the words a person wrote in a note: the title and the text of every block. Table cells and URLs are not counted. */
object WordCount {

    fun of(title: String, blocks: List<Block>): Int =
        words(title) + blocks.sumOf { words(it) }

    private fun words(block: Block): Int = when (block) {
        is TextBlock -> words(block.text)
        is ListBlock -> block.items.sumOf { words(it.text) }
        is ChecklistBlock -> words(block.title) + block.items.sumOf { words(it.text) }
        is LinkBlock -> words(block.title)
        is PromptBlock -> words(block.text)
        is CodeBlock -> words(block.text)
        is QuoteBlock -> words(block.text)
        else -> 0
    }

    private fun words(text: String): Int = text.split(Regex("\\s+")).count { it.isNotEmpty() }
}
