package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.MarkdownParser
import com.stackpad.app.domain.importing.PlainTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownImportTest {
    @Test fun first_h1_becomes_the_title() {
        val r = MarkdownParser.parse("# My note\n\nHello")
        assertEquals("My note", r.title)
        assertEquals("Hello", (r.blocks.single() as TextBlock).text)
    }

    @Test fun front_matter_gives_title_and_tags() {
        val r = MarkdownParser.parse("---\ntitle: Plan\ntags: [a, b]\n---\nBody")
        assertEquals("Plan", r.title)
        assertEquals(listOf("a", "b"), r.tags)
        assertEquals("Body", (r.blocks.single() as TextBlock).text)
    }

    @Test fun headings_and_levels_are_capped() {
        val r = MarkdownParser.parse("text\n\n## Two\n\n#### Four")
        val levels = r.blocks.map { (it as TextBlock).level }
        assertEquals(listOf(TextLevel.PARAGRAPH, TextLevel.H2, TextLevel.H3), levels)
    }

    @Test fun inline_bold_italic_become_spans_without_markers() {
        val b = MarkdownParser.parse("a **bold** and *it* done").blocks.single() as TextBlock
        assertEquals("a bold and it done", b.text)
        assertTrue(b.spans.any { it.type == SpanType.BOLD && b.text.substring(it.start, it.end) == "bold" })
        assertTrue(b.spans.any { it.type == SpanType.ITALIC && b.text.substring(it.start, it.end) == "it" })
    }

    @Test fun lists_tasks_quotes_code_rules() {
        val md = "- a\n- b\n\n1. one\n2. two\n\n- [ ] todo\n- [x] done\n\n> quoted\n\n```kotlin\nval x = 1\n```\n\n---\n"
        val blocks = MarkdownParser.parse(md).blocks
        assertEquals(ListKind.BULLET, (blocks[0] as ListBlock).kind)
        assertEquals(2, (blocks[0] as ListBlock).items.size)
        assertEquals(ListKind.NUMBERED, (blocks[1] as ListBlock).kind)
        val tasks = blocks[2] as ChecklistBlock
        assertEquals(listOf(false, true), tasks.items.map { it.checked })
        assertEquals("quoted", (blocks[3] as QuoteBlock).text)
        assertEquals("val x = 1", (blocks[4] as CodeBlock).text)
        assertTrue(blocks[5] is DividerBlock)
    }

    @Test fun table_becomes_a_sheet() {
        val sheet = MarkdownParser.parse("| A | B |\n|---|---|\n| 1 | 2 |").blocks.single() as SheetBlock
        assertEquals(listOf(listOf("A", "B"), listOf("1", "2")), sheet.cells)
    }

    @Test fun local_and_obsidian_images_are_image_blocks_web_images_links() {
        val blocks = MarkdownParser.parse("![cap](img/a.png)\n\n![[b.jpg]]\n\n![w](https://x.com/p.png)").blocks
        assertEquals("img/a.png", (blocks[0] as ImageBlock).fileName)
        assertEquals("b.jpg", (blocks[1] as ImageBlock).fileName)
        assertTrue(blocks[2] is LinkBlock)
    }

    @Test fun plain_text_splits_paragraphs() {
        val blocks = PlainTextParser.parse("one\ntwo\n\nthree\n\n\n")
        assertEquals(listOf("one\ntwo", "three"), blocks.map { (it as TextBlock).text })
        assertEquals("My file", PlainTextParser.titleFromFileName("My file.txt"))
    }

    @Test fun picture_links_are_decoded_and_ids_depend_on_content() {
        assertEquals("My pic.png", ImportRules.decodeReference("./My%20pic.png?raw=1#x"))
        assertEquals("a+b.png", ImportRules.decodeReference("a+b.png"))
        assertTrue(ImportRules.fileSourceId("a.md", byteArrayOf(1)) != ImportRules.fileSourceId("a.md", byteArrayOf(2)))
        assertEquals(ImportRules.fileSourceId("a.md", byteArrayOf(1)), ImportRules.fileSourceId("a.md", byteArrayOf(1)))
        assertTrue(ImportRules.isMarkdown("X.MD") && ImportRules.isText("n.txt") && !ImportRules.isText("n.md"))
    }
}
