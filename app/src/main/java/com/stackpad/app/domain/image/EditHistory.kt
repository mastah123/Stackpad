package com.stackpad.app.domain.image

/** The editor's current edit and the ones before it, so each step can be undone. Immutable. */
data class EditHistory(
    val current: ImageEdit = ImageEdit(),
    private val past: List<ImageEdit> = emptyList(),
) {
    val canUndo: Boolean get() = past.isNotEmpty()

    /** True when the picture differs from how it was opened. */
    val isChanged: Boolean get() = !current.isUnchanged

    /** Makes [edit] the current one. Doing nothing new adds no step. */
    fun commit(edit: ImageEdit): EditHistory =
        if (edit == current) this else EditHistory(edit, (past + current).takeLast(MAX_STEPS))

    fun undo(): EditHistory = if (past.isEmpty()) this else EditHistory(past.last(), past.dropLast(1))

    /** Back to the opened picture. This is a step too, so it can be undone. */
    fun reset(): EditHistory = commit(ImageEdit())

    private companion object {
        const val MAX_STEPS = 50
    }
}
