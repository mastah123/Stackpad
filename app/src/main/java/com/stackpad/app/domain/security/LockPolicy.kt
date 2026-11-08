package com.stackpad.app.domain.security

/** When the app asks for the screen lock again. Pure, so it is unit tested. */
object LockPolicy {
    /** How long the app may stay in the background before it locks again. */
    const val GRACE_MS = 30_000L

    /**
     * True when the app must be locked now. [backgroundedAt] is when the app last left the
     * foreground, or null if it has not since the lock was last satisfied.
     */
    fun shouldLock(
        enabled: Boolean,
        alreadyLocked: Boolean,
        backgroundedAt: Long?,
        now: Long,
        graceMs: Long = GRACE_MS,
    ): Boolean = when {
        !enabled -> false
        alreadyLocked -> true
        backgroundedAt == null -> false
        // A clock that went backwards must not keep the app open.
        now < backgroundedAt -> true
        else -> now - backgroundedAt >= graceMs
    }
}
