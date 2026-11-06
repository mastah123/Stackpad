package com.stackpad.app.domain.formula

/** Spreadsheet column naming: 0 -> A, 25 -> Z, 26 -> AA. */
fun columnLabel(index: Int): String {
    var n = index
    val label = StringBuilder()
    do {
        label.append('A' + n % 26)
        n = n / 26 - 1
    } while (n >= 0)
    return label.reverse().toString()
}

/** Inverse of [columnLabel]. [letters] must contain only A-Z (any case). */
fun columnIndex(letters: String): Int =
    letters.uppercase().fold(0) { acc, ch -> acc * 26 + (ch - 'A' + 1) } - 1
