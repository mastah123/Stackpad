package com.stackpad.app.domain.notes

/** Title of a duplicated note: the original title plus " (copy)", or the automatic title when it had none. */
object DuplicateTitle {
    private const val SUFFIX = " (copy)"

    fun of(title: String, createdAt: Long): String =
        if (title.isBlank()) AutoTitle.of(createdAt) else title.trimEnd() + SUFFIX
}
