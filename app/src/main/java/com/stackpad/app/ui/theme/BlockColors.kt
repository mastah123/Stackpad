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

/** How a card-like block is painted: its background, outline, label/icon color and text color. */
class BlockLook(val container: Color, val border: Color?, val label: Color, val content: Color)

/**
 * Quote (cream), prompt (lavender) and code (dark navy) have their own looks in both themes;
 * the other blocks keep a wash of their tone over the card color.
 */
@Composable
fun BlockAccent.look(): BlockLook {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    return when (this) {
        BlockAccent.QUOTE -> if (dark) {
            BlockLook(Color(0xFF3D3417), Color(0xFF6B5A24), Color(0xFFF2CF66), Color(0xFFF3EBD0))
        } else {
            BlockLook(Color(0xFFFFF1C9), Color(0xFFF0D98C), Color(0xFF9A6B00), Color(0xFF3B3320))
        }
        BlockAccent.PROMPT -> if (dark) {
            BlockLook(Color(0xFF33295A), Color(0xFF54458F), Color(0xFFC4B3FF), Color(0xFFECE6FF))
        } else {
            BlockLook(Color(0xFFEADFFA), Color(0xFFD2C0F0), Color(0xFF6C4FBF), Color(0xFF2A2250))
        }
        BlockAccent.CODE -> if (dark) {
            BlockLook(Color(0xFF0B0F20), Color(0xFF2E3558), Color(0xFF7ED6A5), Color(0xFFEDEFF8))
        } else {
            BlockLook(Color(0xFF1F2336), null, Color(0xFF7ED6A5), Color(0xFFEDEFF8))
        }
        else -> BlockLook(container(), null, tint(), colors.onSurface)
    }
}
