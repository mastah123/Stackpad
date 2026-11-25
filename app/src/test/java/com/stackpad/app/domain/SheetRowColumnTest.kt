package com.stackpad.app.domain

import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.domain.sheet.FormulaRefs
import com.stackpad.app.domain.sheet.SheetOps.withColumnCleared
import com.stackpad.app.domain.sheet.SheetOps.withColumnInserted
import com.stackpad.app.domain.sheet.SheetOps.withColumnMoved
import com.stackpad.app.domain.sheet.SheetOps.withColumnRemoved
import com.stackpad.app.domain.sheet.SheetOps.withColumnWidth
import com.stackpad.app.domain.sheet.SheetOps.withRowCleared
import com.stackpad.app.domain.sheet.SheetOps.withRowHeight
import com.stackpad.app.domain.sheet.SheetOps.withRowInserted
import com.stackpad.app.domain.sheet.SheetOps.withRowMoved
import com.stackpad.app.domain.sheet.SheetOps.withRowRemoved
import org.junit.Assert.assertEquals
import org.junit.Test

class SheetRowColumnTest {
    private fun sheet(vararg rows: List<String>) = SheetBlock("s", rows.toList())

    @Test fun insertingARowShiftsReferencesBelowIt() {
        val s = sheet(listOf("1", "2"), listOf("3", "=A1+A2"))
        val out = s.withRowInserted(1)
        assertEquals(listOf("", ""), out.cells[1])
        assertEquals("=A1+A3", out.cells[2][1])
        assertEquals(3, out.cells.size)
    }

    @Test fun insertingBelowTheLastRowAddsAtTheEnd() {
        val s = sheet(listOf("a"), listOf("b"))
        assertEquals(listOf("a", "b", ""), s.withRowInserted(2).cells.map { it[0] })
    }

    @Test fun removingARowTurnsReferencesToItIntoRefErrors() {
        val s = sheet(listOf("1", "=A2"), listOf("2", "=A1"), listOf("3", "=A3"))
        val out = s.withRowRemoved(1)
        assertEquals("=#REF!", out.cells[0][1])
        assertEquals("=A2", out.cells[1][1])
        assertEquals(2, out.cells.size)
        assertEquals(s, sheet(listOf("x")).let { s }.withRowRemoved(9))
        assertEquals(1, sheet(listOf("x")).withRowRemoved(0).cells.size)
    }

    @Test fun movingARowKeepsFormulasPointingAtTheSameCells() {
        val s = sheet(listOf("a"), listOf("b"), listOf("=A1"))
        val out = s.withRowMoved(0, 2)
        assertEquals(listOf("b", "=A3", "a"), out.cells.map { it[0] })
    }

    @Test fun columnsInsertRemoveAndMove() {
        val s = sheet(listOf("1", "2", "=A1+B1"))
        assertEquals(listOf("1", "", "2", "=A1+C1"), s.withColumnInserted(1).cells[0])
        assertEquals(listOf("2", "=#REF!+A1"), s.withColumnRemoved(0).cells[0].let { listOf(it[0], it[1]) })
        assertEquals(listOf("2", "1", "=B1+A1"), s.withColumnMoved(0, 1).cells[0])
    }

    @Test fun clearingEmptiesOnlyThatLine() {
        val s = sheet(listOf("1", "2"), listOf("3", "4"))
        assertEquals(listOf(listOf("", ""), listOf("3", "4")), s.withRowCleared(0).cells)
        assertEquals(listOf(listOf("1", ""), listOf("3", "")), s.withColumnCleared(1).cells)
    }

    @Test fun sizesFollowTheirRowsAndColumnsAndStayInRange() {
        val s = sheet(listOf("a", "b"), listOf("c", "d")).withColumnWidth(1, 120).withRowHeight(0, 50)
        assertEquals(listOf(0, 120), s.columnWidths)
        assertEquals(listOf(50, 0), s.rowHeights)
        assertEquals(listOf(120, 0), s.withColumnMoved(1, 0).columnWidths)
        assertEquals(listOf(0, 50, 0), s.withRowInserted(0).rowHeights)
        assertEquals(240, s.withColumnWidth(0, 999).columnWidths[0])
        assertEquals(48, s.withColumnWidth(0, 5).columnWidths[0])
        assertEquals(160, s.withRowHeight(1, 999).rowHeights[1])
    }

    @Test fun refsLeaveFunctionsAndPlainTextAlone() {
        val same = { i: Int -> i + 1 }
        assertEquals("=SUM(B2:C3)+LOG10(4)", FormulaRefs.remap("=SUM(A1:B2)+LOG10(4)", same, same))
        assertEquals("A1 is text", FormulaRefs.remap("A1 is text", same, same))
        assertEquals("=b2", FormulaRefs.remap("=a1", same, same))
    }
}
