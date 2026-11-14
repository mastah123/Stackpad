package com.stackpad.app.data.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.Note
import com.stackpad.app.domain.export.DocItems
import com.stackpad.app.domain.export.Paginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** The note is longer than one picture can be. */
class NoteTooTallException : Exception()

/** Writes a note as a PNG picture or a paginated A4 PDF into the app's cache, ready to be shared. */
class NoteFileExporter(private val context: Context, private val media: MediaStorage) {

    private val directory: File get() = File(context.cacheDir, "exports").apply { mkdirs() }

    /** One tall PNG. Throws [NoteTooTallException] when the note would not fit in [MAX_IMAGE_HEIGHT] pixels. */
    suspend fun toPng(note: Note): File = withContext(Dispatchers.Default) {
        val painter = DocPainter(IMAGE_WIDTH - 2 * IMAGE_MARGIN, media)
        val prepared = DocItems.from(note).map(painter::prepare)
        val height = IMAGE_MARGIN * 2 + prepared.sumOf { it.height } + painter.gap * (prepared.size - 1).coerceAtLeast(0)
        if (height > MAX_IMAGE_HEIGHT) throw NoteTooTallException()

        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.translate(IMAGE_MARGIN.toFloat(), IMAGE_MARGIN.toFloat())
            prepared.forEach { item ->
                item.draw(canvas)
                canvas.translate(0f, (item.height + painter.gap).toFloat())
            }
            val file = newFile(note, "png")
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) { "PNG encoding failed" } }
            file
        } finally {
            bitmap.recycle()
        }
    }

    /** A4 pages (595 x 842 pt). Items that fit are kept whole; taller ones continue on the next page. */
    suspend fun toPdf(note: Note): File = withContext(Dispatchers.Default) {
        val contentWidth = PAGE_WIDTH - 2 * PAGE_MARGIN
        val contentHeight = PAGE_HEIGHT - 2 * PAGE_MARGIN
        val painter = DocPainter(contentWidth, media)
        val prepared = DocItems.from(note).map(painter::prepare)
        val pages = Paginator.paginate(prepared.map { it.height }, contentHeight, painter.gap)

        val pdf = PdfDocument()
        try {
            pages.ifEmpty { listOf(emptyList()) }.forEachIndexed { number, slices ->
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number + 1).create())
                val canvas = page.canvas
                canvas.translate(PAGE_MARGIN.toFloat(), PAGE_MARGIN.toFloat())
                var top = 0
                slices.forEach { slice ->
                    if (top > 0) top += painter.gap
                    canvas.save()
                    canvas.translate(0f, top.toFloat())
                    canvas.clipRect(0f, 0f, contentWidth.toFloat(), slice.height.toFloat())
                    canvas.translate(0f, -slice.offset.toFloat())
                    prepared[slice.index].draw(canvas)
                    canvas.restore()
                    top += slice.height
                }
                pdf.finishPage(page)
            }
            val file = newFile(note, "pdf")
            file.outputStream().use { pdf.writeTo(it) }
            file
        } finally {
            pdf.close()
        }
    }

    /** Old exports are removed first so the cache never grows. */
    @Throws(IOException::class)
    private fun newFile(note: Note, extension: String): File {
        directory.listFiles()?.forEach { it.delete() }
        val base = note.title.trim().replace(Regex("[^\\p{L}\\p{N} _-]"), "").trim().take(40).ifEmpty { "note" }
        return File(directory, "$base.$extension")
    }

    private companion object {
        const val IMAGE_WIDTH = 1080
        const val IMAGE_MARGIN = 54
        const val MAX_IMAGE_HEIGHT = 12_000
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val PAGE_MARGIN = 40
    }
}
