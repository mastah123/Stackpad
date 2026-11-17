package com.stackpad.app.domain.image

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class PixelSize(val width: Int, val height: Int) {
    val aspect: Float get() = width.toFloat() / height
}

data class PixelRect(val left: Int, val top: Int, val width: Int, val height: Int)

/** Sizes of the picture at each step of an [ImageEdit]. Pure maths, shared by the preview and the saved file. */
object EditGeometry {

    /**
     * How much of a [width] x [height] picture stays when it is turned by [degrees] and cut to the
     * largest centred area of the same shape that has no empty corners (1 = all of it).
     */
    fun straightenScale(width: Int, height: Int, degrees: Float): Float {
        val angle = abs(degrees) * PI / 180.0
        val longSide = max(width, height).toDouble()
        val shortSide = min(width, height).toDouble()
        return (1.0 / (cos(angle) + (longSide / shortSide) * sin(angle))).toFloat()
    }

    /** Size after turning and straightening, before the crop. */
    fun baseSize(sourceWidth: Int, sourceHeight: Int, edit: ImageEdit): PixelSize {
        val odd = edit.quarterTurns % 2 == 1
        val width = if (odd) sourceHeight else sourceWidth
        val height = if (odd) sourceWidth else sourceHeight
        val scale = straightenScale(width, height, edit.straighten)
        return PixelSize((width * scale).roundToInt().coerceAtLeast(1), (height * scale).roundToInt().coerceAtLeast(1))
    }

    /** The crop in pixels of the base picture, never smaller than one pixel and always inside it. */
    fun cropPixels(base: PixelSize, crop: CropRect): PixelRect {
        val left = (crop.left * base.width).roundToInt().coerceIn(0, base.width - 1)
        val top = (crop.top * base.height).roundToInt().coerceIn(0, base.height - 1)
        val right = (crop.right * base.width).roundToInt().coerceIn(left + 1, base.width)
        val bottom = (crop.bottom * base.height).roundToInt().coerceIn(top + 1, base.height)
        return PixelRect(left, top, right - left, bottom - top)
    }

    /** Size of the finished picture. */
    fun outputSize(sourceWidth: Int, sourceHeight: Int, edit: ImageEdit): PixelSize {
        val rect = cropPixels(baseSize(sourceWidth, sourceHeight, edit), edit.crop)
        return PixelSize(rect.width, rect.height)
    }
}
