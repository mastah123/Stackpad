package com.stackpad.app.domain

import com.stackpad.app.domain.formula.SheetEvaluator
import com.stackpad.app.domain.formula.columnIndex
import com.stackpad.app.domain.formula.columnLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class SheetEvaluatorTest {

    private fun eval(vararg row: String, col: Int = row.lastIndex): String =
        SheetEvaluator(listOf(row.toList())).display(0, col)

    @Test
    fun plainTextIsReturnedUnchanged() {
        assertEquals("hello", eval("hello"))
        assertEquals("", eval(""))
    }

    @Test
    fun respectsOperatorPrecedence() {
        assertEquals("14", eval("=2+3*4"))
        assertEquals("20", eval("=(2+3)*4"))
        assertEquals("1", eval("=10-3*3"))
    }

    @Test
    fun supportsUnaryMinusAndWhitespace() {
        assertEquals("2", eval("=-3+5"))
        assertEquals("-6", eval("= 2 * -3 "))
        assertEquals("2", eval(" =1+1"))
    }

    @Test
    fun evaluatesCellReferences() {
        assertEquals("5", eval("2", "3", "=A1+B1"))
        assertEquals("10", eval("2", "3", "=(a1+b1)*2"))
    }

    @Test
    fun referencesOtherFormulas() {
        val cells = listOf(listOf("4", "=A1*2"), listOf("=B1+1", "=A2*A1"))
        val evaluator = SheetEvaluator(cells)
        assertEquals("9", evaluator.display(1, 0))
        assertEquals("36", evaluator.display(1, 1))
    }

    @Test
    fun formatsDecimals() {
        assertEquals("0.3333333333", eval("=1/3"))
        assertEquals("0.3", eval("=0.1+0.2"))
        assertEquals("2.5", eval("=5/2"))
    }

    @Test
    fun reportsDivisionByZero() {
        assertEquals("#DIV/0!", eval("=1/0"))
        assertEquals("#DIV/0!", eval("0", "=5/A1"))
    }

    @Test
    fun detectsCircularReferences() {
        val evaluator = SheetEvaluator(listOf(listOf("=B1", "=A1")))
        assertEquals("#CYCLE!", evaluator.display(0, 0))
        assertEquals("#CYCLE!", evaluator.display(0, 1))
        assertEquals("#CYCLE!", eval("=A1"))
    }

    @Test
    fun reportsBadReferencesAndValues() {
        assertEquals("#REF!", eval("=Z9"))
        assertEquals("#REF!", eval("=A0"))
        assertEquals("#VALUE!", eval("abc", "=A1"))
    }

    @Test
    fun reportsSyntaxErrors() {
        assertEquals("#ERR!", eval("=2+"))
        assertEquals("#ERR!", eval("="))
        assertEquals("#ERR!", eval("=(1+2"))
        assertEquals("#ERR!", eval("=1 2"))
    }

    @Test
    fun emptyReferencedCellCountsAsZero() {
        assertEquals("7", eval("", "=A1+7"))
    }

    @Test
    fun columnLabelsFollowSpreadsheetNaming() {
        assertEquals("A", columnLabel(0))
        assertEquals("Z", columnLabel(25))
        assertEquals("AA", columnLabel(26))
        assertEquals("AB", columnLabel(27))
        assertEquals(0, columnIndex("a"))
        assertEquals(26, columnIndex("AA"))
        assertEquals(27, columnIndex("ab"))
    }
}
