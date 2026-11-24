package com.stackpad.app.data.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import com.stackpad.app.domain.image.Annotation
import com.stackpad.app.domain.image.EditGeometry
import com.stackpad.app.domain.image.ImageFilter
import com.stackpad.app.domain.image.ImageEdit

/** Draws a picture with an [ImageEdit] applied. The sizes come from [EditGeometry], so the preview and the saved file agree. */
object ImageTransformer {

    /**
     * Mirror, turn, straighten, crop and colour look; then the size limit, hidden regions and (with
     * [withMarks]) pen, highlighter and text. The editor draws the marks itself while you work.
     */
    fun apply(source: Bitmap, edit: ImageEdit, withMarks: Boolean = true): Bitmap {
        val base = EditGeometry.baseSize(source.width, source.height, edit)
        val crop = EditGeometry.cropPixels(base, edit.crop)
        val result = Bitmap.createBitmap(crop.width, crop.height, Bitmap.Config.ARGB_8888)
        result.eraseColor(Color.WHITE)

        val matrix = Matrix().apply {
            postTranslate(-source.width / 2f, -source.height / 2f)
            if (edit.mirrored) postScale(-1f, 1f)
            postRotate(edit.quarterTurns * 90f)
            postRotate(edit.straighten)
            postTranslate(base.width / 2f - crop.left, base.height / 2f - crop.top)
        }
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        if (edit.filter != ImageFilter.NONE) paint.colorFilter = ColorMatrixColorFilter(ColorMatrix(edit.filter.matrix))
        Canvas(result).drawBitmap(source, matrix, paint)
        return finish(result, edit, withMarks)
    }

    private fun finish(drawn: Bitmap, edit: ImageEdit, withMarks: Boolean): Bitmap {
        var out = drawn
        val (width, height) = edit.size.fit(out.width, out.height)
        if (width != out.width || height != out.height) {
            val scaled = Bitmap.createScaledBitmap(out, width, height, true)
            if (scaled !== out) out.recycle()
            out = scaled
        }
        val hidden = edit.annotations.filterIsInstance<Annotation.Obscure>()
        if (hidden.isNotEmpty()) out = AnnotationPainter.obscure(out, hidden)
        val marks = edit.annotations.filter { it !is Annotation.Obscure }
        if (withMarks && marks.isNotEmpty()) {
            out = AnnotationPainter.ensureMutable(out)
            AnnotationPainter.drawMarks(Canvas(out), out.width, out.height, marks)
        }
        return out
    }
}
