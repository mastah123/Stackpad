package com.stackpad.app.domain

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.export.DocItem
import com.stackpad.app.domain.export.DocItems
import com.stackpad.app.domain.share.ExportPalette
import com.stackpad.app.domain.share.ShareCleanUp
import com.stackpad.app.domain.share.ShareDetails
import com.stackpad.app.domain.share.ShareOptions
import com.stackpad.app.domain.share.ShareOptionsCodec
import com.stackpad.app.domain.share.SharePageSize
import com.stackpad.app.domain.share.ShareTextSize
import com.stackpad.app.domain.share.ShareTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

class ShareOptionsTest {
    private val zone = ZoneId.of("UTC")
    private fun note(vararg blocks: Block, title: String = "T", tags: List<String> = emptyList(), created: Long = 0, updated: Long = 0) =
        Note("n", title, blocks.toList(), false, created, updated, tags = tags)

    private fun items(options: ShareOptions, vararg blocks: Block) = DocItems.from(note(*blocks, title = ""), options, zone, Locale.US)

    // region codec

    @Test fun options_survive_encoding() {
        val o = ShareOptions(
            theme = ShareTheme.APP, noteTint = false, textSize = ShareTextSize.LARGE, includePictures = false,
            hideCompletedTasks = true, pageSize = SharePageSize.LETTER, pageNumbers = true,
            cleanUp = ShareCleanUp(true, false, true, false, true),
            showTitle = false, showWordCount = true, showDates = true, showTags = true, showFooter = true,
        )
        assertEquals(o, ShareOptionsCodec.decode(ShareOptionsCodec.encode(o)))
        assertEquals(ShareOptions(), ShareOptionsCodec.decode(ShareOptionsCodec.encode(ShareOptions())))
    }

    @Test fun broken_or_missing_storage_gives_the_defaults() {
        assertEquals(ShareOptions(), ShareOptionsCodec.decode(null))
        assertEquals(ShareOptions(), ShareOptionsCodec.decode(""))
        assertEquals(ShareOptions(), ShareOptionsCodec.decode("garbage;=;theme=;x"))
        assertEquals(ShareTheme.LIGHT, ShareOptionsCodec.decode("theme=PURPLE").theme)
        assertEquals(ShareTheme.DARK, ShareOptionsCodec.decode("theme=DARK;bogus=1;title=2").theme)
        assertTrue(ShareOptionsCodec.decode("title=2").showTitle)
    }

    // endregion

    // region defaults copy the note

    @Test fun defaults_keep_empty_lines_items_cards_and_tables() {
        val result = items(
            ShareOptions(),
            TextBlock("a", text = ""),
            ListBlock("l", ListKind.BULLET, listOf(ListItem("1", "x"), ListItem("2", ""))),
            ChecklistBlock("c", "", listOf(ChecklistItem("1", ""))),
            SheetBlock("s", listOf(listOf("", ""), listOf("a", ""))),
            QuoteBlock("q", text = ""),
            PromptBlock("p", text = ""),
            CodeBlock("k", text = ""),
            LinkBlock("k2", "", ""),
        )
        assertEquals(DocItem.Spacer, result[0])
        assertEquals(listOf("x", ""), result.filterIsInstance<DocItem.Row>().map { it.text })
        assertEquals(1, result.filterIsInstance<DocItem.Checklist>().size)
        assertEquals(2, result.filterIsInstance<DocItem.Table>().single().rows.size)
        assertEquals(1, result.filterIsInstance<DocItem.Quote>().size)
        assertEquals(2, result.filterIsInstance<DocItem.Code>().size)
        assertEquals(1, result.filterIsInstance<DocItem.Link>().size)
    }

    // endregion

    // region clean up, one tick at a time

    @Test fun empty_lines_tick_removes_only_blank_text_blocks() {
        val blocks = arrayOf(TextBlock("a", text = " "), TextBlock("b", text = "Hi"), ListBlock("l", ListKind.BULLET, listOf(ListItem("1", ""))))
        val result = items(ShareOptions(cleanUp = ShareCleanUp(emptyLines = true)), *blocks)
        assertEquals(listOf("Hi"), result.filterIsInstance<DocItem.Paragraph>().map { it.text })
        assertEquals(1, result.filterIsInstance<DocItem.Row>().size) // the empty list item stays
        assertFalse(result.contains(DocItem.Spacer))
    }

    @Test fun empty_list_items_tick_drops_items_and_renumbers() {
        val list = ListBlock("l", ListKind.NUMBERED, listOf(ListItem("1", "a"), ListItem("2", ""), ListItem("3", "c")))
        val result = items(ShareOptions(cleanUp = ShareCleanUp(emptyListItems = true)), list)
        assertEquals(listOf("1." to "a", "2." to "c"), result.map { (it as DocItem.Row).marker to it.text })
        assertTrue(items(ShareOptions(cleanUp = ShareCleanUp(emptyListItems = true)), ListBlock("e", ListKind.BULLET, listOf(ListItem("1", "")))).isEmpty())
    }

    @Test fun empty_checklist_tick_drops_blank_items_and_empty_checklists() {
        val opts = ShareOptions(cleanUp = ShareCleanUp(emptyChecklists = true))
        val mixed = items(opts, ChecklistBlock("c", "Todo", listOf(ChecklistItem("1", "a"), ChecklistItem("2", ""))))
        assertEquals(1, (mixed.single() as DocItem.Checklist).tasks.size)
        assertTrue(items(opts, ChecklistBlock("e", "", listOf(ChecklistItem("1", "")))).isEmpty())
        // A title alone keeps the card.
        assertEquals(1, items(opts, ChecklistBlock("t", "Title", emptyList())).size)
    }

    @Test fun empty_table_tick_drops_blank_rows_and_empty_tables() {
        val opts = ShareOptions(cleanUp = ShareCleanUp(emptyTables = true))
        val table = items(opts, SheetBlock("s", listOf(listOf("", ""), listOf("a", ""))))
        assertEquals(listOf(listOf("a", "")), (table.single() as DocItem.Table).rows)
        assertTrue(items(opts, SheetBlock("e", listOf(listOf("", "")))).isEmpty())
    }

    @Test fun empty_cards_tick_drops_empty_quote_prompt_code_and_link_only() {
        val result = items(
            ShareOptions(cleanUp = ShareCleanUp(emptyCards = true)),
            QuoteBlock("q", text = ""), PromptBlock("p", text = " "), CodeBlock("k", text = ""), LinkBlock("l", "", ""),
            QuoteBlock("q2", text = "kept"), LinkBlock("l2", "Site", "https://x.y"),
        )
        assertEquals(2, result.size)
    }

    // endregion

    // region look

    @Test fun hide_completed_removes_done_tasks_and_cards_with_nothing_left() {
        val opts = ShareOptions(hideCompletedTasks = true)
        val part = items(opts, ChecklistBlock("c", "T", listOf(ChecklistItem("1", "a", true), ChecklistItem("2", "b"))))
        assertEquals(listOf("b"), (part.single() as DocItem.Checklist).tasks.map { it.text })
        assertTrue(items(opts, ChecklistBlock("d", "T", listOf(ChecklistItem("1", "a", true), ChecklistItem("2", "b", true)))).isEmpty())
        // An empty checklist is not "all done".
        assertEquals(1, items(opts, ChecklistBlock("e", "T", emptyList())).size)
    }

    @Test fun pictures_can_be_left_out() {
        val picture = ImageBlock("i", "a.jpg")
        assertEquals(1, items(ShareOptions(), picture).size)
        assertTrue(items(ShareOptions(includePictures = false), picture).isEmpty())
    }

    // endregion

    // region details

    @Test fun title_can_be_hidden() {
        val n = note(TextBlock("a", text = "Hi"), title = "Plan")
        assertEquals(2, DocItems.from(n, ShareOptions(), zone, Locale.US).size)
        assertEquals(1, DocItems.from(n, ShareOptions(showTitle = false), zone, Locale.US).size)
    }

    @Test fun word_count_dates_and_tags_lines() {
        val n = note(TextBlock("a", text = "one two three"), title = "Plan", tags = listOf("work", " ", "home"), created = 0L, updated = 86_400_000L * 2)
        val all = ShareOptions(showWordCount = true, showDates = true, showTags = true)
        assertEquals(
            listOf("4 words · Created Jan 1, 1970 · Edited Jan 3, 1970", "Tags: work, home"),
            ShareDetails.lines(n, all, zone, Locale.US),
        )
        assertEquals(listOf("4 words"), ShareDetails.lines(n, ShareOptions(showWordCount = true), zone, Locale.US))
        assertTrue(ShareDetails.lines(n, ShareOptions(), zone, Locale.US).isEmpty())
        assertEquals(listOf("1 word"), ShareDetails.lines(note(TextBlock("a", text = "one"), title = ""), ShareOptions(showWordCount = true), zone, Locale.US))
    }

    @Test fun details_come_after_the_title_and_footer_is_last() {
        val n = note(TextBlock("a", text = "Hi"), title = "Plan")
        val result = DocItems.from(n, ShareOptions(showWordCount = true, showFooter = true), zone, Locale.US)
        assertTrue(result[0] is DocItem.Heading)
        assertEquals(DocItem.Meta("2 words"), result[1])
        assertEquals(DocItem.Meta(ShareDetails.FOOTER, centered = true), result.last())
    }

    // endregion

    // region palette

    @Test fun palette_follows_the_theme_option() {
        assertFalse(ExportPalette.forShare(ShareOptions(theme = ShareTheme.LIGHT), appIsDark = true, noteAccent = null).page == 0xFF121218.toInt())
        assertEquals(0xFF121218.toInt(), ExportPalette.forShare(ShareOptions(theme = ShareTheme.DARK), false, null).page)
        assertEquals(0xFF121218.toInt(), ExportPalette.forShare(ShareOptions(theme = ShareTheme.APP), true, null).page)
        assertEquals(0xFFFFFFFF.toInt(), ExportPalette.forShare(ShareOptions(theme = ShareTheme.APP), false, null).page)
    }

    @Test fun note_tint_washes_the_page_only_when_on() {
        val red = 0xFFE57373.toInt()
        val plain = ExportPalette.forShare(ShareOptions(noteTint = false), false, red)
        val tinted = ExportPalette.forShare(ShareOptions(noteTint = true), false, red)
        assertEquals(0xFFFFFFFF.toInt(), plain.page)
        assertTrue(tinted.page != plain.page)
        assertEquals(0xFF, (tinted.page shr 24) and 0xFF)
        // Red stays the strongest channel, but the page is still light.
        assertTrue(((tinted.page shr 8) and 0xFF) > 0xC0)
    }

    @Test fun over_blends_channels() {
        assertEquals(0xFF7F7F7F.toInt(), ExportPalette.over(0xFF000000.toInt(), 0.5f, 0xFFFFFFFF.toInt()))
        assertEquals(0xFF102030.toInt(), ExportPalette.over(0xFF102030.toInt(), 1f, 0xFFFFFFFF.toInt()))
    }

    // endregion
}
