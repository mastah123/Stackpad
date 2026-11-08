package com.stackpad.app.domain

import com.stackpad.app.domain.formula.SheetEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class SheetFunctionsTest {
    // Column A holds 1..4, B holds 10, 20, text, blank. C1.. is where formulas go.
    private fun sheet(formula: String): SheetEvaluator = SheetEvaluator(
        listOf(
            listOf("1", "10", formula),
            listOf("2", "20", ""),
            listOf("3", "note", ""),
            listOf("4", "", ""),
        ),
    )

    private fun eval(formula: String) = sheet(formula).display(0, 2)

    @Test
    fun sumOverRange() {
        assertEquals("10", eval("=SUM(A1:A4)"))
        assertEquals("30", eval("=SUM(B1:B4)")) // text and blank cells are skipped
    }

    @Test
    fun sumAcceptsTwoDimensionalRange() {
        assertEquals("40", eval("=SUM(A1:B4)"))
    }

    @Test
    fun rangeCornersMayBeReversed() {
        assertEquals("10", eval("=SUM(A4:A1)"))
    }

    @Test
    fun averageMinMaxCount() {
        assertEquals("2.5", eval("=AVERAGE(A1:A4)"))
        assertEquals("1", eval("=MIN(A1:A4)"))
        assertEquals("4", eval("=MAX(A1:A4)"))
        assertEquals("4", eval("=COUNT(A1:A4)"))
        assertEquals("2", eval("=COUNT(B1:B4)"))
    }

    @Test
    fun functionNamesAreCaseInsensitive() {
        assertEquals("10", eval("=sum(a1:a4)"))
        assertEquals("4", eval("= Max( A1 : A4 ) "))
    }

    @Test
    fun mixesRangesCellsAndExpressions() {
        assertEquals("30", eval("=SUM(A1:A4, 5, B1/2+10)"))
        assertEquals("11", eval("=SUM(A1, B1)"))
    }

    @Test
    fun functionsInsideArithmeticAndNested() {
        assertEquals("21", eval("=SUM(A1:A4)*2+1"))
        assertEquals("14", eval("=SUM(MAX(A1:A4), MIN(B1:B2))")) // 4 + 10
        assertEquals("20", eval("=(SUM(A1:A4))*2"))
    }

    @Test
    fun emptyCases() {
        assertEquals("0", eval("=SUM()"))
        assertEquals("0", eval("=SUM(B4:B4)"))
        assertEquals("0", eval("=MAX(B3:B4)"))
        assertEquals("#DIV/0!", eval("=AVERAGE(B3:B4)"))
        assertEquals("#DIV/0!", eval("=AVERAGE()"))
    }

    @Test
    fun rangeOverFormulaCells() {
        val cells = listOf(listOf("2", "=A1*2", "=SUM(A1:B1)"))
        assertEquals("6", SheetEvaluator(cells).display(0, 2))
    }

    @Test
    fun errorsInsideRangeSpread() {
        val cells = listOf(listOf("=1/0", "5", "=SUM(A1:B1)"))
        assertEquals("#DIV/0!", SheetEvaluator(cells).display(0, 2))
    }

    @Test
    fun detectsCycleThroughRange() {
        val cells = listOf(listOf("1", "=SUM(A1:B1)"))
        assertEquals("#CYCLE!", SheetEvaluator(cells).display(0, 1))
    }

    @Test
    fun outOfBoundsRangeIsRefError() {
        assertEquals("#REF!", eval("=SUM(A1:A9)"))
        assertEquals("#REF!", eval("=SUM(A5:A6)"))
    }

    @Test
    fun reportsBadFunctionUse() {
        assertEquals("#NAME?", eval("=FOO(A1)"))
        assertEquals("#ERR!", eval("=SUM(A1:A4"))
        assertEquals("#ERR!", eval("=SUM(A1:)"))
        assertEquals("#ERR!", eval("=SUM(A1:5)"))
        assertEquals("#ERR!", eval("=A1:A4"))
        assertEquals("#ERR!", eval("=SUM(1,)"))
    }
}
