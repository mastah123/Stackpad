package com.stackpad.app.domain

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.text.TextSplitting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSplittingTest {
    @Test
    fun splitsAtEnterAndMovesCaretToTheNewLine() {
        val result = TextSplitting.splitLines("abc\ndef", emptyList(), caret = 4)
        assertEquals(listOf("abc", "def"), result.lines.map { it.text })
        assertEquals(1, result.caretLine)
        assertEquals(0, result.caretOffset)
    }

    @Test
    fun rebasesSpansOntoEachLine() {
        val spans = listOf(TextSpan(1, 6, SpanType.BOLD))
        val result = TextSplitting.splitLines("abc\ndef", spans, caret = 4)
        assertEquals(listOf(TextSpan(1, 3, SpanType.BOLD)), result.lines[0].spans)
        assertEquals(listOf(TextSpan(0, 2, SpanType.BOLD)), result.lines[1].spans)
    }

    @Test
    fun trailingNewlineCreatesAnEmptyLine() {
        val result = TextSplitting.splitLines("abc\n", emptyList(), caret = 4)
        assertEquals(listOf("abc", ""), result.lines.map { it.text })
        assertEquals(1, result.caretLine)
        assertEquals(0, result.caretOffset)
    }

    @Test
    fun pastingSeveralLinesPutsCaretAtTheEndOfTheLastOne() {
        val result = TextSplitting.splitLines("a\nb\nc", emptyList(), caret = 5)
        assertEquals(3, result.lines.size)
        assertEquals(2, result.caretLine)
        assertEquals(1, result.caretOffset)
    }

    @Test
    fun caretBeforeTheNewlineStaysOnItsLine() {
        val result = TextSplitting.splitLines("abc\ndef", emptyList(), caret = 3)
        assertEquals(0, result.caretLine)
        assertEquals(3, result.caretOffset)
    }

    @Test
    fun textWithoutNewlinesIsOneLine() {
        val result = TextSplitting.splitLines("hello", emptyList(), caret = 2)
        assertEquals(1, result.lines.size)
        assertEquals(2, result.caretOffset)
    }

    @Test
    fun multilineBlocksFromOlderNotesAreSplit() {
        val blocks: List<Block> = listOf(TextBlock("1", TextLevel.H1, "a\nb"), ImageBlock("2", "x.jpg"))
        val result = TextSplitting.splitMultilineBlocks(blocks)
        assertEquals(3, result.size)
        val first = result[0] as TextBlock
        val second = result[1] as TextBlock
        assertEquals("1", first.id)
        assertEquals(TextLevel.H1, first.level)
        assertEquals("a", first.text)
        assertEquals(TextLevel.PARAGRAPH, second.level)
        assertEquals("b", second.text)
        assertTrue(result[2] is ImageBlock)
    }
}
