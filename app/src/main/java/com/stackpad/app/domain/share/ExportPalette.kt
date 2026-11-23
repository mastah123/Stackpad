package com.stackpad.app.domain.share

/** Colors of an exported page, copied from the app's light and dark themes. All values are opaque 0xAARRGGBB. */
data class ExportPalette(
    val page: Int,
    val ink: Int,
    val muted: Int,
    val line: Int,
    /** The color card tints are washed over. */
    val card: Int,
    /** How strongly a block's tone is washed over [card]. */
    val toneStrength: Float,
    val primary: Int,
    val track: Int,
) {
    companion object {
        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val DARK_PAGE = 0xFF121218.toInt()
        private const val NOTE_TINT_STRENGTH = 0.18f

        /** The palette a share uses: its theme (APP follows [appIsDark]) and, when the note tint is on, the note's color. */
        fun forShare(options: ShareOptions, appIsDark: Boolean, noteAccent: Int?): ExportPalette {
            val dark = when (options.theme) {
                ShareTheme.LIGHT -> false
                ShareTheme.DARK -> true
                ShareTheme.APP -> appIsDark
            }
            return of(dark, if (options.noteTint) noteAccent else null)
        }

        /** [noteAccent] is the note color (0xAARRGGBB) to wash lightly over the page, or null for a plain page. */
        fun of(dark: Boolean, noteAccent: Int?): ExportPalette {
            val base = if (dark) DARK_PAGE else WHITE
            val page = if (noteAccent == null) base else over(noteAccent, NOTE_TINT_STRENGTH, base)
            return if (dark) {
                ExportPalette(
                    page = page, ink = 0xFFE4E1E9.toInt(), muted = 0xFFC9C8D3.toInt(), line = 0xFF474853.toInt(),
                    card = 0xFF23242E.toInt(), toneStrength = 0.22f, primary = 0xFF9EA3FF.toInt(), track = 0xFF474853.toInt(),
                )
            } else {
                ExportPalette(
                    page = page, ink = 0xFF1B1B21.toInt(), muted = 0xFF46464F.toInt(), line = 0xFFC9C8D3.toInt(),
                    card = 0xFFEEEFF8.toInt(), toneStrength = 0.13f, primary = 0xFF4F46E5.toInt(), track = 0xFFC9C8D3.toInt(),
                )
            }
        }

        /** [color] at [alpha] drawn over the opaque [background]. */
        fun over(color: Int, alpha: Float, background: Int): Int {
            fun channel(shift: Int): Int {
                val fg = (color shr shift) and 0xFF
                val bg = (background shr shift) and 0xFF
                return (fg * alpha + bg * (1 - alpha)).toInt().coerceIn(0, 255)
            }
            return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
        }
    }
}
