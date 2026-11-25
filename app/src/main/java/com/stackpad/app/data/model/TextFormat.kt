package com.stackpad.app.data.model

/** Typeface choices for a plain text block. DEFAULT is the app font; the others are the phone's own families. */
enum class TextFont { DEFAULT, SERIF, MONO, HANDWRITING }

/** Text size of a plain text block, as a multiple of the normal body size. */
enum class TextSize(val scale: Float) { SMALL(0.87f), NORMAL(1f), LARGE(1.2f), HUGE(1.45f) }

/** Space between lines of a plain text block. [multiplier] is relative to the font size; null leaves the natural spacing. */
enum class LineSpacing(val multiplier: Float?) { TIGHT(1.15f), NORMAL(null), RELAXED(1.65f), LOOSE(1.95f) }

/** The look of one plain text block: font, size, highlight color (0 = none, 1..[HIGHLIGHT_COUNT]) and line spacing. */
data class TextLook(
    val font: TextFont = TextFont.DEFAULT,
    val size: TextSize = TextSize.NORMAL,
    val highlight: Int = 0,
    val spacing: LineSpacing = LineSpacing.NORMAL,
) {
    val isDefault: Boolean get() = this == TextLook()

    companion object {
        /** Number of highlight colors (not counting "none"). */
        const val HIGHLIGHT_COUNT = 6
    }
}

fun TextBlock.look(): TextLook = TextLook(font, size, highlight, spacing)

fun TextBlock.withLook(look: TextLook): TextBlock = copy(
    font = look.font,
    size = look.size,
    highlight = look.highlight.coerceIn(0, TextLook.HIGHLIGHT_COUNT),
    spacing = look.spacing,
)
