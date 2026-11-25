package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import com.stackpad.app.data.model.TextFont

/** Highlight colors for text blocks: yellow, green, blue, pink, purple, orange (index 1..6). */
object TextHighlights {
    private val swatches = listOf(
        Color(0xFFFFE066),
        Color(0xFF9AE6A0),
        Color(0xFF9CC7FF),
        Color(0xFFFFB3C7),
        Color(0xFFD1B8FF),
        Color(0xFFFFC08A),
    )

    /** The solid swatch for [index], or null for none or an unknown index. */
    fun swatch(index: Int): Color? = swatches.getOrNull(index - 1)

    /** The color drawn behind the text: translucent so the text stays readable in both themes. */
    @Composable
    fun background(index: Int): Color? {
        val swatch = swatch(index) ?: return null
        val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        return swatch.copy(alpha = if (dark) 0.35f else 0.6f)
    }
}

/** The font family for a text block's font choice, or null to keep the app font. */
fun TextFont.family(): FontFamily? = when (this) {
    TextFont.DEFAULT -> null
    TextFont.SERIF -> FontFamily.Serif
    TextFont.MONO -> FontFamily.Monospace
    TextFont.HANDWRITING -> FontFamily.Cursive
}
