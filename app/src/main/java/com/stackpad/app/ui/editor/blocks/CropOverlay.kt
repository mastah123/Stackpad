package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.stackpad.app.domain.image.CropHandle
import com.stackpad.app.domain.image.CropMath
import com.stackpad.app.domain.image.CropRect
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

/** Where a picture of [width] x [height] is drawn (centred, fitted) inside a [container]. */
private class Shown(val left: Float, val top: Float, val width: Float, val height: Float) {
    companion object {
        fun of(container: IntSize, width: Int, height: Int): Shown {
            val scale = min(container.width.toFloat() / width, container.height.toFloat() / height)
            val w = width * scale
            val h = height * scale
            return Shown((container.width - w) / 2f, (container.height - h) / 2f, w, h)
        }
    }
}

/**
 * The crop frame over a picture: dim outside, thirds grid, corner and edge handles. Drag a corner
 * (or an edge when the shape is free) to resize, drag inside to move. Fixed shapes only resize by the corners.
 *
 * @param aspect the frame's fixed width / height in fractions of the picture, or null when free
 */
@Composable
fun CropOverlay(
    pictureWidth: Int,
    pictureHeight: Int,
    crop: CropRect,
    aspect: Float?,
    onCropChange: (CropRect) -> Unit,
    onCropFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var container by remember { mutableStateOf(IntSize.Zero) }
    val currentCrop by rememberUpdatedState(crop)
    val currentAspect by rememberUpdatedState(aspect)
    val change by rememberUpdatedState(onCropChange)
    val finished by rememberUpdatedState(onCropFinished)

    Box(
        modifier = modifier
            .onSizeChanged { container = it }
            .pointerInput(pictureWidth, pictureHeight, container) {
                if (container.width == 0 || container.height == 0) return@pointerInput
                val shown = Shown.of(container, pictureWidth, pictureHeight)
                val slop = 28.dp.toPx()
                var handle: CropHandle? = null
                detectDragGestures(
                    onDragStart = { start -> handle = hit(start, currentCrop, shown, slop, currentAspect == null) },
                    onDrag = { pointer, drag ->
                        pointer.consume()
                        val grabbed = handle ?: return@detectDragGestures
                        change(CropMath.drag(currentCrop, grabbed, drag.x / shown.width, drag.y / shown.height, currentAspect))
                    },
                    onDragEnd = { if (handle != null) finished(); handle = null },
                    onDragCancel = { if (handle != null) finished(); handle = null },
                )
            },
    ) {
        if (container.width > 0 && container.height > 0) {
            val shown = Shown.of(container, pictureWidth, pictureHeight)
            Canvas(Modifier.fillMaxSize()) { drawFrame(shown, crop, aspect == null) }
        }
    }
}

private fun hit(point: Offset, crop: CropRect, shown: Shown, slop: Float, edgesToo: Boolean): CropHandle? {
    val left = shown.left + crop.left * shown.width
    val right = shown.left + crop.right * shown.width
    val top = shown.top + crop.top * shown.height
    val bottom = shown.top + crop.bottom * shown.height

    val corners = listOf(
        CropHandle.TOP_LEFT to Offset(left, top),
        CropHandle.TOP_RIGHT to Offset(right, top),
        CropHandle.BOTTOM_LEFT to Offset(left, bottom),
        CropHandle.BOTTOM_RIGHT to Offset(right, bottom),
    )
    corners.minByOrNull { (_, corner) -> hypot(point.x - corner.x, point.y - corner.y) }
        ?.takeIf { (_, corner) -> hypot(point.x - corner.x, point.y - corner.y) <= slop }
        ?.let { return it.first }

    if (edgesToo) {
        val insideX = point.x in (left - slop)..(right + slop)
        val insideY = point.y in (top - slop)..(bottom + slop)
        when {
            insideX && abs(point.y - top) <= slop -> return CropHandle.TOP
            insideX && abs(point.y - bottom) <= slop -> return CropHandle.BOTTOM
            insideY && abs(point.x - left) <= slop -> return CropHandle.LEFT
            insideY && abs(point.x - right) <= slop -> return CropHandle.RIGHT
        }
    }
    return if (point.x in left..right && point.y in top..bottom) CropHandle.MOVE else null
}

private fun DrawScope.drawFrame(shown: Shown, crop: CropRect, free: Boolean) {
    val left = shown.left + crop.left * shown.width
    val right = shown.left + crop.right * shown.width
    val top = shown.top + crop.top * shown.height
    val bottom = shown.top + crop.bottom * shown.height

    // Dim everything outside the frame, inside the picture.
    val dim = Color.Black.copy(alpha = 0.55f)
    val pictureRight = shown.left + shown.width
    val pictureBottom = shown.top + shown.height
    drawRect(dim, Offset(shown.left, shown.top), Size(shown.width, top - shown.top))
    drawRect(dim, Offset(shown.left, bottom), Size(shown.width, pictureBottom - bottom))
    drawRect(dim, Offset(shown.left, top), Size(left - shown.left, bottom - top))
    drawRect(dim, Offset(right, top), Size(pictureRight - right, bottom - top))

    // Thirds grid and border.
    val thin = 1.dp.toPx()
    val grid = Color.White.copy(alpha = 0.45f)
    for (i in 1..2) {
        val x = left + (right - left) * i / 3f
        val y = top + (bottom - top) * i / 3f
        drawLine(grid, Offset(x, top), Offset(x, bottom), thin)
        drawLine(grid, Offset(left, y), Offset(right, y), thin)
    }
    drawRect(Color.White, Offset(left, top), Size(right - left, bottom - top), style = androidx.compose.ui.graphics.drawscope.Stroke(thin * 1.5f))

    // Corner brackets, and edge ticks when the shape is free.
    val thick = 4.dp.toPx()
    val arm = 20.dp.toPx()
    val white = Color.White
    fun bracket(x: Float, y: Float, dx: Float, dy: Float) {
        drawLine(white, Offset(x - thick / 2 * dx, y), Offset(x + arm * dx, y), thick)
        drawLine(white, Offset(x, y - thick / 2 * dy), Offset(x, y + arm * dy), thick)
    }
    bracket(left, top, 1f, 1f)
    bracket(right, top, -1f, 1f)
    bracket(left, bottom, 1f, -1f)
    bracket(right, bottom, -1f, -1f)
    if (free) {
        val half = 12.dp.toPx()
        val midX = (left + right) / 2f
        val midY = (top + bottom) / 2f
        drawLine(white, Offset(midX - half, top), Offset(midX + half, top), thick)
        drawLine(white, Offset(midX - half, bottom), Offset(midX + half, bottom), thick)
        drawLine(white, Offset(left, midY - half), Offset(left, midY + half), thick)
        drawLine(white, Offset(right, midY - half), Offset(right, midY + half), thick)
    }
}
