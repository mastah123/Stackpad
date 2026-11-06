package com.stackpad.app.data

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockJsonCodecTest {
    private val blocks: List<Block> = listOf(
        TextBlock("1", TextLevel.H2, "Hello", listOf(TextSpan(0, 3, SpanType.BOLD))),
        ListBlock("2", ListKind.LETTERED, listOf(ListItem("a", "one"), ListItem("b", "two"))),
        ChecklistBlock("3", "Todo", listOf(ChecklistItem("c", "x", true), ChecklistItem("d", "y", false))),
        ImageBlock("4", "img_1.jpg"),
        SheetBlock("5", listOf(listOf("1", "=A1+1"), listOf("", "text"))),
        AudioBlock("6", "aud_1.m4a", 4200L, listOf(10, 50, 90)),
        LinkBlock("7", "Site", "https://example.com"),
        PromptBlock("8", "Summarize this"),
    )

    @Test
    fun everyBlockTypeSurvivesARoundTrip() {
        assertEquals(blocks, BlockJsonCodec.decode(BlockJsonCodec.encode(blocks)))
    }

    @Test
    fun invalidJsonDecodesToEmptyList() {
        assertTrue(BlockJsonCodec.decode("not json").isEmpty())
        assertTrue(BlockJsonCodec.decode("").isEmpty())
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val json = """[{"type":"text","id":"1","text":"hi","future":123}]"""
        assertEquals(listOf<Block>(TextBlock("1", text = "hi")), BlockJsonCodec.decode(json))
    }
}
