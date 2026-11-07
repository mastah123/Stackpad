package com.stackpad.app.domain.notes

/** Rules for folder names. */
object FolderNames {
    const val MAX_LENGTH = 40
    private val whitespace = Regex("\\s+")

    /** Trims and collapses spaces; returns null when no usable name is left. */
    fun normalize(raw: String): String? =
        raw.trim().replace(whitespace, " ").take(MAX_LENGTH).trim().ifEmpty { null }
}
