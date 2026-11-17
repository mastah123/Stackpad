package com.stackpad.app.domain.image

import kotlin.math.abs

/** A rectangle inside a picture, as fractions (0 to 1) of its width and height. */
data class CropRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    /** True when nothing is cut off. */
    val isFull: Boolean
        get() = abs(left) < EPSILON && abs(top) < EPSILON && abs(right - 1f) < EPSILON && abs(bottom - 1f) < EPSILON

    /** The same area after the picture is turned a quarter clockwise. */
    fun rotatedRight() = CropRect(1f - bottom, left, 1f - top, right)

    /** The same area after the picture is turned a quarter counter-clockwise. */
    fun rotatedLeft() = CropRect(top, 1f - right, bottom, 1f - left)

    /** The same area after the picture is mirrored left to right. */
    fun mirrored() = CropRect(1f - right, top, 1f - left, bottom)

    companion object {
        val FULL = CropRect(0f, 0f, 1f, 1f)
        private const val EPSILON = 0.0005f
    }
}
