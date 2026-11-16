package com.stackpad.app.domain

import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.importing.EnexParser
import com.stackpad.app.domain.importing.EnmlConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvernoteImportTest {
    private val enml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE en-note SYSTEM "http://xml.evernote.com/pub/enml2.dtd">
<en-note><div>Hello <b>world</b>&nbsp;!</div><div><en-todo checked="true"/>done</div><div><en-todo/>open</div><ul><li>x</li><li>y</li></ul><en-media type="image/png" hash="abc123"/></en-note>"""

    @Test fun enml_converts_text_tasks_lists_and_media() {
        val blocks = EnmlConverter.convert(enml)
        val text = blocks[0] as TextBlock
        assertTrue(text.text.startsWith("Hello world"))
        assertTrue(text.spans.any { it.type == SpanType.BOLD })
        val tasks = blocks[1] as ChecklistBlock
        assertEquals(listOf(true, false), tasks.items.map { it.checked })
        assertEquals(2, (blocks[2] as ListBlock).items.size)
        assertTrue((blocks[3] as ImageBlock).fileName.endsWith("abc123"))
    }

    @Test fun enex_reads_notes_tags_dates_and_resources() {
        val data = "AQID" // bytes 1,2,3
        val enex = """<?xml version="1.0"?><en-export><note><title>T1</title><created>20240102T030405Z</created><updated>20240103T030405Z</updated><tag>work</tag>
<content><![CDATA[<en-note><div>hi</div></en-note>]]></content>
<resource><data encoding="base64">$data</data><mime>image/png</mime><resource-attributes><file-name>a.png</file-name></resource-attributes></resource></note>
<note><title>T2</title><content><![CDATA[<en-note>b</en-note>]]></content></note></en-export>"""
        val notes = ArrayList<EnexParser.Note>()
        EnexParser.parse(enex.byteInputStream()) { notes += it }
        assertEquals(listOf("T1", "T2"), notes.map { it.title })
        assertEquals(listOf("work"), notes[0].tags)
        assertEquals(1, notes[0].resources.size)
        assertEquals(listOf<Byte>(1, 2, 3), notes[0].resources[0].data.toList())
        assertEquals("a.png", notes[0].resources[0].fileName)
        assertTrue(notes[0].createdAt > 0 && notes[0].updatedAt > notes[0].createdAt)
    }
}
