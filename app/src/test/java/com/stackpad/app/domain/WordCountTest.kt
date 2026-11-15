package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.text.WordCount
import org.junit.Assert.assertEquals
import org.junit.Test

class WordCountTest {
    @Test fun empty_note_has_no_words() = assertEquals(0, WordCount.of("", emptyList()))

    @Test fun title_and_text_are_counted() =
        assertEquals(5, WordCount.of("My plan", listOf(TextBlock("a", text = "one  two\tthree"))))

    @Test fun list_and_checklist_items_are_counted() {
        val blocks = listOf(
            ListBlock("l", ListKind.BULLET, listOf(ListItem("1", "buy milk"), ListItem("2", ""))),
            ChecklistBlock("c", items = listOf(ChecklistItem("1", "call mom now", false))),
        )
        assertEquals(5, WordCount.of("", blocks))
    }

    @Test fun divider_adds_nothing() = assertEquals(0, WordCount.of("", listOf(DividerBlock("d"))))
}
