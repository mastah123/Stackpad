package com.stackpad.app.domain.image

import kotlin.math.max
import kotlin.math.min

/** The parts of the crop frame a finger can grab. */
enum class CropHandle { TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, MOVE }

/** Shapes offered for the crop frame. [ratio] is width / height, or null when it is not fixed here. */
enum class AspectChoice(val ratio: Float?) {
    FREE(null),
    ORIGINAL(null),
    SQUARE(1f),
    LANDSCAPE_4_3(4f / 3f),
    PORTRAIT_3_4(3f / 4f),
    LANDSCAPE_16_9(16f / 9f),
    PORTRAIT_9_16(9f / 16f),
    ;

    /** Width / height in pixels for a picture of [base] size, or null for a free frame. */
    fun pixelRatio(base: PixelSize): Float? = if (this == ORIGINAL) base.aspect else ratio

    val isFixed: Boolean get() = this != FREE
}

/** Moving and resizing the crop frame. All values are fractions of the picture. Pure, so unit tested. */
object CropMath {
    const val MIN_SIZE = 0.08f

    /** Pixel ratio (width / height) turned into the same ratio measured in fractions of a [base] size picture. */
    fun fractionAspect(pixelRatio: Float, base: PixelSize): Float = pixelRatio * base.height / base.width

    /** Moves the frame, keeping its size and staying inside the picture. */
    fun move(rect: CropRect, dx: Float, dy: Float): CropRect {
        val x = dx.coerceIn(-rect.left, 1f - rect.right)
        val y = dy.coerceIn(-rect.top, 1f - rect.bottom)
        return CropRect(rect.left + x, rect.top + y, rect.right + x, rect.bottom + y)
    }

    /**
     * Drags [handle] by ([dx], [dy]). With a fixed [aspect] (fraction width / fraction height) only the
     * corners resize, keeping the opposite corner in place; edges are ignored.
     */
    fun drag(rect: CropRect, handle: CropHandle, dx: Float, dy: Float, aspect: Float?): CropRect {
        if (handle == CropHandle.MOVE) return move(rect, dx, dy)
        return if (aspect == null) dragFree(rect, handle, dx, dy) else dragFixed(rect, handle, dx, dy, aspect)
    }

    private fun dragFree(rect: CropRect, handle: CropHandle, dx: Float, dy: Float): CropRect {
        var left = rect.left
        var top = rect.top
        var right = rect.right
        var bottom = rect.bottom
        when (handle) {
            CropHandle.TOP_LEFT, CropHandle.LEFT, CropHandle.BOTTOM_LEFT -> left = (left + dx).coerceIn(0f, right - MIN_SIZE)
            CropHandle.TOP_RIGHT, CropHandle.RIGHT, CropHandle.BOTTOM_RIGHT -> right = (right + dx).coerceIn(left + MIN_SIZE, 1f)
            else -> Unit
        }
        when (handle) {
            CropHandle.TOP_LEFT, CropHandle.TOP, CropHandle.TOP_RIGHT -> top = (top + dy).coerceIn(0f, bottom - MIN_SIZE)
            CropHandle.BOTTOM_LEFT, CropHandle.BOTTOM, CropHandle.BOTTOM_RIGHT -> bottom = (bottom + dy).coerceIn(top + MIN_SIZE, 1f)
            else -> Unit
        }
        return CropRect(left, top, right, bottom)
    }

    private fun dragFixed(rect: CropRect, handle: CropHandle, dx: Float, dy: Float, aspect: Float): CropRect {
        val movesRight = handle == CropHandle.TOP_RIGHT || handle == CropHandle.BOTTOM_RIGHT
        val movesLeft = handle == CropHandle.TOP_LEFT || handle == CropHandle.BOTTOM_LEFT
        val movesBottom = handle == CropHandle.BOTTOM_LEFT || handle == CropHandle.BOTTOM_RIGHT
        val movesTop = handle == CropHandle.TOP_LEFT || handle == CropHandle.TOP_RIGHT
        if (!(movesRight || movesLeft) || !(movesBottom || movesTop)) return rect

        val anchorX = if (movesRight) rect.left else rect.right
        val anchorY = if (movesBottom) rect.top else rect.bottom
        val signX = if (movesRight) 1f else -1f
        val signY = if (movesBottom) 1f else -1f
        val cornerX = (if (movesRight) rect.right else rect.left) + dx
        val cornerY = (if (movesBottom) rect.bottom else rect.top) + dy

        val wantedWidth = (cornerX - anchorX) * signX
        val wantedHeight = (cornerY - anchorY) * signY
        val spaceX = if (movesRight) 1f - anchorX else anchorX
        val spaceY = if (movesBottom) 1f - anchorY else anchorY
        val maxWidth = min(spaceX, spaceY * aspect)
        val minWidth = min(max(MIN_SIZE, MIN_SIZE * aspect), maxWidth)
        val width = max(wantedWidth, wantedHeight * aspect).coerceIn(minWidth, maxWidth)
        val height = width / aspect

        val x2 = anchorX + signX * width
        val y2 = anchorY + signY * height
        return CropRect(min(anchorX, x2), min(anchorY, y2), max(anchorX, x2), max(anchorY, y2))
    }

    /** The largest area of shape [aspect] (fraction width / fraction height) inside [rect], with the same centre. */
    fun fit(rect: CropRect, aspect: Float): CropRect {
        val height: Float
        val width: Float
        if (rect.width / rect.height > aspect) {
            height = rect.height
            width = height * aspect
        } else {
            width = rect.width
            height = width / aspect
        }
        val centerX = (rect.left + rect.right) / 2f
        val centerY = (rect.top + rect.bottom) / 2f
        return CropRect(centerX - width / 2f, centerY - height / 2f, centerX + width / 2f, centerY + height / 2f)
    }
}
