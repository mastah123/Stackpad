package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/** Editor blocks that are drawn as cards, each with a color that hints at what it is for. */
enum class BlockAccent(val color: Color) {
    /** Tasks to tick off: green. */
    CHECKLIST(Color(0xFF43A047)),

    /** Numbers and formulas: teal. */
    TABLE(Color(0xFF00897B)),

    /** Something to open elsewhere: blue. */
    LINK(Color(0xFF1E88E5)),

    /** Text to hand to an AI: purple. */
    PROMPT(Color(0xFF8E24AA)),

    /** Words from someone else: amber. */
    QUOTE(Color(0xFFF9A825)),

    /** Code to copy: slate. */
    CODE(Color(0xFF546E7A)),

    /** A voice recording: rose. */
    AUDIO(Color(0xFFD81B60)),
}

/** The accent itself, for icons, bars and borders. */
fun BlockAccent.tint(): Color = color

/** Card background: the accent washed lightly over the normal surface, stronger in dark theme. */
@Composable
fun BlockAccent.container(): Color {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    return color.copy(alpha = if (dark) 0.22f else 0.13f).compositeOver(colors.surfaceVariant)
}
