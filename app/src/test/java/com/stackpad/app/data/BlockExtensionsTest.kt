package com.stackpad.app.data

import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.isBlank
import com.stackpad.app.data.model.mediaFileName
import com.stackpad.app.data.model.searchableText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockExtensionsTest {
    @Test
    fun quoteAndCodeAreSearchable() {
        assertEquals("wise words", QuoteBlock("q", "wise words").searchableText())
        assertEquals("val x = 1", CodeBlock("c", "val x = 1").searchableText())
        assertEquals("", DividerBlock("d").searchableText())
    }

    @Test
    fun blankDetection() {
        assertTrue(QuoteBlock("q", "  ").isBlank())
        assertTrue(CodeBlock("c").isBlank())
        assertFalse(QuoteBlock("q", "x").isBlank())
        // A divider is content, so a note holding only a divider is still saved.
        assertFalse(DividerBlock("d").isBlank())
    }

    @Test
    fun newBlocksOwnNoMedia() {
        assertNull(QuoteBlock("q", "x").mediaFileName())
        assertNull(CodeBlock("c", "x").mediaFileName())
        assertNull(DividerBlock("d").mediaFileName())
    }

    @Test
    fun imageCaptionIsSearchableButImageIsNeverBlank() {
        assertEquals("sunset", ImageBlock("i", "a.jpg", "sunset").searchableText())
        assertEquals("", ImageBlock("i", "a.jpg").searchableText())
        assertFalse(ImageBlock("i", "a.jpg").isBlank())
        assertEquals("a.jpg", ImageBlock("i", "a.jpg", "x").mediaFileName())
    }
}
