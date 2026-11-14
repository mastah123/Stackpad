package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.export.DocItem
import com.stackpad.app.domain.export.DocItems
import org.junit.Assert.assertEquals
import org.junit.Test

class DocItemsTest {
    private fun note(title: String, vararg blocks: com.stackpad.app.data.model.Block) =
        Note(id = "n", title = title, blocks = blocks.toList(), pinned = false, createdAt = 0, updatedAt = 0)

    @Test fun title_becomes_heading_and_blank_blocks_are_skipped() {
        val items = DocItems.from(note("Plan", TextBlock("a", text = ""), TextBlock("b", text = "Hi")))
        assertEquals(2, items.size)
        assertEquals("Plan", (items[0] as DocItem.Heading).text)
        assertEquals("Hi", (items[1] as DocItem.Paragraph).text)
    }

    @Test fun list_rows_use_markers() {
        val items = DocItems.from(note("", ListBlock("l", ListKind.NUMBERED, listOf(ListItem("1", "x"), ListItem("2", "y")))))
        assertEquals(listOf("1.", "2."), items.map { (it as DocItem.Row).marker })
    }

    @Test fun checklist_shows_state() {
        val items = DocItems.from(note("", ChecklistBlock("c", items = listOf(ChecklistItem("1", "a", true), ChecklistItem("2", "b")))))
        assertEquals(listOf("☑", "☐"), items.map { (it as DocItem.Row).marker })
    }

    @Test fun formulas_are_evaluated_in_tables() {
        val items = DocItems.from(note("", SheetBlock("s", listOf(listOf("1", "2", "=A1+B1")))))
        assertEquals(listOf(listOf("1", "2", "3")), (items[0] as DocItem.Table).rows)
    }

    @Test fun divider_is_kept() {
        assertEquals(listOf(DocItem.Divider), DocItems.from(note("", DividerBlock("d"))))
    }
}
