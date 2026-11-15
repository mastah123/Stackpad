package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.export.DocItem
import com.stackpad.app.domain.preview.NotePreview
import org.junit.Assert.assertEquals
import org.junit.Test

class NotePreviewTest {
    @Test fun keeps_only_the_first_items() {
        val blocks = (1..10).map { TextBlock("b$it", text = "line $it") }
        assertEquals(3, NotePreview.from(blocks, 3).size)
    }

    @Test fun blank_blocks_do_not_use_up_the_budget() {
        val blocks = listOf(TextBlock("a", text = ""), TextBlock("b", text = "x"), TextBlock("c", text = "y"))
        assertEquals(listOf("x", "y"), NotePreview.from(blocks, 2).map { (it as DocItem.Paragraph).text })
    }

    @Test fun tables_are_cut_to_three_rows() {
        val rows = (1..6).map { listOf("$it", "v") }
        val table = NotePreview.from(listOf(SheetBlock("s", rows)), 5)[0] as DocItem.Table
        assertEquals(3, table.rows.size)
    }

    @Test fun checklists_are_cut_to_four_tasks() {
        val tasks = (1..9).map { ChecklistItem("$it", "task $it", false) }
        val list = NotePreview.from(listOf(ChecklistBlock("c", "", tasks)), 5)[0] as DocItem.Checklist
        assertEquals(4, list.tasks.size)
    }

    @Test fun zero_budget_gives_nothing() = assertEquals(0, NotePreview.from(listOf(TextBlock("a", text = "x")), 0).size)
}
