package com.stackpad.app.domain

import com.stackpad.app.domain.notes.TrashPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class TrashPolicyTest {
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun cutoffIsThirtyDaysAgo() {
        assertEquals(100 * day - 30 * day, TrashPolicy.cutoff(100 * day))
    }

    @Test
    fun daysLeftRoundsUp() {
        val trashed = 10 * day
        assertEquals(30, TrashPolicy.daysLeft(trashed, trashed))
        assertEquals(30, TrashPolicy.daysLeft(trashed, trashed + 1))
        assertEquals(1, TrashPolicy.daysLeft(trashed, trashed + 29 * day + 1))
    }

    @Test
    fun daysLeftNeverNegative() {
        assertEquals(0, TrashPolicy.daysLeft(0, 40 * day))
        assertEquals(0, TrashPolicy.daysLeft(0, 30 * day))
    }
}
