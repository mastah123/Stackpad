package com.stackpad.app.domain

import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.domain.importing.DocxParser
import com.stackpad.app.domain.importing.PdfTextStructurer
import com.stackpad.app.domain.importing.StyledTextBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocxAndPdfImportTest {
    private fun doc(body: String) = ("""<w:document xmlns:w="w" xmlns:a="a" xmlns:r="r"><w:body>$body</w:body></w:document>""").byteInputStream()

    @Test fun docx_headings_runs_and_lists() {
        val body = """<w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>Title</w:t></w:r></w:p>
<w:p><w:r><w:t xml:space="preserve">plain </w:t></w:r><w:r><w:rPr><w:b/></w:rPr><w:t>bold</w:t></w:r></w:p>
<w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="2"/></w:numPr></w:pPr><w:r><w:t>one</w:t></w:r></w:p>
<w:p><w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="2"/></w:numPr></w:pPr><w:r><w:t>two</w:t></w:r></w:p>"""
        val blocks = DocxParser.parse(doc(body), emptyMap(), mapOf("2" to ListKind.NUMBERED))
        assertEquals(TextLevel.H1, (blocks[0] as TextBlock).level)
        val p = blocks[1] as TextBlock
        assertEquals("plain bold", p.text)
        assertTrue(p.spans.any { it.type == SpanType.BOLD && p.text.substring(it.start, it.end) == "bold" })
        val list = blocks[2] as ListBlock
        assertEquals(ListKind.NUMBERED, list.kind)
        assertEquals(2, list.items.size)
    }

    @Test fun docx_table_and_picture() {
        val body = """<w:tbl><w:tr><w:tc><w:p><w:r><w:t>A</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>B</w:t></w:r></w:p></w:tc></w:tr></w:tbl>
<w:p><w:r><w:drawing><a:blip r:embed="rId5"/></w:drawing></w:r></w:p>"""
        val blocks = DocxParser.parse(doc(body), mapOf("rId5" to "word/media/image1.png"), emptyMap())
        assertEquals(listOf(listOf("A", "B")), (blocks[0] as SheetBlock).cells)
        assertEquals("word/media/image1.png", (blocks[1] as ImageBlock).fileName)
    }

    @Test fun docx_relationships_numbering_and_title() {
        val rels = """<Relationships><Relationship Id="rId5" Type="x" Target="media/image1.png"/><Relationship Id="rId6" Type="x" Target="http://a" TargetMode="External"/></Relationships>"""
        assertEquals(mapOf("rId5" to "word/media/image1.png"), DocxParser.parseRelationships(rels.byteInputStream()))
        val numbering = """<w:numbering xmlns:w="w"><w:abstractNum w:abstractNumId="0"><w:lvl w:ilvl="0"><w:numFmt w:val="bullet"/></w:lvl></w:abstractNum><w:abstractNum w:abstractNumId="1"><w:lvl w:ilvl="0"><w:numFmt w:val="decimal"/></w:lvl></w:abstractNum><w:num w:numId="1"><w:abstractNumId w:val="1"/></w:num><w:num w:numId="2"><w:abstractNumId w:val="0"/></w:num></w:numbering>"""
        assertEquals(mapOf("1" to ListKind.NUMBERED, "2" to ListKind.BULLET), DocxParser.parseNumbering(numbering.byteInputStream()))
        assertEquals("Doc", DocxParser.parseTitle("""<cp:coreProperties xmlns:dc="d" xmlns:cp="c"><dc:title> Doc </dc:title></cp:coreProperties>""".byteInputStream()))
    }

    @Test fun styled_builder_merges_and_trims() {
        val b = StyledTextBuilder()
        b.append("  hi ", bold = true)
        b.append("there  ", bold = true)
        val s = b.build()
        assertEquals("hi there", s.text)
        assertEquals(1, s.spans.size)
        assertEquals(0, s.spans[0].start)
        assertEquals(8, s.spans[0].end)
    }

    @Test fun pdf_lines_join_into_paragraphs_and_lists() {
        val page = "INTRODUCTION\nThis is a long line of text that goes on and on and on\nand wraps with a hyphen-\nated word here.\n\n• first\n• second\n1. one\n2. two\n3"
        val blocks = PdfTextStructurer.blocks(listOf(page))
        val head = blocks[0] as TextBlock
        assertEquals(TextLevel.H2, head.level)
        val para = blocks[1] as TextBlock
        assertTrue(para.text.contains("hyphenated word here."))
        assertEquals(ListKind.BULLET, (blocks[2] as ListBlock).kind)
        assertEquals(ListKind.NUMBERED, (blocks[3] as ListBlock).kind)
        assertEquals(4, blocks.size)
    }

    @Test fun pdf_repeated_headers_are_dropped_and_scans_detected() {
        val pages = (1..4).map { "Company Report\nBody text number $it is here and long enough\n$it" }
        val text = PdfTextStructurer.blocks(pages).joinToString(" ") { (it as TextBlock).text }
        assertFalse(text.contains("Company Report"))
        assertTrue(PdfTextStructurer.hasText(pages))
        assertFalse(PdfTextStructurer.hasText(listOf("", " \n ", "1")))
    }
}
