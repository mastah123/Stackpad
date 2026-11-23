package com.stackpad.app.domain.sheet

/** Reads and writes CSV (RFC 4180 style: quoted fields, doubled quotes, line breaks inside quotes). */
object CsvCodec {
    /** Result of reading CSV: rectangular [cells] limited to the table size, and whether data was cut off. */
    data class Parsed(val cells: List<List<String>>, val truncated: Boolean)

    fun encode(cells: List<List<String>>): String =
        cells.joinToString("\r\n") { row -> row.joinToString(",") { quote(it) } }

    private fun quote(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    /** Returns null when [text] holds no cells at all. Detects comma, semicolon or tab separators. */
    fun parse(text: String, maxRows: Int = SheetOps.MAX_ROWS, maxColumns: Int = SheetOps.MAX_COLUMNS): Parsed? {
        val body = text.removePrefix("﻿")
        val rows = readRows(body, detectDelimiter(body))
            .dropLastWhile { row -> row.all { it.isEmpty() } }
        if (rows.isEmpty()) return null
        val width = rows.maxOf { it.size }
        val truncated = rows.size > maxRows || width > maxColumns
        val rowCount = minOf(rows.size, maxRows)
        val columnCount = minOf(width, maxColumns)
        val cells = rows.take(rowCount).map { row -> List(columnCount) { row.getOrElse(it) { "" } } }
        return Parsed(cells, truncated)
    }

    /** The first of `,` `;` tab that appears outside quotes on the first line; comma otherwise. */
    private fun detectDelimiter(text: String): Char {
        var inQuotes = false
        val counts = mutableMapOf(',' to 0, ';' to 0, '\t' to 0)
        for (ch in text) {
            if (ch == '"') inQuotes = !inQuotes
            if (!inQuotes && (ch == '\n' || ch == '\r')) break
            if (!inQuotes && ch in counts) counts[ch] = counts.getValue(ch) + 1
        }
        val best = counts.maxByOrNull { it.value }
        return if (best == null || best.value == 0) ',' else best.key
    }

    private fun readRows(text: String, delimiter: Char): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        fun endField() {
            row.add(field.toString())
            field.setLength(0)
        }
        fun endRow() {
            endField()
            rows.add(row)
            row = mutableListOf()
        }
        while (i < text.length) {
            val ch = text[i]
            when {
                inQuotes && ch == '"' && text.getOrNull(i + 1) == '"' -> {
                    field.append('"')
                    i++
                }
                ch == '"' -> inQuotes = !inQuotes
                inQuotes -> field.append(ch)
                ch == delimiter -> endField()
                ch == '\r' -> {
                    if (text.getOrNull(i + 1) == '\n') i++
                    endRow()
                }
                ch == '\n' -> endRow()
                else -> field.append(ch)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) endRow()
        return rows
    }
}
