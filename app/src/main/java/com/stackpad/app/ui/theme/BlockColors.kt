package com.stackpad.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.stackpad.app.domain.blocks.BlockTone

/** Editor blocks that are drawn as cards, each tinted by [BlockTone] so its color hints at what it is for. */
enum class BlockAccent(val tone: BlockTone) {
    CHECKLIST(BlockTone.CHECKLIST),
    TABLE(BlockTone.TABLE),
    LINK(BlockTone.LINK),
    PROMPT(BlockTone.PROMPT),
    QUOTE(BlockTone.QUOTE),
    CODE(BlockTone.CODE),
    AUDIO(BlockTone.AUDIO);

    val color: Color = Color(tone.argb)
}

/** The accent itself, for icons, bars and borders. */
fun BlockAccent.tint(): Color = color

/** Card background: the tone washed lightly over the normal surface, stronger in dark theme. */
@Composable
fun BlockTone.container(): Color {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    return Color(argb).copy(alpha = if (dark) 0.22f else 0.13f).compositeOver(colors.surfaceVariant)
}

@Composable
fun BlockAccent.container(): Color = tone.container()
