package com.stackpad.app.data.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.stackpad.app.domain.image.EditGeometry
import com.stackpad.app.domain.image.ImageEdit

/** Draws a picture with an [ImageEdit] applied. The sizes come from [EditGeometry], so the preview and the saved file agree. */
object ImageTransformer {

    fun apply(source: Bitmap, edit: ImageEdit): Bitmap {
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
        Canvas(result).drawBitmap(source, matrix, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG))
        return result
    }
}
