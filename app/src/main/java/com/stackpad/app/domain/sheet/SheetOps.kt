package com.stackpad.app.domain.sheet

import com.stackpad.app.data.model.SheetBlock

/** Pure edit operations for [SheetBlock]; each returns a new block (or the same one if a limit is hit). */
object SheetOps {
    const val MAX_ROWS = 50
    const val MAX_COLUMNS = 12

    val SheetBlock.rowCount: Int get() = cells.size
    val SheetBlock.columnCount: Int get() = cells.firstOrNull()?.size ?: 0

    fun SheetBlock.withCell(row: Int, col: Int, text: String): SheetBlock {
        if (row !in cells.indices || col !in cells[row].indices) return this
        return copy(
            cells = cells.mapIndexed { r, line ->
                if (r == row) line.mapIndexed { c, value -> if (c == col) text else value } else line
            },
        )
    }

    fun SheetBlock.withRowAdded(): SheetBlock =
        if (rowCount >= MAX_ROWS) this else copy(cells = cells + listOf(List(columnCount) { "" }))

    fun SheetBlock.withLastRowRemoved(): SheetBlock =
        if (rowCount <= 1) this else copy(cells = cells.dropLast(1))

    fun SheetBlock.withColumnAdded(): SheetBlock =
        if (columnCount >= MAX_COLUMNS) this else copy(cells = cells.map { it + "" })

    fun SheetBlock.withLastColumnRemoved(): SheetBlock =
        if (columnCount <= 1) this else copy(cells = cells.map { it.dropLast(1) })
}
