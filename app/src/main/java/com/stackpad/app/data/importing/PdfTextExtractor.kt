package com.stackpad.app.data.importing

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

/** Reads the text of each page of a PDF with PdfBox-Android. The only file that uses that library. */
internal class PdfTextExtractor(context: Context) {
    private val appContext = context.applicationContext

    /** One string per page (empty for pages without text, such as scans). Throws when the PDF cannot be read. */
    fun pages(file: File): List<String> {
        PDFBoxResourceLoader.init(appContext)
        val document = PDDocument.load(file)
        try {
            val stripper = PDFTextStripper()
            return (1..document.numberOfPages).map { page ->
                stripper.startPage = page
                stripper.endPage = page
                stripper.getText(document)
            }
        } finally {
            document.close()
        }
    }
}
