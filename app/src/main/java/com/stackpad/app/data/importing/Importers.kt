package com.stackpad.app.data.importing

import android.content.ContentResolver
import android.net.Uri

/** Every importer, so the rest of the app picks one by [ImportKind]. */
class Importers(
    private val keep: KeepImporter,
    private val text: TextFileImporter,
    private val evernote: EvernoteImporter,
    private val word: DocxImporter,
    private val pdf: PdfImporter,
) {
    /** Imports the picked files with the importer for [kind]. Throws [ImportException]; nothing is added then. */
    suspend fun import(kind: ImportKind, resolver: ContentResolver, uris: List<Uri>): ImportSummary {
        val sources = resolver.importSources(uris)
        return when (kind) {
            // Takeout often splits big exports into several zips; they are imported one after the other.
            ImportKind.KEEP -> sources.map { source -> source.open().use { keep.import(it) } }
                .reduceOrNull(ImportSummary::plus) ?: throw ImportException(ImportError.NOTHING_FOUND)
            ImportKind.MARKDOWN -> text.import(sources)
            ImportKind.EVERNOTE -> evernote.import(sources)
            ImportKind.WORD -> word.import(sources)
            ImportKind.PDF -> pdf.import(sources)
        }
    }
}
