package com.stackpad.app.domain.image

import kotlin.math.roundToInt

/** How big the saved picture may be. A smaller size (and a little more compression) makes the note lighter. */
enum class ImageSize(val maxEdge: Int?, val jpegQuality: Int) {
    ORIGINAL(null, 92),
    LARGE(2048, 90),
    MEDIUM(1280, 85),
    SMALL(800, 80),
    ;

    /** The width and height after limiting the longer side to [maxEdge]; pictures are never enlarged. */
    fun fit(width: Int, height: Int): Pair<Int, Int> {
        val limit = maxEdge ?: return width to height
        val longest = maxOf(width, height)
        if (longest <= limit) return width to height
        val scale = limit.toFloat() / longest
        return maxOf(1, (width * scale).roundToInt()) to maxOf(1, (height * scale).roundToInt())
    }
}
