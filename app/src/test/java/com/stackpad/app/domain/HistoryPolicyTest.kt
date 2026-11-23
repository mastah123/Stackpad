package com.stackpad.app.domain

import com.stackpad.app.domain.history.HistoryPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryPolicyTest {
    private val interval = HistoryPolicy.INTERVAL_MS

    @Test fun firstChangeToAnExistingNoteIsKept() {
        assertTrue(HistoryPolicy.shouldCapture(null, now = 1_000, contentChanged = true, previousIsBlank = false))
    }

    @Test fun editsWithinTheIntervalDoNotAddVersions() {
        assertFalse(HistoryPolicy.shouldCapture(0, now = interval - 1, contentChanged = true, previousIsBlank = false))
    }

    @Test fun anEditAfterTheIntervalAddsAVersion() {
        assertTrue(HistoryPolicy.shouldCapture(0, now = interval, contentChanged = true, previousIsBlank = false))
    }

    @Test fun unchangedOrEmptyNotesAreNeverKept() {
        assertFalse(HistoryPolicy.shouldCapture(null, now = interval, contentChanged = false, previousIsBlank = false))
        assertFalse(HistoryPolicy.shouldCapture(null, now = interval, contentChanged = true, previousIsBlank = true))
    }

    @Test fun onlyTheNewestVersionsAreKept() {
        val ids = (30L downTo 1L).toList()
        assertEquals((10L downTo 1L).toList(), HistoryPolicy.idsToTrim(ids))
        assertEquals(emptyList<Long>(), HistoryPolicy.idsToTrim(listOf(3, 2, 1)))
        assertEquals(listOf(1L), HistoryPolicy.idsToTrim(listOf(3, 2, 1), max = 2))
    }
}
