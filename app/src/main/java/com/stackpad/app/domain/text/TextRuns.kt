package com.stackpad.app.domain.text

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan

/**
 * A note keeps each run of plain text in ONE text block (one line per line break); another kind of
 * block (table, list, picture...) is what ends a run. These helpers keep that shape and convert
 * between it and the older one-line-per-block shape.
 */
object TextRuns {
    /** Where a block that was merged into an earlier one now lives. */
    data class Moved(val into: String, val offset: Int)

    data class Normalized(val blocks: List<Block>, val moved: Map<String, Moved>)

    /**
     * Turns older heading levels into line headings, then joins neighbouring text blocks into one
     * (separated by a line break). Safe to run again: a second run changes nothing.
     */
    fun normalize(blocks: List<Block>): Normalized {
        val out = ArrayList<Block>()
        val moved = LinkedHashMap<String, Moved>()
        for (original in blocks) {
            val block = (original as? TextBlock)?.let(::withoutLevel) ?: original
            val last = out.lastOrNull()
            if (block is TextBlock && last is TextBlock) {
                val offset = last.text.length + 1
                out[out.lastIndex] = last.copy(
                    text = last.text + "\n" + block.text,
                    spans = last.spans + block.spans.map { it.copy(start = it.start + offset, end = it.end + offset) },
                )
                moved[block.id] = Moved(last.id, offset)
                // Blocks already merged into the block that was just extended keep pointing at it.
            } else {
                out += block
            }
        }
        return Normalized(out, moved)
    }

    /** An older block's heading level becomes a heading span on its first line. */
    private fun withoutLevel(block: TextBlock): TextBlock {
        if (block.level == TextLevel.PARAGRAPH) return block
        val firstEnd = block.text.indexOf('\n').let { if (it < 0) block.text.length else it }
        val type = HeadingSpans.typeOf(block.level)
        val spans = if (type != null && firstEnd > 0) block.spans + TextSpan(0, firstEnd, type) else block.spans
        return block.copy(level = TextLevel.PARAGRAPH, spans = spans)
    }

    /**
     * Splits [block] at [at] for inserting another block there: the text before and after the caret,
     * with one line break dropped at each cut. A side with no text is null.
     */
    fun splitAround(block: TextBlock, at: Int): Pair<TextBlock?, TextBlock?> {
        val cut = at.coerceIn(0, block.text.length)
        val beforeEnd = if (cut > 0 && block.text[cut - 1] == '\n') cut - 1 else cut
        val afterStart = if (cut < block.text.length && block.text[cut] == '\n') cut + 1 else cut
        val before = slice(block, 0, beforeEnd, block.id)
        val after = slice(block, afterStart, block.text.length, BlockFactory.newId())
        return before.takeIf { it.text.isNotEmpty() } to after.takeIf { it.text.isNotEmpty() }
    }

    private fun slice(block: TextBlock, from: Int, to: Int, id: String): TextBlock = block.copy(
        id = id,
        text = block.text.substring(from, to),
        spans = block.spans.mapNotNull { span ->
            val start = maxOf(span.start, from)
            val end = minOf(span.end, to)
            if (end > start) span.copy(start = start - from, end = end - from) else null
        },
    )

    /** One single-line block per line, with heading spans turned back into [TextLevel] (for export and previews). */
    fun toLineBlocks(block: TextBlock): List<TextBlock> {
        val levels = HeadingSpans.lineLevels(block.text, block.spans)
        val lines = TextSplitting.splitLines(block.text, block.spans, caret = 0).lines
        return lines.mapIndexed { index, line ->
            val own = levels.getOrElse(index) { TextLevel.PARAGRAPH }
            val legacy = if (index == 0 && own == TextLevel.PARAGRAPH) block.level else own
            TextBlock(
                id = if (index == 0) block.id else "${block.id}:$index",
                level = legacy,
                text = line.text,
                spans = line.spans.filterNot { HeadingSpans.isHeading(it.type) },
            )
        }
    }

    /** [blocks] with every text block expanded by [toLineBlocks]; other blocks are untouched. */
    fun expand(blocks: List<Block>): List<Block> =
        blocks.flatMap { if (it is TextBlock) toLineBlocks(it) else listOf(it) }
}
