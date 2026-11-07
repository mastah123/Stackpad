package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/** The colors a note can be given. Index 0 means "no color"; names live in `note_color_names`. */
object NoteColors {
    private val accents = listOf(
        Color(0xFFE57373), // red
        Color(0xFFFFB74D), // orange
        Color(0xFFFFD54F), // yellow
        Color(0xFF81C784), // green
        Color(0xFF4DB6AC), // teal
        Color(0xFF64B5F6), // blue
        Color(0xFFBA68C8), // purple
        Color(0xFFF06292), // pink
    )

    /** Number of choices including "none". */
    val count: Int = accents.size + 1

    /** The accent for [index], or null for no color or an unknown index. */
    fun accent(index: Int): Color? = accents.getOrNull(index - 1)
}

/** Background for a colored note card: the accent washed over the normal card color. */
@Composable
fun noteCardColor(index: Int): Color {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val accent = NoteColors.accent(index) ?: return base
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return accent.copy(alpha = if (dark) 0.28f else 0.30f).compositeOver(base)
}
