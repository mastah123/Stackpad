package com.stackpad.app.domain.sheet

import com.stackpad.app.domain.formula.FormulaErrors
import com.stackpad.app.domain.formula.columnIndex
import com.stackpad.app.domain.formula.columnLabel

/** Rewrites the cell references inside formulas when rows or columns are inserted, removed or moved. */
object FormulaRefs {
    // A1, b12, AA3 ... but not a function name such as LOG10( .
    private val REF = Regex("""\b([A-Za-z]{1,3})(\d+)\b(?!\()""")

    /**
     * Applies [row] and [col] (old index -> new index, or null when that row/column is gone) to every
     * reference in [raw]. Text that is not a formula is returned as is; a reference to a removed cell becomes #REF!.
     */
    fun remap(raw: String, row: (Int) -> Int?, col: (Int) -> Int?): String {
        if (!raw.startsWith("=")) return raw
        return REF.replace(raw) { match ->
            val letters = match.groupValues[1]
            val newCol = col(columnIndex(letters))
            val newRow = row(match.groupValues[2].toInt() - 1)
            if (newCol == null || newRow == null) {
                FormulaErrors.REF
            } else {
                val label = columnLabel(newCol).let { if (letters.first().isLowerCase()) it.lowercase() else it }
                "$label${newRow + 1}"
            }
        }
    }
}
