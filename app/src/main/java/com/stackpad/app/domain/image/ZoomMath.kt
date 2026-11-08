package com.stackpad.app.domain.image

import kotlin.math.abs
import kotlin.math.max

/** Scale and pan limits for the full-screen image viewer. Pure logic, so it is unit tested. */
object ZoomMath {
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 5f
    const val DOUBLE_TAP_SCALE = 2.5f

    fun clampScale(scale: Float): Float = scale.coerceIn(MIN_SCALE, MAX_SCALE)

    /** Largest pan distance in one axis that keeps the zoomed image covering the container. */
    fun maxPan(containerSize: Float, scale: Float): Float = max(0f, containerSize * (scale - 1f) / 2f)

    fun clampPan(pan: Float, containerSize: Float, scale: Float): Float {
        val limit = maxPan(containerSize, scale)
        return pan.coerceIn(-limit, limit)
    }

    /** Double tap zooms in from normal size, and back out from any zoomed size. */
    fun doubleTapScale(current: Float): Float =
        if (abs(current - MIN_SCALE) < 0.01f) DOUBLE_TAP_SCALE else MIN_SCALE
}
