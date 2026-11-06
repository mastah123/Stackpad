package com.stackpad.app.domain

import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.domain.sheet.SheetOps
import com.stackpad.app.domain.sheet.SheetOps.columnCount
import com.stackpad.app.domain.sheet.SheetOps.rowCount
import com.stackpad.app.domain.sheet.SheetOps.withCell
import com.stackpad.app.domain.sheet.SheetOps.withColumnAdded
import com.stackpad.app.domain.sheet.SheetOps.withLastColumnRemoved
import com.stackpad.app.domain.sheet.SheetOps.withLastRowRemoved
import com.stackpad.app.domain.sheet.SheetOps.withRowAdded
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SheetOpsTest {
    private val sheet = BlockFactory.sheet()

    @Test
    fun defaultSheetHasExpectedSize() {
        assertEquals(BlockFactory.DEFAULT_SHEET_ROWS, sheet.rowCount)
        assertEquals(BlockFactory.DEFAULT_SHEET_COLUMNS, sheet.columnCount)
    }

    @Test
    fun editsOneCellOnly() {
        val edited = sheet.withCell(1, 2, "x")
        assertEquals("x", edited.cells[1][2])
        assertEquals("", edited.cells[0][0])
    }

    @Test
    fun outOfRangeEditIsIgnored() {
        assertSame(sheet, sheet.withCell(99, 0, "x"))
        assertSame(sheet, sheet.withCell(0, 99, "x"))
    }

    @Test
    fun addsAndRemovesRowsAndColumns() {
        assertEquals(sheet.rowCount + 1, sheet.withRowAdded().rowCount)
        assertEquals(sheet.columnCount + 1, sheet.withColumnAdded().columnCount)
        assertEquals(sheet.rowCount - 1, sheet.withLastRowRemoved().rowCount)
        assertEquals(sheet.columnCount - 1, sheet.withLastColumnRemoved().columnCount)
        assertEquals(sheet.columnCount + 1, sheet.withColumnAdded().cells.last().size)
    }

    @Test
    fun respectsMinimumAndMaximumSize() {
        var small = sheet
        repeat(20) { small = small.withLastRowRemoved().withLastColumnRemoved() }
        assertEquals(1, small.rowCount)
        assertEquals(1, small.columnCount)

        var big = sheet
        repeat(100) { big = big.withRowAdded().withColumnAdded() }
        assertEquals(SheetOps.MAX_ROWS, big.rowCount)
        assertEquals(SheetOps.MAX_COLUMNS, big.columnCount)
    }
}
