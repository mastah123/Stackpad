package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextLevel
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream

/**
 * Reads the parts of a Word document that matter in a note: paragraphs, headings (Title,
 * Heading 1-3), bold/italic/underline, bullet/numbered/lettered lists, tables and pictures.
 * The caller reads the zip; this works on the XML parts.
 */
object DocxParser {

    /** Pictures are image blocks whose name is the zip path of the picture, e.g. `word/media/image1.png`. */
    fun parse(document: InputStream, relationships: Map<String, String>, listKinds: Map<String, ListKind>): List<Block> {
        val handler = DocumentHandler(relationships, listKinds)
        Xml.parse(document, handler)
        return handler.assembler.build()
    }

    /** `word/_rels/document.xml.rels`: relationship id to the zip path of its target (pictures). */
    fun parseRelationships(xml: InputStream): Map<String, String> {
        val map = HashMap<String, String>()
        Xml.parse(
            xml,
            object : DefaultHandler() {
                override fun startElement(uri: String?, localName: String?, qName: String, attrs: Attributes) {
                    if (qName != "Relationship") return
                    val id = attrs.getValue("Id") ?: return
                    val target = attrs.getValue("Target") ?: return
                    if (attrs.getValue("TargetMode") == "External") return
                    map[id] = if (target.startsWith("/")) target.trimStart('/') else "word/" + target
                }
            },
        )
        return map
    }

    /** `word/numbering.xml`: list id to the marker style of its first level. */
    fun parseNumbering(xml: InputStream): Map<String, ListKind> {
        val abstractKinds = HashMap<String, ListKind>()
        val numToAbstract = HashMap<String, String>()
        Xml.parse(
            xml,
            object : DefaultHandler() {
                private var abstractId: String? = null
                private var level: String? = null
                private var numId: String? = null

                override fun startElement(uri: String?, localName: String?, qName: String, attrs: Attributes) {
                    when (qName) {
                        "w:abstractNum" -> abstractId = attrs.getValue("w:abstractNumId")
                        "w:lvl" -> level = attrs.getValue("w:ilvl")
                        "w:numFmt" -> {
                            val id = abstractId
                            if (id != null && level == "0" && id !in abstractKinds) {
                                abstractKinds[id] = when (attrs.getValue("w:val")) {
                                    "bullet", "none" -> ListKind.BULLET
                                    "lowerLetter", "upperLetter" -> ListKind.LETTERED
                                    else -> ListKind.NUMBERED
                                }
                            }
                        }
                        "w:num" -> numId = attrs.getValue("w:numId")
                        "w:abstractNumId" -> if (abstractId == null || numId != null) {
                            val n = numId
                            val a = attrs.getValue("w:val")
                            if (n != null && a != null) numToAbstract[n] = a
                        }
                    }
                }

                override fun endElement(uri: String?, localName: String?, qName: String) {
                    when (qName) {
                        "w:abstractNum" -> abstractId = null
                        "w:lvl" -> level = null
                        "w:num" -> numId = null
                    }
                }
            },
        )
        return numToAbstract.mapNotNull { (num, abstract) -> abstractKinds[abstract]?.let { num to it } }.toMap()
    }

    /** `docProps/core.xml`: the document title, or null. */
    fun parseTitle(xml: InputStream): String? {
        var title: String? = null
        Xml.parse(
            xml,
            object : DefaultHandler() {
                private val text = StringBuilder()
                override fun startElement(uri: String?, localName: String?, qName: String, attrs: Attributes) = text.setLength(0)
                override fun characters(ch: CharArray, start: Int, length: Int) { text.append(ch, start, length) }
                override fun endElement(uri: String?, localName: String?, qName: String) {
                    if (qName == "dc:title") title = text.toString().trim().takeIf { it.isNotEmpty() }
                }
            },
        )
        return title
    }

    private class DocumentHandler(
        private val relationships: Map<String, String>,
        private val listKinds: Map<String, ListKind>,
    ) : DefaultHandler() {
        val assembler = BlockAssembler()

        private var paragraph = StyledTextBuilder()
        private var style = ""
        private var numId: String? = null
        private val pictures = ArrayList<String>()

        private var inRun = false
        private var inRunProps = false
        private var inText = false
        private var bold = false
        private var italic = false
        private var underline = false

        private var tableDepth = 0
        private var rows = ArrayList<MutableList<String>>()
        private var cell: StringBuilder? = null

        override fun startElement(uri: String?, localName: String?, qName: String, attrs: Attributes) {
            when (qName) {
                "w:p" -> { paragraph = StyledTextBuilder(); style = ""; numId = null; pictures.clear() }
                "w:pStyle" -> style = attrs.getValue("w:val").orEmpty()
                "w:numId" -> if (!inRun) numId = attrs.getValue("w:val")?.takeIf { it != "0" }
                "w:r" -> { inRun = true; bold = false; italic = false; underline = false }
                "w:rPr" -> if (inRun) inRunProps = true
                "w:b" -> if (inRunProps) bold = isOn(attrs)
                "w:i" -> if (inRunProps) italic = isOn(attrs)
                "w:u" -> if (inRunProps) underline = attrs.getValue("w:val").let { it != null && it != "none" }
                "w:t" -> inText = true
                "w:tab" -> if (inRun) paragraph.append(" ")
                "w:br" -> if (inRun) paragraph.append("\n")
                "a:blip" -> attrs.getValue("r:embed")?.let { relationships[it] }?.let(pictures::add)
                "w:tbl" -> if (tableDepth++ == 0) rows = ArrayList()
                "w:tr" -> if (tableDepth == 1) rows.add(ArrayList())
                "w:tc" -> if (tableDepth == 1) cell = StringBuilder()
            }
        }

        private fun isOn(attrs: Attributes): Boolean = attrs.getValue("w:val").let { it == null || it == "1" || it == "true" || it == "on" }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if (inText) paragraph.append(String(ch, start, length), bold, italic, underline)
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            when (qName) {
                "w:t" -> inText = false
                "w:rPr" -> inRunProps = false
                "w:r" -> inRun = false
                "w:p" -> endParagraph()
                "w:tc" -> if (tableDepth == 1) {
                    rows.lastOrNull()?.add(cell?.toString().orEmpty())
                    cell = null
                }
                "w:tbl" -> if (--tableDepth == 0) assembler.table(rows)
            }
        }

        private fun endParagraph() {
            val styled = paragraph.build()
            val target = cell
            if (target != null) {
                if (styled.text.isNotEmpty()) {
                    if (target.isNotEmpty()) target.append(' ')
                    target.append(styled.text)
                }
                return
            }
            val kind = numId?.let { listKinds[it] ?: ListKind.BULLET }
            val lower = style.lowercase()
            val level = headingLevel(lower)
            when {
                styled.isBlank -> Unit
                level != null -> assembler.text(styled, level)
                kind != null -> assembler.listItem(kind, styled.text)
                else -> assembler.text(styled)
            }
            pictures.forEach { assembler.image(it) }
        }

        private fun headingLevel(style: String): TextLevel? = when {
            style == "title" -> TextLevel.H1
            style == "subtitle" -> TextLevel.H2
            style.startsWith("heading") -> when (style.removePrefix("heading").trim().firstOrNull()) {
                '1' -> TextLevel.H1
                '2' -> TextLevel.H2
                else -> TextLevel.H3
            }
            else -> null
        }
    }
}
