package com.stackpad.app.domain.text

import com.stackpad.app.data.model.ListKind

/** True for the three bullet styles. */
val ListKind.isBullet: Boolean
    get() = this == ListKind.BULLET || this == ListKind.BULLET_HOLLOW || this == ListKind.BULLET_SQUARE

/** Marker text for list items: bullets, `1.` numbers, `a.`/`A.` letters, `i.`/`I.` roman numerals and `α.` Greek letters. */
object ListMarkers {

    /** The styles offered under "Bullets", "Numbers" and "Letters" when adding a list. */
    val BULLET_KINDS = listOf(ListKind.BULLET, ListKind.BULLET_HOLLOW, ListKind.BULLET_SQUARE)
    val NUMBER_KINDS = listOf(ListKind.NUMBERED, ListKind.UPPER_ROMAN, ListKind.LOWER_ROMAN)
    val LETTER_KINDS = listOf(ListKind.LETTERED, ListKind.UPPER_ALPHA, ListKind.GREEK)

    private val GREEK = "αβγδεζηθικλμνξοπρστυφχψω"
    private val ROMAN = listOf(
        1000 to "m", 900 to "cm", 500 to "d", 400 to "cd", 100 to "c", 90 to "xc",
        50 to "l", 40 to "xl", 10 to "x", 9 to "ix", 5 to "v", 4 to "iv", 1 to "i",
    )

    fun label(kind: ListKind, index: Int): String = when (kind) {
        ListKind.BULLET -> "•"
        ListKind.BULLET_HOLLOW -> "○"
        ListKind.BULLET_SQUARE -> "▪"
        ListKind.NUMBERED -> "${index + 1}."
        ListKind.LETTERED -> "${letters(index)}."
        ListKind.UPPER_ALPHA -> "${letters(index).uppercase()}."
        ListKind.LOWER_ROMAN -> "${roman(index + 1)}."
        ListKind.UPPER_ROMAN -> "${roman(index + 1).uppercase()}."
        ListKind.GREEK -> "${sequence(index, GREEK)}."
    }

    fun letters(index: Int): String = sequence(index, "abcdefghijklmnopqrstuvwxyz")

    /** a..z, then aa, ab, ... (the same pattern for any alphabet). */
    private fun sequence(index: Int, alphabet: String): String {
        var n = index
        val sb = StringBuilder()
        do {
            sb.append(alphabet[n % alphabet.length])
            n = n / alphabet.length - 1
        } while (n >= 0)
        return sb.reverse().toString()
    }

    /** Lowercase roman numeral for 1..3999; larger numbers fall back to digits. */
    fun roman(number: Int): String {
        if (number !in 1..3999) return number.toString()
        var left = number
        val sb = StringBuilder()
        for ((value, text) in ROMAN) {
            while (left >= value) {
                sb.append(text)
                left -= value
            }
        }
        return sb.toString()
    }
}
