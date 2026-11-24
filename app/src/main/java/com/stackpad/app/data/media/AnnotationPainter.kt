package com.stackpad.app.data.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.stackpad.app.domain.image.Annotation
import kotlin.math.max
import kotlin.math.min

/** Draws marks onto a picture. The editor preview and the saved file use the same code, so they look alike. */
object AnnotationPainter {
    private const val HIGHLIGHT_ALPHA = 0x66

    /** Draws pen lines, highlighter lines and text on [canvas] for a picture of [width] x [height]. Hidden regions are not drawn here. */
    fun drawMarks(canvas: Canvas, width: Int, height: Int, marks: List<Annotation>) {
        val unit = min(width, height).toFloat()
        for (mark in marks) {
            when (mark) {
                is Annotation.Stroke -> drawStroke(canvas, mark, width, height, unit)
                is Annotation.Label -> drawLabel(canvas, mark, width, height, unit)
                is Annotation.Obscure -> Unit
            }
        }
    }

    private fun drawStroke(canvas: Canvas, stroke: Annotation.Stroke, width: Int, height: Int, unit: Float) {
        if (stroke.points.isEmpty()) return
        val thickness = max(1f, stroke.width * unit)
        val color = if (stroke.highlight) (stroke.color and 0x00FFFFFF) or (HIGHLIGHT_ALPHA shl 24) else stroke.color
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = thickness
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val first = stroke.points.first()
        if (stroke.points.size == 1) {
            paint.style = Paint.Style.FILL
            canvas.drawCircle(first.x * width, first.y * height, thickness / 2f, paint)
            return
        }
        paint.style = Paint.Style.STROKE
        val path = Path().apply {
            moveTo(first.x * width, first.y * height)
            stroke.points.drop(1).forEach { lineTo(it.x * width, it.y * height) }
        }
        canvas.drawPath(path, paint)
    }

    private fun drawLabel(canvas: Canvas, label: Annotation.Label, width: Int, height: Int, unit: Float) {
        if (label.text.isBlank()) return
        val size = max(8f, label.size * unit)
        val brightness = (Color.red(label.color) * 299 + Color.green(label.color) * 587 + Color.blue(label.color) * 114) / 1000
        val outline = if (brightness > 150) Color.BLACK else Color.WHITE
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = label.color; textSize = size }
        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = outline
            textSize = size
            style = Paint.Style.STROKE
            strokeWidth = size * 0.14f
            strokeJoin = Paint.Join.ROUND
        }
        val lineHeight = fill.fontSpacing
        label.text.lines().forEachIndexed { index, line ->
            val x = label.x * width
            val baseline = label.y * height + lineHeight * index - fill.ascent()
            canvas.drawText(line, x, baseline, edge)
            canvas.drawText(line, x, baseline, fill)
        }
    }

    /** Blurs or pixelates each region. Returns a picture that can be drawn on (a copy when [bitmap] cannot be changed). */
    fun obscure(bitmap: Bitmap, regions: List<Annotation.Obscure>): Bitmap {
        val target = ensureMutable(bitmap)
        val w = target.width
        val h = target.height
        for (region in regions) {
            val left = (region.rect.left * w).toInt().coerceIn(0, w - 1)
            val top = (region.rect.top * h).toInt().coerceIn(0, h - 1)
            val right = (region.rect.right * w).toInt().coerceIn(left + 1, w)
            val bottom = (region.rect.bottom * h).toInt().coerceIn(top + 1, h)
            val rw = right - left
            val rh = bottom - top
            val piece = Bitmap.createBitmap(target, left, top, rw, rh)
            val divisor = if (region.pixelate) max(8, min(w, h) / 40) else max(6, min(w, h) / 70)
            val small = Bitmap.createScaledBitmap(piece, max(1, rw / divisor), max(1, rh / divisor), !region.pixelate)
            val big = Bitmap.createScaledBitmap(small, rw, rh, !region.pixelate)
            Canvas(target).drawBitmap(big, left.toFloat(), top.toFloat(), null)
            if (big !== small) big.recycle()
            if (small !== piece) small.recycle()
            piece.recycle()
        }
        return target
    }

    /** [bitmap] itself when it can be drawn on, otherwise a copy that can (the original is recycled). */
    fun ensureMutable(bitmap: Bitmap): Bitmap {
        if (bitmap.isMutable) return bitmap
        val copy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        bitmap.recycle()
        return copy
    }
}
