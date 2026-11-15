package com.stackpad.app.domain.importing

import java.util.UUID

/** Limits and naming rules shared by every importer. Pure, so it is unit tested. */
object ImportRules {
    const val MAX_ENTRIES = 100_000
    const val MAX_JSON_BYTES = 8L * 1024 * 1024
    const val MAX_PICTURE_BYTES = 100L * 1024 * 1024
    const val MAX_TOTAL_BYTES = 4L * 1024 * 1024 * 1024

    private val pictureExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic")

    /** The same source always gives the same note id, so importing a file twice never duplicates notes. */
    fun noteId(source: String, sourceId: String): String =
        UUID.nameUUIDFromBytes("$source:$sourceId".toByteArray(Charsets.UTF_8)).toString()

    /** A zip entry path made safe to extract under a work folder, or null when it could escape it. */
    fun safePath(entryName: String): String? {
        val normalized = entryName.replace('\\', '/').trimStart('/')
        if (normalized.isEmpty()) return null
        val parts = normalized.split('/')
        if (parts.any { it == ".." || it.isEmpty() }) return null
        return normalized
    }

    fun isPicture(path: String): Boolean = path.substringAfterLast('.', "").lowercase() in pictureExtensions

    fun isJson(path: String): Boolean = path.substringAfterLast('.', "").equals("json", ignoreCase = true)

    /**
     * Finds the extracted file an attachment refers to. Keep writes ".jpeg" in its JSON but ".jpg"
     * on disk (and the other way round), so the extension is matched loosely and the folder of the
     * note comes first.
     */
    fun resolvePicture(attachmentPath: String, noteFolder: String, available: Set<String>): String? {
        val name = attachmentPath.replace('\\', '/').substringAfterLast('/')
        val base = name.substringBeforeLast('.')
        val candidates = listOf(name, "$base.jpg", "$base.jpeg", "$base.png", "$base.webp")
        val folders = listOf(noteFolder, "")
        for (candidate in candidates) {
            for (folder in folders) {
                val path = if (folder.isEmpty()) candidate else "$folder/$candidate"
                available.firstOrNull { it.equals(path, ignoreCase = true) }?.let { return it }
            }
        }
        return available.firstOrNull { it.substringAfterLast('/').equals(name, ignoreCase = true) }
    }
}
