package com.stackpad.app.data.model

/** Typeface choices for text. DEFAULT is the app font; the others are the phone's own families. */
enum class TextFont { DEFAULT, SERIF, MONO, HANDWRITING }

/** Text size of a plain text block, as a multiple of the normal body size. */
enum class TextSize(val scale: Float) { SMALL(0.87f), NORMAL(1f), LARGE(1.2f), HUGE(1.45f) }

/** Space between lines. [multiplier] is relative to the font size; null leaves the natural spacing. */
enum class LineSpacing(val multiplier: Float?) { TIGHT(1.15f), NORMAL(null), RELAXED(1.65f), LOOSE(1.95f) }
