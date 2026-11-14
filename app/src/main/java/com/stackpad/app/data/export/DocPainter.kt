package com.stackpad.app.data.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.export.DocItem

/** An item that has been measured and can be drawn at the canvas origin. */
class PreparedItem(val height: Int, val draw: (Canvas) -> Unit)

/**
 * Measures and draws [DocItem]s for the image and PDF export. Layout is designed on a
 * [DESIGN_WIDTH]-unit wide page and scaled so the same note looks alike in both formats.
 *
 * @param width content width in output pixels (or PDF points).
 */
class DocPainter(private val width: Int, private val media: MediaStorage) {

    private val unit = width / DESIGN_WIDTH

    /** Space between two items. */
    val gap: Int = (6 * unit).toInt()

    private val ink = Color.rgb(28, 28, 32)
    private val muted = Color.rgb(110, 110, 120)
    private val line = Color.rgb(210, 210, 216)
    private val tint = Color.rgb(240, 241, 245)
    private val accent = Color.rgb(52, 92, 200)

    fun prepare(item: DocItem): PreparedItem = when (item) {
        is DocItem.Heading -> text(item.text, item.spans, headingSize(item.level), bold = true)
        is DocItem.Paragraph -> text(item.text, item.spans, 15f)
        is DocItem.Row -> row(item)
        is DocItem.Quote -> quote(item)
        is DocItem.Code -> code(item)
        is DocItem.Link -> link(item)
        is DocItem.Image -> image(item)
        is DocItem.Table -> table(item)
        is DocItem.Audio -> pill(item.label)
        DocItem.Divider -> divider()
    }

    // region Text

    private fun paint(sizeUnits: Float, color: Int = ink, bold: Boolean = false, mono: Boolean = false) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizeUnits * unit
            this.color = color
            typeface = when {
                mono -> Typeface.MONOSPACE
                bold -> Typeface.DEFAULT_BOLD
                else -> Typeface.DEFAULT
            }
        }

    private fun layout(text: CharSequence, paint: TextPaint, layoutWidth: Int, maxLines: Int = Int.MAX_VALUE): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, layoutWidth.coerceAtLeast(1))
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(maxLines)
            .setEllipsize(if (maxLines == Int.MAX_VALUE) null else TextUtils.TruncateAt.END)
            .build()

    private fun styled(text: String, spans: List<TextSpan>): CharSequence {
        if (spans.isEmpty()) return text
        val out = SpannableStringBuilder(text)
        spans.forEach { span ->
            val start = span.start.coerceIn(0, text.length)
            val end = span.end.coerceIn(0, text.length)
            if (start >= end) return@forEach
            val what: Any = when (span.type) {
                SpanType.BOLD -> StyleSpan(Typeface.BOLD)
                SpanType.ITALIC -> StyleSpan(Typeface.ITALIC)
                SpanType.UNDERLINE -> UnderlineSpan()
            }
            out.setSpan(what, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return out
    }

    private fun headingSize(level: TextLevel) = when (level) {
        TextLevel.H1 -> 24f
        TextLevel.H2 -> 20f
        else -> 17f
    }

    private fun text(text: String, spans: List<TextSpan>, size: Float, bold: Boolean = false): PreparedItem {
        val layout = layout(styled(text, spans), paint(size, bold = bold), width)
        return PreparedItem(layout.height) { layout.draw(it) }
    }

    private fun row(item: DocItem.Row): PreparedItem {
        val markerWidth = (24 * unit).toInt()
        val color = if (item.done) muted else ink
        val body = layout(item.text, paint(15f, color), width - markerWidth)
        val marker = layout(item.marker, paint(15f, if (item.done) muted else accent), markerWidth)
        return PreparedItem(maxOf(body.height, marker.height)) { canvas ->
            marker.draw(canvas)
            canvas.save()
            canvas.translate(markerWidth.toFloat(), 0f)
            body.draw(canvas)
            if (item.done) strike(canvas, body)
            canvas.restore()
        }
    }

    private fun strike(canvas: Canvas, layout: StaticLayout) {
        val p = Paint().apply { color = muted; strokeWidth = unit }
        for (i in 0 until layout.lineCount) {
            val y = (layout.getLineTop(i) + layout.getLineBottom(i)) / 2f
            canvas.drawLine(layout.getLineLeft(i), y, layout.getLineRight(i), y, p)
        }
    }

    private fun quote(item: DocItem.Quote): PreparedItem {
        val inset = (12 * unit).toInt()
        val body = layout(item.text, paint(15f, muted).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }, width - inset)
        return PreparedItem(body.height) { canvas ->
            canvas.drawRect(0f, 0f, 3 * unit, body.height.toFloat(), Paint().apply { color = accent })
            canvas.save()
            canvas.translate(inset.toFloat(), 0f)
            body.draw(canvas)
            canvas.restore()
        }
    }

    private fun code(item: DocItem.Code): PreparedItem {
        val pad = (8 * unit).toInt()
        val body = layout(item.text, paint(12.5f, mono = true), width - 2 * pad)
        val height = body.height + 2 * pad
        return PreparedItem(height) { canvas ->
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 6 * unit, 6 * unit, Paint().apply { color = tint })
            canvas.save()
            canvas.translate(pad.toFloat(), pad.toFloat())
            body.draw(canvas)
            canvas.restore()
        }
    }

    private fun link(item: DocItem.Link): PreparedItem {
        val title = item.title.ifBlank { item.url }
        val titleLayout = layout(title, paint(15f, accent, bold = true), width)
        val urlLayout = item.url.takeIf { it.isNotBlank() && it != title }?.let { layout(it, paint(12f, muted), width) }
        val total = titleLayout.height + (urlLayout?.height ?: 0)
        return PreparedItem(total) { canvas ->
            titleLayout.draw(canvas)
            urlLayout?.let {
                canvas.save()
                canvas.translate(0f, titleLayout.height.toFloat())
                it.draw(canvas)
                canvas.restore()
            }
        }
    }

    private fun pill(label: String): PreparedItem {
        val pad = (8 * unit).toInt()
        val body = layout(label, paint(14f, muted), width - 2 * pad, maxLines = 1)
        val height = body.height + 2 * pad
        return PreparedItem(height) { canvas ->
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), height / 2f, height / 2f, Paint().apply { color = tint })
            canvas.save()
            canvas.translate(pad * 1.5f, pad.toFloat())
            body.draw(canvas)
            canvas.restore()
        }
    }

    private fun divider(): PreparedItem {
        val height = (12 * unit).toInt()
        return PreparedItem(height) { canvas ->
            canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, Paint().apply { color = line; strokeWidth = unit })
        }
    }

    // endregion

    // region Image

    private fun image(item: DocItem.Image): PreparedItem {
        val bitmap = decode(item.fileName)
        val caption = item.caption.takeIf { it.isNotBlank() }?.let { layout(it, paint(12f, muted), width) }
        if (bitmap == null) return pill("Image unavailable")

        val scale = minOf(1f, width.toFloat() / bitmap.width)
        val drawWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val drawHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val captionGap = if (caption != null) (4 * unit).toInt() else 0
        val total = drawHeight + captionGap + (caption?.height ?: 0)
        return PreparedItem(total) { canvas ->
            val left = (width - drawWidth) / 2f
            canvas.drawBitmap(bitmap, null, RectF(left, 0f, left + drawWidth, drawHeight.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
            caption?.let {
                canvas.save()
                canvas.translate(0f, (drawHeight + captionGap).toFloat())
                it.draw(canvas)
                canvas.restore()
            }
        }
    }

    /** Decodes the picture no larger than the page needs, or returns null when it cannot be read. */
    private fun decode(fileName: String): Bitmap? {
        val file = media.file(fileName)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= width) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    // endregion

    // region Table

    private fun table(item: DocItem.Table): PreparedItem {
        val columns = item.rows.maxOf { it.size }.coerceAtLeast(1)
        val cell = width / columns
        val pad = (4 * unit).toInt()
        val layouts = item.rows.mapIndexed { r, row ->
            (0 until columns).map { c ->
                layout(row.getOrElse(c) { "" }, paint(13f, bold = r == 0), cell - 2 * pad, maxLines = 3)
            }
        }
        val heights = layouts.map { row -> row.maxOf { it.height } + 2 * pad }
        val total = heights.sum()
        return PreparedItem(total) { canvas ->
            val border = Paint().apply { color = line; strokeWidth = unit; style = Paint.Style.STROKE }
            val header = Paint().apply { color = tint }
            var top = 0
            layouts.forEachIndexed { r, row ->
                if (r == 0) canvas.drawRect(0f, 0f, cell * columns.toFloat(), heights[0].toFloat(), header)
                row.forEachIndexed { c, cellLayout ->
                    canvas.save()
                    canvas.translate((c * cell + pad).toFloat(), (top + pad).toFloat())
                    cellLayout.draw(canvas)
                    canvas.restore()
                    canvas.drawRect((c * cell).toFloat(), top.toFloat(), ((c + 1) * cell).toFloat(), (top + heights[r]).toFloat(), border)
                }
                top += heights[r]
            }
        }
    }

    // endregion

    private companion object {
        const val DESIGN_WIDTH = 360f
    }
}
