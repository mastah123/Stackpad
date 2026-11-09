package com.stackpad.app.domain

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.templates.BlockIds
import com.stackpad.app.domain.templates.BuiltInTemplates
import com.stackpad.app.domain.templates.NoteTemplate
import com.stackpad.app.domain.templates.TemplateRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplatesTest {
    private fun ids(blocks: List<Block>): List<String> = blocks.flatMap { b ->
        listOf(b.id) + when (b) {
            is ListBlock -> b.items.map { it.id }
            is ChecklistBlock -> b.items.map { it.id }
            else -> emptyList()
        }
    }

    private val sample = listOf<Block>(
        TextBlock("t1", text = "hello"),
        ListBlock("l1", ListKind.BULLET, listOf(ListItem("i1", "a"), ListItem("i2", "b"))),
        ChecklistBlock("c1", "todo", listOf(ChecklistItem("k1", "x", checked = true, dueDate = "2026-11-10"))),
    )

    @Test fun freshIdsReplaceEveryId() {
        val copy = BlockIds.withFreshIds(sample)
        assertTrue(ids(copy).intersect(ids(sample).toSet()).isEmpty())
        assertEquals(ids(sample).size, ids(copy).toSet().size)
    }

    @Test fun freshIdsKeepContent() {
        val copy = BlockIds.withFreshIds(sample)
        assertEquals("hello", (copy[0] as TextBlock).text)
        assertEquals(listOf("a", "b"), (copy[1] as ListBlock).items.map { it.text })
        assertEquals(true, (copy[2] as ChecklistBlock).items[0].checked)
    }

    @Test fun nameNormalization() {
        assertEquals("My plan", TemplateRules.normalizeName("  My   plan "))
        assertNull(TemplateRules.normalizeName("   "))
        assertEquals(TemplateRules.MAX_NAME_LENGTH, TemplateRules.normalizeName("x".repeat(100))!!.length)
    }

    @Test fun storingDropsMediaBlanksAndResetsChecklists() {
        val blocks = listOf<Block>(
            ImageBlock("m1", "img_1.jpg"),
            AudioBlock("a1", "aud_1.m4a", 1000, emptyList()),
            TextBlock("blank"),
            TextBlock("t1", text = "keep"),
            ChecklistBlock("c1", "", listOf(ChecklistItem("k1", "x", checked = true, dueDate = "2026-11-10"))),
        )
        val stored = TemplateRules.storableBlocks(blocks)
        assertEquals(listOf("t1", "c1"), stored.map { it.id })
        val item = (stored[1] as ChecklistBlock).items.single()
        assertFalse(item.checked)
        assertNull(item.dueDate)
    }

    @Test fun applyReplacesAnEmptyNote() {
        val template = NoteTemplate("t", "T", blocks = sample)
        val result = TemplateRules.apply(listOf(TextBlock("blank")), template)
        assertEquals(sample.size, result.size)
        assertTrue(ids(result).intersect(ids(sample).toSet()).isEmpty())
    }

    @Test fun applyAppendsToANoteWithContent() {
        val template = NoteTemplate("t", "T", blocks = sample)
        val existing = listOf<Block>(TextBlock("e1", text = "mine"))
        val result = TemplateRules.apply(existing, template)
        assertEquals("e1", result.first().id)
        assertEquals(1 + sample.size, result.size)
    }

    @Test fun builtInsAreUsable() {
        val all = BuiltInTemplates.all()
        assertTrue(all.size >= 4)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        all.forEach { t ->
            assertTrue(t.blocks.isNotEmpty())
            assertTrue(t.blocks.none { it is ImageBlock || it is AudioBlock })
            assertEquals(ids(t.blocks).size, ids(t.blocks).toSet().size)
        }
    }
}
