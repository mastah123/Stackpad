package com.stackpad.app.domain

import com.stackpad.app.domain.sheet.CsvCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvCodecTest {
    @Test fun encodesPlainAndQuotedFields() {
        val csv = CsvCodec.encode(listOf(listOf("a", "b,c"), listOf("say \"hi\"", "line1\nline2")))
        assertEquals("a,\"b,c\"\r\n\"say \"\"hi\"\"\",\"line1\nline2\"", csv)
    }

    @Test fun roundTripKeepsEveryCell() {
        val cells = listOf(listOf("x", "a,b", ""), listOf("q\"q", "two\nlines", "=1+2"))
        assertEquals(cells, CsvCodec.parse(CsvCodec.encode(cells))!!.cells)
    }

    @Test fun parsesSemicolonAndTabFiles() {
        assertEquals(listOf(listOf("a", "b"), listOf("1", "2")), CsvCodec.parse("a;b\n1;2")!!.cells)
        assertEquals(listOf(listOf("a", "b"), listOf("1", "2")), CsvCodec.parse("a\tb\r\n1\t2\r\n")!!.cells)
    }

    @Test fun ignoresDelimitersInsideQuotesWhenDetecting() {
        assertEquals(listOf(listOf("a;b", "c")), CsvCodec.parse("\"a;b\",c")!!.cells)
    }

    @Test fun padsShortRowsAndStripsBomAndTrailingBlankLines() {
        val parsed = CsvCodec.parse("\uFEFFa,b,c\n1\n\n\n")!!
        assertEquals(listOf(listOf("a", "b", "c"), listOf("1", "", "")), parsed.cells)
        assertFalse(parsed.truncated)
    }

    @Test fun limitsRowsAndColumnsAndReportsIt() {
        val wide = (1..20).joinToString(",") { "c$it" }
        val parsed = CsvCodec.parse(List(60) { wide }.joinToString("\n"))!!
        assertEquals(50, parsed.cells.size)
        assertEquals(12, parsed.cells[0].size)
        assertTrue(parsed.truncated)
    }

    @Test fun emptyTextHasNoTable() {
        assertNull(CsvCodec.parse(""))
        assertNull(CsvCodec.parse("\n\n"))
    }

    @Test fun lastFieldWithoutNewlineIsKept() {
        assertEquals(listOf(listOf("a", "b")), CsvCodec.parse("a,b")!!.cells)
    }
}
