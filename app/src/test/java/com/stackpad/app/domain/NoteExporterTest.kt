package com.stackpad.app.domain

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.data.model.Block
import com.stackpad.app.domain.export.ExportFormat
import com.stackpad.app.domain.export.NoteExporter
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteExporterTest {
    private fun note(title: String, vararg blocks: Block) =
        Note("n", title, blocks.toList(), false, 0, 0)

    private fun md(title: String, vararg blocks: Block) = NoteExporter.export(note(title, *blocks), ExportFormat.MARKDOWN)
    private fun plain(title: String, vararg blocks: Block) = NoteExporter.export(note(title, *blocks), ExportFormat.PLAIN)

    @Test
    fun titleAndHeadings() {
        assertEquals(
            "# Trip\n\n# Big\n\n## Medium\n\n### Small\n\nbody",
            md(
                "Trip",
                TextBlock("1", TextLevel.H1, "Big"),
                TextBlock("2", TextLevel.H2, "Medium"),
                TextBlock("3", TextLevel.H3, "Small"),
                TextBlock("4", text = "body"),
            ),
        )
    }

    @Test
    fun plainTextKeepsLinesTogether() {
        assertEquals("Trip\n\nline one\nline two", plain("Trip", TextBlock("1", text = "line one"), TextBlock("2", text = "line two")))
    }

    @Test
    fun blankBlocksAndTitleAreSkipped() {
        assertEquals("hello", md("", TextBlock("1", text = ""), TextBlock("2", text = "hello"), TextBlock("3", text = "  ")))
    }

    @Test
    fun spansBecomeMarkdown() {
        val text = TextBlock(
            "1", text = "bold italic under both",
            spans = listOf(
                TextSpan(0, 4, SpanType.BOLD),
                TextSpan(5, 11, SpanType.ITALIC),
                TextSpan(12, 17, SpanType.UNDERLINE),
                TextSpan(18, 22, SpanType.BOLD),
                TextSpan(18, 22, SpanType.ITALIC),
            ),
        )
        assertEquals("**bold** *italic* <u>under</u> ***both***", md("", text))
        assertEquals("bold italic under both", plain("", text))
    }

    @Test
    fun spanWhitespaceStaysOutsideMarkers() {
        val text = TextBlock("1", text = "a bold word", spans = listOf(TextSpan(1, 7, SpanType.BOLD)))
        assertEquals("a **bold** word", md("", text))
    }

    @Test
    fun adjacentSameStyleSegmentsMerge() {
        val text = TextBlock(
            "1", text = "abcd",
            spans = listOf(TextSpan(0, 2, SpanType.BOLD), TextSpan(2, 4, SpanType.BOLD)),
        )
        assertEquals("**abcd**", md("", text))
    }

    @Test
    fun outOfRangeSpansAreClamped() {
        val text = TextBlock("1", text = "abc", spans = listOf(TextSpan(1, 99, SpanType.BOLD), TextSpan(5, 9, SpanType.ITALIC)))
        assertEquals("a**bc**", md("", text))
    }

    @Test
    fun lists() {
        val bullets = ListBlock("1", ListKind.BULLET, listOf(ListItem("a", "one"), ListItem("b", ""), ListItem("c", "two")))
        val numbers = ListBlock("2", ListKind.NUMBERED, listOf(ListItem("a", "x"), ListItem("b", "y")))
        val letters = ListBlock("3", ListKind.LETTERED, listOf(ListItem("a", "p"), ListItem("b", "q")))
        assertEquals("- one\n- two\n\n1. x\n2. y\n\na. p\nb. q", md("", bullets, numbers, letters))
        assertEquals("• one\n• two\n\n1. x\n2. y\n\na. p\nb. q", plain("", bullets, numbers, letters))
    }

    @Test
    fun checklistWithDueDate() {
        val list = ChecklistBlock(
            "1", "Groceries",
            listOf(ChecklistItem("a", "Milk", true), ChecklistItem("b", "Eggs", false, "2026-11-10")),
        )
        assertEquals("**Groceries**\n- [x] Milk\n- [ ] Eggs (due 2026-11-10)", md("", list))
        assertEquals("Groceries\n[x] Milk\n[ ] Eggs (due 2026-11-10)", plain("", list))
    }

    @Test
    fun imagesAudioAndDivider() {
        assertEquals("*[Image]*\n\n*[Image: Sunset]*\n\n[Audio note 1:05]\n\n---",
            md("", ImageBlock("1", "a.jpg"), ImageBlock("2", "b.jpg", "Sunset"), AudioBlock("3", "x.m4a", 65_000, emptyList()), DividerBlock("4")))
        assertEquals("[Image: Sunset]", plain("", ImageBlock("2", "b.jpg", "Sunset")))
    }

    @Test
    fun tableShowsFormulaResults() {
        val sheet = SheetBlock("1", listOf(listOf("Item", "Cost"), listOf("A", "2"), listOf("B", "3"), listOf("Total", "=SUM(B2:B3)")))
        assertEquals(
            "| Item | Cost |\n| --- | --- |\n| A | 2 |\n| B | 3 |\n| Total | 5 |",
            md("", sheet),
        )
        assertEquals("Item | Cost\nA | 2\nB | 3\nTotal | 5", plain("", sheet))
    }

    @Test
    fun tablePipesAreEscaped() {
        val sheet = SheetBlock("1", listOf(listOf("a|b", "c")))
        assertEquals("| a\\|b | c |\n| --- | --- |", md("", sheet))
    }

    @Test
    fun linksQuotesAndCode() {
        assertEquals("[Site](https://x.com)\n\nhttps://y.com\n\n> wise\n> words",
            md("", LinkBlock("1", "Site", "https://x.com"), LinkBlock("2", "", "https://y.com"), QuoteBlock("3", "wise\nwords")))
        assertEquals("Site: https://x.com", plain("", LinkBlock("1", "Site", "https://x.com")))
        assertEquals("```\nval x = 1\n```\n\n```\nsummarize\n```", md("", CodeBlock("1", "val x = 1\n"), PromptBlock("2", "summarize")))
        assertEquals("val x = 1\n\nsummarize", plain("", CodeBlock("1", "val x = 1\n"), PromptBlock("2", "summarize")))
    }

    @Test
    fun fenceGrowsWhenCodeContainsBackticks() {
        assertEquals("````\n```\ninner\n```\n````", md("", CodeBlock("1", "```\ninner\n```")))
    }

    @Test fun exportBlocksHasNoTitle() {
        val text = NoteExporter.exportBlocks(
            listOf(TextBlock("t1", text = "one"), TextBlock("t2", text = "two")),
            ExportFormat.MARKDOWN,
        )
        assertEquals("one\n\ntwo", text)
    }
}
