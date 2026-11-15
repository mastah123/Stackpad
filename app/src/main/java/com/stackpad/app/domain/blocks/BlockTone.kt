package com.stackpad.app.domain.blocks

/**
 * The color that tells what a card-like block is for. Shared by the editor and by the image and
 * PDF export so both look alike. [argb] is an opaque 0xAARRGGBB color.
 */
enum class BlockTone(val argb: Int) {
    /** Tasks to tick off: green. */
    CHECKLIST(0xFF43A047.toInt()),

    /** Numbers and formulas: teal. */
    TABLE(0xFF00897B.toInt()),

    /** Something to open elsewhere: blue. */
    LINK(0xFF1E88E5.toInt()),

    /** Text to hand to an AI: purple. */
    PROMPT(0xFF8E24AA.toInt()),

    /** Words from someone else: amber. */
    QUOTE(0xFFF9A825.toInt()),

    /** Code to copy: slate. */
    CODE(0xFF546E7A.toInt()),

    /** A voice recording: rose. */
    AUDIO(0xFFD81B60.toInt()),
}
