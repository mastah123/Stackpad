package com.stackpad.app.domain.text

import com.stackpad.app.data.model.ListKind

/** Marker text for list items: bullets, `1.` numbers and `a.` letters (a..z, aa, ab, ...). */
object ListMarkers {

    fun label(kind: ListKind, index: Int): String = when (kind) {
        ListKind.BULLET -> "•"
        ListKind.NUMBERED -> "${index + 1}."
        ListKind.LETTERED -> "${letters(index)}."
    }

    fun letters(index: Int): String {
        var n = index
        val sb = StringBuilder()
        do {
            sb.append('a' + n % 26)
            n = n / 26 - 1
        } while (n >= 0)
        return sb.reverse().toString()
    }
}
