package com.stackpad.app.domain

import com.stackpad.app.data.model.LineSpacing
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextFont
import com.stackpad.app.data.model.TextLook
import com.stackpad.app.data.model.TextSize
import com.stackpad.app.data.model.look
import com.stackpad.app.data.model.withLook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextFormatTest {
    @Test fun aNewTextBlockHasTheDefaultLook() {
        assertTrue(TextBlock(id = "a").look().isDefault)
    }

    @Test fun withLookChangesOnlyTheLookFields() {
        val block = TextBlock(id = "a", text = "hello")
        val styled = block.withLook(TextLook(TextFont.SERIF, TextSize.LARGE, 3, LineSpacing.LOOSE))
        assertEquals("hello", styled.text)
        assertEquals(TextLook(TextFont.SERIF, TextSize.LARGE, 3, LineSpacing.LOOSE), styled.look())
        assertFalse(styled.look().isDefault)
    }

    @Test fun anUnknownHighlightIsClamped() {
        assertEquals(TextLook.HIGHLIGHT_COUNT, TextBlock(id = "a").withLook(TextLook(highlight = 99)).highlight)
        assertEquals(0, TextBlock(id = "a").withLook(TextLook(highlight = -4)).highlight)
    }

    @Test fun sizesGrowAndSpacingsOpenUp() {
        val scales = TextSize.values().map { it.scale }
        assertEquals(scales.sorted(), scales)
        assertEquals(1f, TextSize.NORMAL.scale)
        val spacings = LineSpacing.values().mapNotNull { it.multiplier }
        assertEquals(spacings.sorted(), spacings)
        assertEquals(null, LineSpacing.NORMAL.multiplier)
    }
}
