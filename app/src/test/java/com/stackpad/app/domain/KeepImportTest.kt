package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.importing.ImportRules
import com.stackpad.app.domain.importing.KeepConverter
import com.stackpad.app.domain.importing.KeepNote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepImportTest {
    private fun convert(note: KeepNote) = KeepConverter.convert(note, "Keep/a.json", now = 5_000)

    @Test fun text_note_keeps_title_and_text() {
        val result = convert(KeepNote(title = " Groceries ", text = "milk\neggs\n"))
        assertEquals("Groceries", result.title)
        assertEquals("milk\neggs", (result.blocks.single() as TextBlock).text)
    }

    @Test fun list_note_becomes_a_checklist_without_empty_rows() {
        val result = convert(KeepNote(items = listOf(KeepNote.Item("a", true), KeepNote.Item("  ", false), KeepNote.Item("b", false))))
        val list = result.blocks.single() as ChecklistBlock
        assertEquals(listOf("a" to true, "b" to false), list.items.map { it.text to it.checked })
    }

    @Test fun text_comes_before_checklist_and_links_after() {
        val result = convert(
            KeepNote(
                text = "intro",
                items = listOf(KeepNote.Item("x", false)),
                links = listOf(KeepNote.Link("Site", "https://a.example"), KeepNote.Link("Dup", "https://a.example")),
            ),
        )
        assertEquals(listOf("TextBlock", "ChecklistBlock", "LinkBlock"), result.blocks.map { it::class.simpleName })
        assertEquals(1, result.blocks.filterIsInstance<LinkBlock>().size)
    }

    @Test fun labels_become_clean_tags() {
        val result = convert(KeepNote(labels = listOf("Work Stuff", "work stuff", "#Home", "   ")))
        assertEquals(listOf("work-stuff", "home"), result.tags)
    }

    @Test fun colors_map_and_unknown_is_none() {
        assertEquals(1, KeepConverter.colorIndex("RED"))
        assertEquals(6, KeepConverter.colorIndex("cerulean"))
        assertEquals(0, KeepConverter.colorIndex("BROWN"))
        assertEquals(0, KeepConverter.colorIndex("DEFAULT"))
    }

    @Test fun timestamps_are_converted_from_microseconds() {
        val result = convert(KeepNote(createdUsec = 1_700_000_000_000_000, editedUsec = 1_700_000_100_000_000))
        assertEquals(1_700_000_000_000, result.createdAt)
        assertEquals(1_700_000_100_000, result.updatedAt)
    }

    @Test fun missing_timestamps_fall_back() {
        val result = convert(KeepNote(editedUsec = 2_000_000))
        assertEquals(2_000L, result.createdAt)
        assertEquals(5_000L, convert(KeepNote()).createdAt)
    }

    @Test fun images_are_pictures_and_other_attachments_are_counted() {
        val result = convert(
            KeepNote(attachments = listOf(KeepNote.Attachment("p.jpeg", "image/jpeg"), KeepNote.Attachment("v.3gp", "audio/3gpp"))),
        )
        assertEquals(listOf("p.jpeg"), result.pictures)
        assertEquals(1, result.skippedAttachments)
    }

    @Test fun note_ids_are_stable_and_differ_by_source() {
        assertEquals(ImportRules.noteId("keep", "a.json"), ImportRules.noteId("keep", "a.json"))
        assertTrue(ImportRules.noteId("keep", "a.json") != ImportRules.noteId("keep", "b.json"))
        assertTrue(ImportRules.noteId("keep", "a.json") != ImportRules.noteId("md", "a.json"))
    }

    @Test fun unsafe_paths_are_rejected() {
        assertNull(ImportRules.safePath("../evil.json"))
        assertNull(ImportRules.safePath("a/../../evil"))
        assertNull(ImportRules.safePath(""))
        assertEquals("Takeout/Keep/a.json", ImportRules.safePath("/Takeout/Keep/a.json"))
        assertEquals("a/b.json", ImportRules.safePath("a\\b.json"))
    }

    @Test fun picture_and_json_detection() {
        assertTrue(ImportRules.isPicture("x/Photo.JPG"))
        assertTrue(!ImportRules.isPicture("x/a.json"))
        assertTrue(ImportRules.isJson("x/a.JSON"))
    }

    @Test fun picture_resolves_across_jpg_and_jpeg_and_prefers_the_note_folder() {
        val files = setOf("Takeout/Keep/abc.jpg", "Other/abc.jpg", "Takeout/Keep/x.png")
        assertEquals("Takeout/Keep/abc.jpg", ImportRules.resolvePicture("abc.jpeg", "Takeout/Keep", files))
        assertEquals("Takeout/Keep/x.png", ImportRules.resolvePicture("x.jpg", "Takeout/Keep", files))
        assertNull(ImportRules.resolvePicture("missing.jpg", "Takeout/Keep", files))
    }
}
