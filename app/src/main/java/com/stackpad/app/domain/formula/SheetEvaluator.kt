package com.stackpad.app.domain.formula

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/** Thrown while evaluating a formula; [code] is the text shown in the cell. */
class FormulaException(val code: String) : Exception(code)

/** Error codes shown in cells. */
object FormulaErrors {
    const val SYNTAX = "#ERR!"
    const val DIV_ZERO = "#DIV/0!"
    const val CYCLE = "#CYCLE!"
    const val REF = "#REF!"
    const val VALUE = "#VALUE!"
    const val NUM = "#NUM!"
    const val NAME = "#NAME?"
}

/**
 * Evaluates the cells of one spreadsheet.
 *
 * A cell whose raw text starts with `=` is a formula supporting numbers, cell references
 * (`A1`, `b2`), `+ - * /`, unary minus, parentheses, and the functions `SUM`, `AVERAGE`,
 * `MIN`, `MAX` and `COUNT` over cells, ranges (`A1:A5`) and expressions. In a range, blank
 * and text cells are skipped. Everything else is plain text.
 * Create one evaluator per snapshot of cells; results are cached.
 */
class SheetEvaluator(private val cells: List<List<String>>) {

    private val cache = HashMap<Pair<Int, Int>, Double>()

    /** The text to show for a cell: the formula result, an error code, or the raw text. */
    fun display(row: Int, col: Int): String {
        val raw = rawAt(row, col)
        if (!raw.trimStart().startsWith("=")) return raw
        return try {
            formatNumber(numberAt(row, col, HashSet()))
        } catch (e: FormulaException) {
            e.code
        }
    }

    private fun rawAt(row: Int, col: Int): String = cells.getOrNull(row)?.getOrNull(col).orEmpty()

    private fun numberAt(row: Int, col: Int, visiting: MutableSet<Pair<Int, Int>>): Double {
        val key = row to col
        cache[key]?.let { return it }
        if (!visiting.add(key)) throw FormulaException(FormulaErrors.CYCLE)
        try {
            val raw = rawAt(row, col).trim()
            val value = when {
                raw.isEmpty() -> 0.0
                raw.startsWith("=") -> ExpressionParser(
                    source = raw.substring(1),
                    resolve = { r, c ->
                        checkInside(r, c)
                        numberAt(r, c, visiting)
                    },
                    resolveRange = { r1, c1, r2, c2 -> rangeNumbers(r1, c1, r2, c2, visiting) },
                ).parse()
                else -> raw.toDoubleOrNull()?.takeIf { it.isFinite() }
                    ?: throw FormulaException(FormulaErrors.VALUE)
            }
            cache[key] = value
            return value
        } finally {
            visiting.remove(key)
        }
    }

    private fun checkInside(row: Int, col: Int) {
        if (row !in cells.indices || col !in cells[row].indices) throw FormulaException(FormulaErrors.REF)
    }

    /** Numeric values in a rectangle, in row order. Blank and text cells are skipped. */
    private fun rangeNumbers(
        r1: Int, c1: Int, r2: Int, c2: Int,
        visiting: MutableSet<Pair<Int, Int>>,
    ): List<Double> {
        val rows = minOf(r1, r2)..maxOf(r1, r2)
        val cols = minOf(c1, c2)..maxOf(c1, c2)
        val values = ArrayList<Double>()
        for (r in rows) for (c in cols) {
            checkInside(r, c)
            val raw = rawAt(r, c).trim()
            when {
                raw.isEmpty() -> Unit
                raw.startsWith("=") -> values += numberAt(r, c, visiting)
                else -> raw.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { values += it }
            }
        }
        return values
    }

    companion object {
        /** Whole numbers print without decimals; others keep up to 10 significant digits. */
        fun formatNumber(value: Double): String {
            if (value == Math.rint(value) && abs(value) < 1e15) return value.toLong().toString()
            return BigDecimal(value).round(MathContext(10)).stripTrailingZeros().toPlainString()
        }
    }
}

/** Recursive-descent parser: expr = term (('+'|'-') term)*, term = unary (('*'|'/') unary)*. */
private class ExpressionParser(
    private val source: String,
    private val resolve: (row: Int, col: Int) -> Double,
    private val resolveRange: (r1: Int, c1: Int, r2: Int, c2: Int) -> List<Double>,
) {
    private var pos = 0

    fun parse(): Double {
        val value = expression()
        skipWhitespace()
        if (pos != source.length) throw FormulaException(FormulaErrors.SYNTAX)
        if (!value.isFinite()) throw FormulaException(FormulaErrors.NUM)
        return value
    }

    private fun expression(): Double {
        var value = term()
        while (true) {
            skipWhitespace()
            val c = peek()
            if (c == '+') {
                pos++
                value += term()
            } else if (c == '-') {
                pos++
                value -= term()
            } else {
                break
            }
        }
        return value
    }

    private fun term(): Double {
        var value = unary()
        while (true) {
            skipWhitespace()
            val c = peek()
            if (c == '*') {
                pos++
                value *= unary()
            } else if (c == '/') {
                pos++
                val divisor = unary()
                if (divisor == 0.0) throw FormulaException(FormulaErrors.DIV_ZERO)
                value /= divisor
            } else {
                break
            }
        }
        return value
    }

    private fun unary(): Double {
        skipWhitespace()
        return when (peek()) {
            '-' -> {
                pos++
                -unary()
            }
            '+' -> {
                pos++
                unary()
            }
            else -> primary()
        }
    }

    private fun primary(): Double {
        skipWhitespace()
        val c = peek()
        return when {
            c == '(' -> {
                pos++
                val value = expression()
                skipWhitespace()
                if (peek() != ')') throw FormulaException(FormulaErrors.SYNTAX)
                pos++
                value
            }
            c.isAsciiDigit() || c == '.' -> number()
            c.isAsciiLetter() -> referenceOrFunction()
            else -> throw FormulaException(FormulaErrors.SYNTAX)
        }
    }

    private fun number(): Double {
        val start = pos
        while (peek().isAsciiDigit() || peek() == '.') pos++
        return source.substring(start, pos).toDoubleOrNull()
            ?: throw FormulaException(FormulaErrors.SYNTAX)
    }

    /** A cell reference such as `B2`, or a function call such as `SUM(A1:A3)`. */
    private fun referenceOrFunction(): Double {
        val letterStart = pos
        while (peek().isAsciiLetter()) pos++
        val letters = source.substring(letterStart, pos)
        if (peek() == '(') return function(letters)
        val (row, col) = cellAfterLetters(letters)
        return resolve(row, col)
    }

    /** Reads the digits of a cell reference whose letters were already consumed. */
    private fun cellAfterLetters(letters: String): Pair<Int, Int> {
        if (letters.isEmpty()) throw FormulaException(FormulaErrors.SYNTAX)
        val digitStart = pos
        while (peek().isAsciiDigit()) pos++
        if (digitStart == pos) throw FormulaException(FormulaErrors.SYNTAX)
        val rowNumber = source.substring(digitStart, pos).toIntOrNull()
            ?: throw FormulaException(FormulaErrors.REF)
        if (rowNumber < 1 || letters.length > 3) throw FormulaException(FormulaErrors.REF)
        return (rowNumber - 1) to columnIndex(letters)
    }

    private fun function(name: String): Double {
        val upper = name.uppercase()
        if (upper !in FUNCTIONS) throw FormulaException(FormulaErrors.NAME)
        pos++ // '('
        val values = ArrayList<Double>()
        skipWhitespace()
        if (peek() == ')') {
            pos++
        } else {
            while (true) {
                values += argument()
                skipWhitespace()
                if (peek() == ',') {
                    pos++
                    continue
                }
                if (peek() != ')') throw FormulaException(FormulaErrors.SYNTAX)
                pos++
                break
            }
        }
        return when (upper) {
            "SUM" -> values.sum()
            "COUNT" -> values.size.toDouble()
            "MIN" -> values.minOrNull() ?: 0.0
            "MAX" -> values.maxOrNull() ?: 0.0
            else -> {
                if (values.isEmpty()) throw FormulaException(FormulaErrors.DIV_ZERO)
                values.sum() / values.size
            }
        }
    }

    /** One function argument: a range like `A1:B3` (many values) or a single expression. */
    private fun argument(): List<Double> {
        skipWhitespace()
        val start = pos
        if (peek().isAsciiLetter()) {
            while (peek().isAsciiLetter()) pos++
            val firstLetters = source.substring(start, pos)
            if (peek().isAsciiDigit()) {
                val first = cellAfterLetters(firstLetters)
                skipWhitespace()
                if (peek() == ':') {
                    pos++
                    skipWhitespace()
                    val secondStart = pos
                    while (peek().isAsciiLetter()) pos++
                    val second = cellAfterLetters(source.substring(secondStart, pos))
                    return resolveRange(first.first, first.second, second.first, second.second)
                }
            }
        }
        pos = start
        return listOf(expression())
    }

    private companion object {
        val FUNCTIONS = setOf("SUM", "AVERAGE", "MIN", "MAX", "COUNT")
    }

    private fun peek(): Char = if (pos < source.length) source[pos] else '\u0000'

    private fun skipWhitespace() {
        while (peek() == ' ' || peek() == '\t') pos++
    }

    private fun Char.isAsciiDigit() = this in '0'..'9'
    private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
}
