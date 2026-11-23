package com.stackpad.app.domain

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.text.HeadingSpans
import com.stackpad.app.domain.text.TextRuns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextRunsTest {
    private fun t(id: String, text: String, level: TextLevel = TextLevel.PARAGRAPH, spans: List<TextSpan> = emptyList()) =
        TextBlock(id, level, text, spans)

    @Test fun neighbouringTextBlocksJoinIntoOneAndOthersSplitRuns() {
        val blocks: List<Block> = listOf(t("a", "one"), t("b", "two"), ImageBlock("i", "x.jpg"), t("c", "three"))
        val result = TextRuns.normalize(blocks)
        assertEquals(3, result.blocks.size)
        assertEquals("one\ntwo", (result.blocks[0] as TextBlock).text)
        assertEquals("a", result.blocks[0].id)
        assertTrue(result.blocks[1] is ImageBlock)
        assertEquals("three", (result.blocks[2] as TextBlock).text)
        assertEquals(TextRuns.Moved("a", 4), result.moved["b"])
    }

    @Test fun olderHeadingLevelBecomesALineHeadingAndSpansMove() {
        val blocks = listOf(
            t("a", "Title", TextLevel.H1),
            t("b", "bold", spans = listOf(TextSpan(0, 4, SpanType.BOLD))),
        )
        val merged = TextRuns.normalize(blocks).blocks.single() as TextBlock
        assertEquals("Title\nbold", merged.text)
        assertEquals(TextLevel.PARAGRAPH, merged.level)
        assertEquals(setOf(TextSpan(0, 5, SpanType.H1), TextSpan(6, 10, SpanType.BOLD)), merged.spans.toSet())
        assertEquals(listOf(TextLevel.H1, TextLevel.PARAGRAPH), HeadingSpans.lineLevels(merged.text, merged.spans))
    }

    @Test fun normalizingTwiceChangesNothing() {
        val once = TextRuns.normalize(listOf(t("a", "x", TextLevel.H2), t("b", "y"))).blocks
        assertEquals(once, TextRuns.normalize(once).blocks)
    }

    @Test fun blankLinesAreKept() {
        val merged = TextRuns.normalize(listOf(t("a", "x"), t("b", ""), t("c", "y"))).blocks.single() as TextBlock
        assertEquals("x\n\ny", merged.text)
    }

    @Test fun splitAroundDropsOneBreakAtEachCut() {
        val block = t("a", "one\ntwo\nthree", spans = listOf(TextSpan(4, 7, SpanType.BOLD)))
        val (before, after) = TextRuns.splitAround(block, 4)
        assertEquals("one", before!!.text)
        assertEquals("two\nthree", after!!.text)
        assertEquals(listOf(TextSpan(0, 3, SpanType.BOLD)), after.spans)
        assertEquals("a", before.id)
    }

    @Test fun splitAroundEdgesGiveNullSides() {
        val block = t("a", "hello")
        assertNull(TextRuns.splitAround(block, 0).first)
        assertEquals("hello", TextRuns.splitAround(block, 0).second!!.text)
        assertNull(TextRuns.splitAround(block, 5).second)
        assertNull(TextRuns.splitAround(t("a", ""), 0).first)
    }

    @Test fun splitAroundAtLineEndKeepsTheBlankLineAfter() {
        val (before, after) = TextRuns.splitAround(t("a", "a\n\nb"), 1)
        assertEquals("a", before!!.text)
        assertEquals("\nb", after!!.text)
    }

    @Test fun expandGivesOneBlockPerLineWithLevels() {
        val block = t("a", "Head\nbody", spans = listOf(TextSpan(0, 4, SpanType.H2), TextSpan(5, 9, SpanType.ITALIC)))
        val lines = TextRuns.expand(listOf(block)).map { it as TextBlock }
        assertEquals(listOf("Head", "body"), lines.map { it.text })
        assertEquals(listOf(TextLevel.H2, TextLevel.PARAGRAPH), lines.map { it.level })
        assertEquals(emptyList<TextSpan>(), lines[0].spans)
        assertEquals(listOf(TextSpan(0, 4, SpanType.ITALIC)), lines[1].spans)
    }

    @Test fun expandStillReadsOlderLevels() {
        val lines = TextRuns.expand(listOf(t("a", "Old", TextLevel.H1))).map { it as TextBlock }
        assertEquals(TextLevel.H1, lines.single().level)
    }

    @Test fun typedTextJoinsTheHeadingLine() {
        val text = "Title!\nbody"
        val spans = listOf(TextSpan(0, 5, SpanType.H1))
        assertEquals(listOf(TextSpan(0, 6, SpanType.H1)), HeadingSpans.normalize(text, spans))
    }

    @Test fun enterInTheMiddleOfAHeadingKeepsBothHalvesHeadings() {
        val spans = listOf(TextSpan(0, 5, SpanType.H1)) // after Enter the text is "Ti\ntle" and the span still runs over it
        val fixed = HeadingSpans.normalize("Ti\ntle", spans)
        assertEquals(listOf(TextLevel.H1, TextLevel.H1), HeadingSpans.lineLevels("Ti\ntle", fixed))
    }

    @Test fun enterAtTheEndOfAHeadingStartsAParagraph() {
        val fixed = HeadingSpans.normalize("Title\n", listOf(TextSpan(0, 5, SpanType.H1)))
        assertEquals(listOf(TextLevel.H1, TextLevel.PARAGRAPH), HeadingSpans.lineLevels("Title\n", fixed))
    }

    @Test fun toggleLevelAppliesToEveryTouchedLineAndRemovesWhenAllHaveIt() {
        val text = "a\nb\nc"
        val on = HeadingSpans.toggleLevel(text, emptyList(), 0, 3, TextLevel.H2)
        assertEquals(listOf(TextLevel.H2, TextLevel.H2, TextLevel.PARAGRAPH), HeadingSpans.lineLevels(text, on))
        val off = HeadingSpans.toggleLevel(text, on, 0, 3, TextLevel.H2)
        assertEquals(listOf(TextLevel.PARAGRAPH, TextLevel.PARAGRAPH, TextLevel.PARAGRAPH), HeadingSpans.lineLevels(text, off))
        assertEquals(TextLevel.H2, HeadingSpans.commonLevel(text, on, 0, 3))
        assertNull(HeadingSpans.commonLevel(text, on, 0, 5))
    }

    @Test fun selectionEndingAtALineStartDoesNotTouchThatLine() {
        val text = "a\nb"
        val on = HeadingSpans.toggleLevel(text, emptyList(), 0, 2, TextLevel.H1)
        assertEquals(listOf(TextLevel.H1, TextLevel.PARAGRAPH), HeadingSpans.lineLevels(text, on))
    }

    @Test fun levelAtUsesTheLineOfTheCaret() {
        val text = "Head\nbody"
        val spans = listOf(TextSpan(0, 4, SpanType.H3))
        assertEquals(TextLevel.H3, HeadingSpans.levelAt(text, spans, 4))
        assertEquals(TextLevel.PARAGRAPH, HeadingSpans.levelAt(text, spans, 5))
    }

    private val run = t("a", "Head\nbody\nmore", spans = listOf(TextSpan(0, 4, SpanType.H2)))

    @Test fun exportAndPreviewSeeEachLineOfARun() {
        val items = com.stackpad.app.domain.export.DocItems.blocks(listOf(run))
        assertEquals(
            listOf(
                com.stackpad.app.domain.export.DocItem.Heading(TextLevel.H2, "Head"),
                com.stackpad.app.domain.export.DocItem.Paragraph("body"),
                com.stackpad.app.domain.export.DocItem.Paragraph("more"),
            ),
            items,
        )
        val note = com.stackpad.app.data.model.Note("n", "", listOf(run), false, 0, 0)
        assertEquals("## Head\n\nbody\n\nmore", com.stackpad.app.domain.export.NoteExporter.export(note, com.stackpad.app.domain.export.ExportFormat.MARKDOWN))
        assertEquals("Head\nbody\nmore", com.stackpad.app.domain.export.NoteExporter.export(note, com.stackpad.app.domain.export.ExportFormat.PLAIN))
    }
}
