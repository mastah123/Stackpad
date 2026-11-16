package com.stackpad.app.data.importing

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import kotlin.math.roundToInt

/** Draws PDF pages as JPEG pictures with the system's PdfRenderer; used for scans, which have no text. */
internal object PdfPageRenderer {
    const val MAX_PAGES = 50
    private const val WIDTH = 1200
    private const val QUALITY = 85

    /** Renders up to [MAX_PAGES] pages into [outDir]. Returns the page files in order and how many pages were left out. */
    fun render(pdf: File, outDir: File): Pair<List<File>, Int> {
        val pages = ArrayList<File>()
        val descriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        try {
            val count = minOf(renderer.pageCount, MAX_PAGES)
            for (index in 0 until count) {
                val page = renderer.openPage(index)
                try {
                    val height = (WIDTH.toFloat() * page.height / page.width).roundToInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val file = File(outDir, "page-$index.jpg")
                    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
                    bitmap.recycle()
                    pages += file
                } finally {
                    page.close()
                }
            }
            return pages to (renderer.pageCount - count)
        } finally {
            renderer.close()
            descriptor.close()
        }
    }
}
