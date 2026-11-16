package com.stackpad.app.data.importing

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/** Wraps picked files as [ImportSource]s, using each file's display name (its extension tells the type). */
internal fun ContentResolver.importSources(uris: List<Uri>): List<ImportSource> = uris.map { uri ->
    val name = runCatching {
        query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/').orEmpty()
    ImportSource(name) { openInputStream(uri) ?: throw ImportException(ImportError.IO) }
}
