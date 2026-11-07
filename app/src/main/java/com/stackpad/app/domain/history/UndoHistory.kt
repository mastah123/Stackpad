package com.stackpad.app.domain.history

/**
 * Undo/redo stacks of immutable snapshots.
 *
 * [record] is called with the state *before* a change. Rapid changes that share a
 * [coalesceKey] (typing in one field) collapse into a single undo step.
 */
class UndoHistory<T>(
    private val limit: Int = 100,
    private val coalesceWindowMs: Long = 1_000L,
) {
    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()
    private var lastKey: String? = null
    private var lastTime = 0L

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** All snapshots still reachable through undo or redo. */
    fun snapshots(): List<T> = undoStack.toList() + redoStack.toList()

    fun record(before: T, coalesceKey: String? = null, nowMs: Long) {
        val continuing = coalesceKey != null && coalesceKey == lastKey && nowMs - lastTime <= coalesceWindowMs
        lastKey = coalesceKey
        lastTime = nowMs
        if (continuing) return
        undoStack.addLast(before)
        if (undoStack.size > limit) undoStack.removeFirst()
        redoStack.clear()
    }

    /** Returns the state to restore, or null when there is nothing to undo. */
    fun undo(current: T): T? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(current)
        lastKey = null
        return previous
    }

    fun redo(current: T): T? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(current)
        lastKey = null
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        lastKey = null
    }
}
