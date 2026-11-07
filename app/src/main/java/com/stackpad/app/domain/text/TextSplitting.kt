package com.stackpad.app.domain.text

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan

/** One line produced by splitting text, with its formatting spans re-based to the line start. */
data class TextLine(val text: String, val spans: List<TextSpan>)

/** The lines of a split, and which line / offset the caret ends up in. */
data class SplitResult(val lines: List<TextLine>, val caretLine: Int, val caretOffset: Int)

/**
 * Text blocks hold exactly one line. Pressing Enter (or pasting several lines) splits the
 * text into separate blocks, which is what lets every line have its own heading level.
 */
object TextSplitting {

    fun splitLines(text: String, spans: List<TextSpan>, caret: Int): SplitResult {
        val safeCaret = caret.coerceIn(0, text.length)
        val lines = ArrayList<TextLine>()
        var caretLine = 0
        var caretOffset = 0
        var start = 0
        while (true) {
            val newline = text.indexOf('\n', start)
            val end = if (newline < 0) text.length else newline
            val lineSpans = spans.mapNotNull { span ->
                val from = maxOf(span.start, start)
                val to = minOf(span.end, end)
                if (to > from) span.copy(start = from - start, end = to - start) else null
            }
            lines.add(TextLine(text.substring(start, end), lineSpans))
            if (safeCaret in start..end) {
                caretLine = lines.lastIndex
                caretOffset = safeCaret - start
            }
            if (newline < 0) break
            start = end + 1
        }
        return SplitResult(lines, caretLine, caretOffset)
    }

    /** Splits every text block containing line breaks (e.g. from older notes) into one block per line. */
    fun splitMultilineBlocks(blocks: List<Block>): List<Block> = blocks.flatMap { block ->
        if (block is TextBlock && '\n' in block.text) {
            splitLines(block.text, block.spans, caret = 0).lines.mapIndexed { index, line ->
                TextBlock(
                    id = if (index == 0) block.id else BlockFactory.newId(),
                    level = if (index == 0) block.level else TextLevel.PARAGRAPH,
                    text = line.text,
                    spans = line.spans,
                )
            }
        } else {
            listOf(block)
        }
    }
}
