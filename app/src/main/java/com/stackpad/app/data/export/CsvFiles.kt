package com.stackpad.app.data.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

/** Reads a CSV file the user picked and writes one to the share cache. */
object CsvFiles {
    private const val MAX_BYTES = 2_000_000

    /** The file's text, or null when it cannot be read or is too large. */
    fun read(resolver: ContentResolver, uri: Uri): String? = runCatching {
        resolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                out.write(buffer, 0, n)
                if (out.size() > MAX_BYTES) return@use null
            }
            String(out.toByteArray(), Charsets.UTF_8)
        }
    }.getOrNull()

    /** Writes [csv] (with a UTF-8 byte-order mark so spreadsheet apps read accents) to the exports folder. */
    fun write(context: Context, name: String, csv: String): File? = runCatching {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        File(dir, name).apply { writeText("﻿$csv", Charsets.UTF_8) }
    }.getOrNull()
}
