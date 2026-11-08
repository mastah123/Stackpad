package com.stackpad.app.domain

import com.stackpad.app.domain.security.LockPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockPolicyTest {
    private fun lock(enabled: Boolean = true, locked: Boolean = false, at: Long? = null, now: Long = 100_000) =
        LockPolicy.shouldLock(enabled, locked, at, now)

    @Test fun disabledNeverLocks() {
        assertFalse(lock(enabled = false, locked = true, at = 0))
    }
    @Test fun coldStartLocksViaAlreadyLocked() = assertTrue(lock(locked = true))
    @Test fun stayingForegroundDoesNotLock() = assertFalse(lock(at = null))
    @Test fun shortAbsenceDoesNotLock() = assertFalse(lock(at = 100_000 - 29_999))
    @Test fun exactGraceLocks() = assertTrue(lock(at = 100_000 - LockPolicy.GRACE_MS))
    @Test fun longAbsenceLocks() = assertTrue(lock(at = 0))
    @Test fun clockGoingBackwardsLocks() = assertTrue(lock(at = 200_000, now = 100_000))
}
