package com.stackpad.app.data.importing

import java.io.InputStream

/** Why an import failed. */
enum class ImportError { NOT_AN_EXPORT, NOTHING_FOUND, TOO_LARGE, IO }

class ImportException(val error: ImportError, cause: Throwable? = null) : Exception(error.name, cause)

/** What an import did, for the message shown afterwards. */
data class ImportSummary(
    val imported: Int,
    /** Notes from an earlier import of the same file, left as they are. */
    val alreadyImported: Int,
    /** Notes that were in the source's trash. */
    val skippedTrashed: Int,
    /** Files that looked like notes but could not be read. */
    val unreadable: Int,
    val pictures: Int,
    /** Audio, drawings and pictures that could not be imported. */
    val skippedAttachments: Int,
) {
    operator fun plus(other: ImportSummary) = ImportSummary(
        imported + other.imported,
        alreadyImported + other.alreadyImported,
        skippedTrashed + other.skippedTrashed,
        unreadable + other.unreadable,
        pictures + other.pictures,
        skippedAttachments + other.skippedAttachments,
    )
}

/** A file the person picked: its display name and a way to read it. */
class ImportSource(val name: String, val open: () -> InputStream)

/** The apps and file types notes can be imported from. */
enum class ImportKind { KEEP, MARKDOWN, EVERNOTE, WORD, PDF }
