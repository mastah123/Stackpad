package com.stackpad.app.data.export

import android.graphics.Bitmap
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
import com.stackpad.app.data.media.BitmapLoader
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.blocks.BlockTone
import com.stackpad.app.domain.export.DocItem
import com.stackpad.app.domain.share.ExportPalette

/** An item that has been measured and can be drawn at the canvas origin. */
class PreparedItem(val height: Int, val draw: (Canvas) -> Unit)

/**
 * Measures and draws [DocItem]s for the image and PDF export. Sizes copy the app (body text 14,
 * headings 22/18/15, the list marker column, checklist card with its progress bar) on a
 * [DESIGN_WIDTH]-unit wide page, scaled so the same note looks alike in both formats.
 *
 * @param width content width in output pixels (or PDF points).
 * @param textScale 1 for the app's text size; headings, cards and boxes scale with it.
 */
class DocPainter(
    private val width: Int,
    private val media: MediaStorage,
    private val palette: ExportPalette,
    private val textScale: Float = 1f,
) {

    private val unit = width / DESIGN_WIDTH

    /** Space between two items. */
    val gap: Int = (2 * unit).toInt().coerceAtLeast(1)

    private val ink = palette.ink
    private val muted = palette.muted
    private val line = palette.line
    /** The color card tints are washed over. */
    private val tint = palette.card

    fun prepare(item: DocItem): PreparedItem = when (item) {
        is DocItem.Heading -> text(item.text, item.spans, headingSize(item.level), headingLine(item.level), bold = true)
        is DocItem.Paragraph -> text(item.text, item.spans, BODY, BODY_LINE)
        is DocItem.Row -> row(item)
        is DocItem.Checklist -> checklist(item)
        is DocItem.Quote -> quote(item)
        is DocItem.Code -> code(item)
        is DocItem.Link -> link(item)
        is DocItem.Image -> image(item)
        is DocItem.Table -> table(item)
        is DocItem.Audio -> audio(item.label)
        is DocItem.Meta -> meta(item)
        DocItem.Spacer -> spacer()
        DocItem.Divider -> divider()
    }

    // region Text

    private fun paint(sizeUnits: Float, color: Int = ink, bold: Boolean = false, mono: Boolean = false) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizeUnits * unit * textScale
            this.color = color
            typeface = when {
                mono -> Typeface.MONOSPACE
                bold -> Typeface.DEFAULT_BOLD
                else -> Typeface.DEFAULT
            }
        }

    private fun layout(text: CharSequence, paint: TextPaint, layoutWidth: Int, maxLines: Int = Int.MAX_VALUE, lineHeight: Float = BODY_LINE): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, layoutWidth.coerceAtLeast(1))
            .setLineSpacing(0f, lineHeight)
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
        TextLevel.H1 -> 22f
        TextLevel.H2 -> 18f
        else -> 15f
    }

    /** Line height divided by text size, as in the app's text styles (27/22, 23/18, 20/15). */
    private fun headingLine(level: TextLevel) = when (level) {
        TextLevel.H1 -> 27f / 22f
        TextLevel.H2 -> 23f / 18f
        else -> 20f / 15f
    }

    private fun text(text: String, spans: List<TextSpan>, size: Float, lineHeight: Float, bold: Boolean = false): PreparedItem {
        val layout = layout(styled(text, spans), paint(size, bold = bold), width, lineHeight = lineHeight)
        return PreparedItem(layout.height) { layout.draw(it) }
    }

    /** One blank line, like an empty text block in the note. */
    private fun spacer(): PreparedItem {
        val height = layout(" ", paint(BODY), width).height
        return PreparedItem(height) { }
    }

    private fun meta(item: DocItem.Meta): PreparedItem {
        val body = layout(item.text, paint(12f, muted), width, lineHeight = 15f / 12f)
        return PreparedItem(body.height) { canvas ->
            if (!item.centered) {
                body.draw(canvas)
            } else {
                canvas.save()
                canvas.translate((width - body.getLineWidth(0).coerceAtMost(width.toFloat())) / 2f, 0f)
                body.draw(canvas)
                canvas.restore()
            }
        }
    }

    /**
     * A list row. The marker sits right-aligned in a column as wide as the longest marker of its list
     * (8 per character plus 8, like the editor), so long items wrap under the text, not the marker.
     */
    private fun row(item: DocItem.Row): PreparedItem {
        val markerColumn = ((item.markerChars * 8 + 8) * unit * textScale).toInt()
        val markerGap = (6 * unit).toInt()
        val body = layout(item.text.ifEmpty { " " }, paint(BODY), (width - markerColumn).coerceAtLeast(1))
        val marker = TextPaint(paint(BODY, muted)).let { p ->
            StaticLayout.Builder.obtain(item.marker, 0, item.marker.length, p, (markerColumn - markerGap).coerceAtLeast(1))
                .setIncludePad(false)
                .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                .build()
        }
        return PreparedItem(maxOf(body.height, marker.height)) { canvas ->
            marker.draw(canvas)
            canvas.save()
            canvas.translate(markerColumn.toFloat(), 0f)
            body.draw(canvas)
            canvas.restore()
        }
    }

    /** One green card like the editor's: title with the done count, a progress bar, then a real box and the text of every task. */
    private fun checklist(item: DocItem.Checklist): PreparedItem {
        val tone = BlockTone.CHECKLIST
        val pad = (8 * unit).toInt()
        val box = (16 * unit * textScale).toInt()
        val boxGap = (8 * unit).toInt()
        val rowGap = (5 * unit).toInt()
        val barHeight = (4 * unit).toInt().coerceAtLeast(2)
        val textWidth = width - 2 * pad - box - boxGap

        val done = item.tasks.count { it.done }
        val count = if (item.tasks.isEmpty()) null else layout("$done/${item.tasks.size}", paint(12f, muted), width, lineHeight = 15f / 12f)
        val countWidth = count?.getLineWidth(0)?.toInt() ?: 0
        val title = item.title.takeIf { it.isNotBlank() }?.let {
            layout(it, paint(15f, tone.argb, bold = true), (width - 2 * pad - countWidth - pad).coerceAtLeast(1), lineHeight = 20f / 15f)
        }
        val header = maxOf(title?.height ?: 0, count?.height ?: 0)
        val rows = item.tasks.map { task ->
            task to layout(task.text, paint(BODY, if (task.done) muted else ink), textWidth)
        }

        var height = 2 * pad
        if (item.tasks.isNotEmpty() || header > 0) height += header + (if (header > 0) rowGap else 0)
        if (item.tasks.isNotEmpty()) height += barHeight + rowGap
        height += rows.sumOf { maxOf(it.second.height, box) } + rowGap * (rows.size - 1).coerceAtLeast(0)
        if (item.tasks.isEmpty() && header == 0) height = 2 * pad + box

        return PreparedItem(height) { canvas ->
            card(canvas, height, tone, radius = 14 * unit)
            var top = pad
            title?.let {
                canvas.save(); canvas.translate(pad.toFloat(), top + (header - it.height) / 2f); it.draw(canvas); canvas.restore()
            }
            count?.let {
                canvas.save(); canvas.translate(width - pad - countWidth.toFloat(), top + (header - it.height) / 2f); it.draw(canvas); canvas.restore()
            }
            if (header > 0) top += header + rowGap
            if (item.tasks.isNotEmpty()) {
                val bar = RectF(pad.toFloat(), top.toFloat(), (width - pad).toFloat(), (top + barHeight).toFloat())
                canvas.drawRoundRect(bar, barHeight / 2f, barHeight / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.track })
                val filled = bar.width() * done / item.tasks.size
                if (filled > 0f) {
                    canvas.drawRoundRect(RectF(bar.left, bar.top, bar.left + filled, bar.bottom), barHeight / 2f, barHeight / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.primary })
                }
                top += barHeight + rowGap
            }
            rows.forEach { (task, text) ->
                val rowHeight = maxOf(text.height, box)
                drawBox(canvas, pad.toFloat(), top + (rowHeight - box) / 2f, box.toFloat(), task.done, tone)
                canvas.save()
                canvas.translate((pad + box + boxGap).toFloat(), top + (rowHeight - text.height) / 2f)
                text.draw(canvas)
                if (task.done) strike(canvas, text)
                canvas.restore()
                top += rowHeight + rowGap
            }
        }
    }

    private fun drawBox(canvas: Canvas, left: Float, top: Float, size: Float, done: Boolean, tone: BlockTone) {
        val rect = RectF(left, top, left + size, top + size)
        val radius = size * 0.22f
        if (done) {
            canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tone.argb })
            val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = size * 0.13f
                strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
            }
            val path = android.graphics.Path().apply {
                moveTo(left + size * 0.24f, top + size * 0.52f)
                lineTo(left + size * 0.43f, top + size * 0.72f)
                lineTo(left + size * 0.76f, top + size * 0.30f)
            }
            canvas.drawPath(path, tick)
        } else {
            canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tone.argb; style = Paint.Style.STROKE; strokeWidth = size * 0.1f
            })
        }
    }

    /** Light card background for a block type: the tone washed over the page's light gray. */
    private fun container(tone: BlockTone): Int {
        val a = palette.toneStrength
        fun mix(c: Int, base: Int) = (c * a + base * (1 - a)).toInt()
        return Color.rgb(
            mix(Color.red(tone.argb), Color.red(tint)),
            mix(Color.green(tone.argb), Color.green(tint)),
            mix(Color.blue(tone.argb), Color.blue(tint)),
        )
    }

    private fun card(canvas: Canvas, height: Int, tone: BlockTone, outline: Boolean = false, radius: Float = 8 * unit) {
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = container(tone) })
        if (outline) {
            canvas.drawRoundRect(rect, radius, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (tone.argb and 0x00FFFFFF) or 0x66000000; style = Paint.Style.STROKE; strokeWidth = unit
            })
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
        val tone = BlockTone.QUOTE
        val pad = (8 * unit).toInt()
        val inset = pad + (6 * unit).toInt()
        val body = layout(item.text, paint(BODY, ink).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }, width - inset - pad)
        val height = body.height + 2 * pad
        return PreparedItem(height) { canvas ->
            card(canvas, height, tone)
            canvas.drawRoundRect(RectF(0f, 0f, 4 * unit, height.toFloat()), 2 * unit, 2 * unit, Paint().apply { color = tone.argb })
            canvas.save()
            canvas.translate(inset.toFloat(), pad.toFloat())
            body.draw(canvas)
            canvas.restore()
        }
    }

    private fun code(item: DocItem.Code): PreparedItem {
        val pad = (8 * unit).toInt()
        val mono = item.tone == BlockTone.CODE
        val body = layout(item.text, paint(if (mono) 12.5f else BODY, mono = mono), width - 2 * pad)
        val height = body.height + 2 * pad
        return PreparedItem(height) { canvas ->
            card(canvas, height, item.tone, outline = !mono)
            canvas.save()
            canvas.translate(pad.toFloat(), pad.toFloat())
            body.draw(canvas)
            canvas.restore()
        }
    }

    private fun link(item: DocItem.Link): PreparedItem {
        val tone = BlockTone.LINK
        val pad = (8 * unit).toInt()
        val title = item.title.ifBlank { item.url }
        val titleLayout = layout(title, paint(BODY, tone.argb, bold = true), width - 2 * pad)
        val urlLayout = item.url.takeIf { it.isNotBlank() && it != title }?.let { layout(it, paint(12f, muted), width - 2 * pad) }
        val height = 2 * pad + titleLayout.height + (urlLayout?.height ?: 0)
        return PreparedItem(height) { canvas ->
            card(canvas, height, tone)
            canvas.save()
            canvas.translate(pad.toFloat(), pad.toFloat())
            titleLayout.draw(canvas)
            urlLayout?.let {
                canvas.translate(0f, titleLayout.height.toFloat())
                it.draw(canvas)
            }
            canvas.restore()
        }
    }

    /** Neutral rounded label, used when something cannot be drawn (a missing picture). */
    private fun pill(label: String): PreparedItem {
        val pad = (8 * unit).toInt()
        val body = layout(label, paint(BODY, muted), width - 2 * pad, maxLines = 1)
        val height = body.height + 2 * pad
        return PreparedItem(height) { canvas ->
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), height / 2f, height / 2f, Paint().apply { color = tint })
            canvas.save()
            canvas.translate(pad * 1.5f, pad.toFloat())
            body.draw(canvas)
            canvas.restore()
        }
    }

    /** Rose pill with a round play button, like the player in the editor. */
    private fun audio(label: String): PreparedItem {
        val tone = BlockTone.AUDIO
        val pad = (6 * unit).toInt()
        val button = (28 * unit).toInt()
        val body = layout(label, paint(BODY, ink), width - 2 * pad - button - 3 * pad, maxLines = 1)
        val height = button + 2 * pad
        return PreparedItem(height) { canvas ->
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), height / 2f, height / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = container(tone) })
            val cx = pad + button / 2f
            val cy = height / 2f
            canvas.drawCircle(cx, cy, button / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tone.argb })
            val triangle = android.graphics.Path().apply {
                moveTo(cx - button * 0.12f, cy - button * 0.2f)
                lineTo(cx - button * 0.12f, cy + button * 0.2f)
                lineTo(cx + button * 0.22f, cy)
                close()
            }
            canvas.drawPath(triangle, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })
            canvas.save()
            canvas.translate(pad + button + 2f * pad, (height - body.height) / 2f)
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

    /** The picture at the full width of the page, as in the note (small pictures are enlarged), with rounded corners. */
    private fun image(item: DocItem.Image): PreparedItem {
        val bitmap = decode(item.fileName) ?: return pill("Image unavailable")
        val drawHeight = (bitmap.height.toFloat() * width / bitmap.width).toInt().coerceAtLeast(1)
        return PreparedItem(drawHeight) { canvas ->
            val rect = RectF(0f, 0f, width.toFloat(), drawHeight.toFloat())
            canvas.save()
            canvas.clipPath(android.graphics.Path().apply { addRoundRect(rect, 10 * unit, 10 * unit, android.graphics.Path.Direction.CW) })
            canvas.drawBitmap(bitmap, null, rect, Paint(Paint.FILTER_BITMAP_FLAG))
            canvas.restore()
        }
    }

    /** Decodes the picture upright and no larger than the page needs, or returns null when it cannot be read. */
    private fun decode(fileName: String): Bitmap? {
        val file = media.file(fileName)
        if (!file.exists()) return null
        return runCatching { BitmapLoader.loadFittingWidth(file, width) }.getOrNull()
    }

    // endregion

    // region Table

    private fun table(item: DocItem.Table): PreparedItem {
        val tone = BlockTone.TABLE
        val columns = item.rows.maxOf { it.size }.coerceAtLeast(1)
        val cell = width / columns
        val pad = (4 * unit).toInt()
        val layouts = item.rows.mapIndexed { r, row ->
            (0 until columns).map { c ->
                layout(row.getOrElse(c) { "" }, paint(13f, if (r == 0) tone.argb else ink, bold = r == 0), cell - 2 * pad, maxLines = 3)
            }
        }
        val heights = layouts.map { row -> row.maxOf { it.height } + 2 * pad }
        val total = heights.sum()
        return PreparedItem(total) { canvas ->
            val tableWidth = (cell * columns).toFloat()
            val border = Paint().apply { color = (tone.argb and 0x00FFFFFF) or 0x66000000; strokeWidth = unit; style = Paint.Style.STROKE }
            canvas.drawRect(0f, 0f, tableWidth, total.toFloat(), Paint().apply { color = container(tone) })
            canvas.drawRect(0f, 0f, tableWidth, heights[0].toFloat(), Paint().apply { color = (tone.argb and 0x00FFFFFF) or 0x33000000 })
            var top = 0
            layouts.forEachIndexed { r, row ->
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

        /** The app's body text: 14 with an 18 line height. */
        const val BODY = 14f
        const val BODY_LINE = 18f / 14f
    }
}
