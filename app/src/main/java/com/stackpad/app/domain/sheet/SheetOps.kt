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

    const val MIN_COLUMN_DP = 48
    const val MAX_COLUMN_DP = 240
    const val MIN_ROW_DP = 28
    const val MAX_ROW_DP = 160

    // ---- rows and columns anywhere in the table (formulas are rewritten to keep pointing at the same cells) ----

    /** Adds an empty row so that it becomes row [at] (0 = above the first row, rowCount = below the last). */
    fun SheetBlock.withRowInserted(at: Int): SheetBlock {
        if (rowCount >= MAX_ROWS || at !in 0..rowCount) return this
        val shifted = { r: Int -> if (r >= at) r + 1 else r }
        val rows = cells.toMutableList().apply { add(at, List(columnCount) { "" }) }
        return rebuilt(rows, shifted, { it }, columnWidths, padded(rowHeights, rowCount).toMutableList().apply { add(at, 0) })
    }

    fun SheetBlock.withRowRemoved(at: Int): SheetBlock {
        if (rowCount <= 1 || at !in cells.indices) return this
        val map = { r: Int -> if (r == at) null else if (r > at) r - 1 else r }
        val rows = cells.toMutableList().apply { removeAt(at) }
        return rebuilt(rows, map, { it }, columnWidths, padded(rowHeights, rowCount).toMutableList().apply { removeAt(at) })
    }

    /** Moves row [from] so that it ends up at index [to]. */
    fun SheetBlock.withRowMoved(from: Int, to: Int): SheetBlock {
        if (from !in cells.indices || to !in cells.indices || from == to) return this
        val rows = cells.toMutableList().apply { add(to, removeAt(from)) }
        val heights = padded(rowHeights, rowCount).toMutableList().apply { add(to, removeAt(from)) }
        return rebuilt(rows, { r -> movedIndex(r, from, to) }, { it }, columnWidths, heights)
    }

    fun SheetBlock.withRowCleared(at: Int): SheetBlock =
        if (at !in cells.indices) this else copy(cells = cells.mapIndexed { r, line -> if (r == at) line.map { "" } else line })

    fun SheetBlock.withColumnInserted(at: Int): SheetBlock {
        if (columnCount >= MAX_COLUMNS || at !in 0..columnCount) return this
        val shifted = { c: Int -> if (c >= at) c + 1 else c }
        val rows = cells.map { line -> line.toMutableList().apply { add(at, "") } }
        return rebuilt(rows, { it }, shifted, padded(columnWidths, columnCount).toMutableList().apply { add(at, 0) }, rowHeights)
    }

    fun SheetBlock.withColumnRemoved(at: Int): SheetBlock {
        if (columnCount <= 1 || at !in 0 until columnCount) return this
        val map = { c: Int -> if (c == at) null else if (c > at) c - 1 else c }
        val rows = cells.map { line -> line.toMutableList().apply { removeAt(at) } }
        return rebuilt(rows, { it }, map, padded(columnWidths, columnCount).toMutableList().apply { removeAt(at) }, rowHeights)
    }

    /** Moves column [from] so that it ends up at index [to]. */
    fun SheetBlock.withColumnMoved(from: Int, to: Int): SheetBlock {
        if (from !in 0 until columnCount || to !in 0 until columnCount || from == to) return this
        val rows = cells.map { line -> line.toMutableList().apply { add(to, removeAt(from)) } }
        val widths = padded(columnWidths, columnCount).toMutableList().apply { add(to, removeAt(from)) }
        return rebuilt(rows, { it }, { c -> movedIndex(c, from, to) }, widths, rowHeights)
    }

    fun SheetBlock.withColumnCleared(at: Int): SheetBlock =
        if (at !in 0 until columnCount) this else copy(cells = cells.map { line -> line.mapIndexed { c, v -> if (c == at) "" else v } })

    fun SheetBlock.withColumnWidth(column: Int, dp: Int): SheetBlock {
        if (column !in 0 until columnCount) return this
        return copy(columnWidths = padded(columnWidths, columnCount).mapIndexed { c, w -> if (c == column) dp.coerceIn(MIN_COLUMN_DP, MAX_COLUMN_DP) else w })
    }

    fun SheetBlock.withRowHeight(row: Int, dp: Int): SheetBlock {
        if (row !in cells.indices) return this
        return copy(rowHeights = padded(rowHeights, rowCount).mapIndexed { r, h -> if (r == row) dp.coerceIn(MIN_ROW_DP, MAX_ROW_DP) else h })
    }

    private fun padded(sizes: List<Int>, count: Int): List<Int> = List(count) { sizes.getOrNull(it) ?: 0 }

    private fun movedIndex(index: Int, from: Int, to: Int): Int = when {
        index == from -> to
        from < to && index in (from + 1)..to -> index - 1
        from > to && index in to until from -> index + 1
        else -> index
    }

    private fun SheetBlock.rebuilt(
        rows: List<List<String>>,
        row: (Int) -> Int?,
        col: (Int) -> Int?,
        widths: List<Int>,
        heights: List<Int>,
    ) = copy(
        cells = rows.map { line -> line.map { FormulaRefs.remap(it, row, col) } },
        columnWidths = widths,
        rowHeights = heights,
    )
}
