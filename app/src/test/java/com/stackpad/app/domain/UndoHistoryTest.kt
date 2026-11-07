package com.stackpad.app.domain

import com.stackpad.app.domain.history.UndoHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoHistoryTest {
    @Test
    fun undoAndRedoWalkTheStack() {
        val h = UndoHistory<String>()
        h.record("a", nowMs = 0)
        h.record("b", nowMs = 10)
        assertEquals("b", h.undo("c"))
        assertEquals("a", h.undo("b"))
        assertFalse(h.canUndo)
        assertEquals("b", h.redo("a"))
        assertEquals("c", h.redo("b"))
        assertFalse(h.canRedo)
    }

    @Test
    fun newChangeClearsRedo() {
        val h = UndoHistory<String>()
        h.record("a", nowMs = 0)
        h.undo("b")
        assertTrue(h.canRedo)
        h.record("a", nowMs = 5)
        assertFalse(h.canRedo)
    }

    @Test
    fun sameKeyWithinWindowCoalesces() {
        val h = UndoHistory<String>(coalesceWindowMs = 1000)
        h.record("s0", "text", 0)
        h.record("s1", "text", 200)
        h.record("s2", "text", 400)
        assertEquals("s0", h.undo("s3"))
        assertNull(h.undo("s0"))
    }

    @Test
    fun differentKeyOrLateChangeStartsNewStep() {
        val h = UndoHistory<String>(coalesceWindowMs = 1000)
        h.record("s0", "text", 0)
        h.record("s1", "title", 100)
        h.record("s2", "title", 5000)
        assertEquals("s2", h.undo("x"))
        assertEquals("s1", h.undo("s2"))
        assertEquals("s0", h.undo("s1"))
    }

    @Test
    fun nullKeyNeverCoalesces() {
        val h = UndoHistory<String>()
        h.record("a", null, 0)
        h.record("b", null, 1)
        assertEquals("b", h.undo("c"))
        assertEquals("a", h.undo("b"))
    }

    @Test
    fun limitDropsOldest() {
        val h = UndoHistory<Int>(limit = 3)
        (1..5).forEach { h.record(it, nowMs = it.toLong()) }
        assertEquals(listOf(3, 4, 5), h.snapshots())
    }

    @Test
    fun emptyUndoRedoReturnNull() {
        val h = UndoHistory<String>()
        assertNull(h.undo("x"))
        assertNull(h.redo("x"))
    }
}
