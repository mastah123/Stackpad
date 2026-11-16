package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextLevel
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/** Converts Evernote's note format (ENML, a kind of XHTML) into blocks. Pictures become image blocks named `res:<hash>`. */
object EnmlConverter {

    const val RESOURCE_PREFIX = "res:"

    fun convert(enml: String): List<Block> {
        val cleaned = enml
            .replace(Regex("<\\?xml[^>]*\\?>"), "")
            .replace(Regex("<!DOCTYPE[^>]*>"), "")
            .replace("&nbsp;", "&#160;")
        val handler = Handler()
        return try {
            Xml.parse(cleaned, handler)
            handler.finish()
        } catch (e: org.xml.sax.SAXException) {
            // A broken note still keeps what was read before the error.
            handler.finish()
        }
    }

    private class Handler : DefaultHandler() {
        private val assembler = BlockAssembler()
        private var line = StyledTextBuilder()
        private var bold = 0
        private var italic = 0
        private var underline = 0
        private var heading: TextLevel = TextLevel.PARAGRAPH
        private val lists = ArrayList<ListKind>()
        private var quoteDepth = 0
        private var pre = false
        private var task: Boolean? = null
        private var href: String? = null
        private var linkStart = 0

        private var table: ArrayList<MutableList<String>>? = null
        private var cell: StringBuilder? = null

        fun finish(): List<Block> {
            flush()
            return assembler.build()
        }

        private fun flush() {
            val styled = line.build()
            line = StyledTextBuilder()
            val pendingTask = task
            task = null
            val cellText = cell
            if (cellText != null) {
                if (styled.text.isNotEmpty()) {
                    if (cellText.isNotEmpty()) cellText.append(' ')
                    cellText.append(styled.text)
                }
                return
            }
            if (styled.isBlank) return
            when {
                pendingTask != null -> assembler.task(styled.text, pendingTask)
                pre -> assembler.code(styled.text)
                lists.isNotEmpty() -> assembler.listItem(lists.last(), styled.text)
                quoteDepth > 0 -> assembler.quote(styled.text)
                else -> assembler.text(styled, heading)
            }
        }

        override fun startElement(uri: String?, localName: String?, qName: String, attrs: Attributes) {
            when (qName.lowercase()) {
                "div", "p", "br", "li", "section" -> flush()
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    flush()
                    heading = when (qName[1]) { '1' -> TextLevel.H1; '2' -> TextLevel.H2; else -> TextLevel.H3 }
                }
                "ul" -> { flush(); lists += ListKind.BULLET }
                "ol" -> { flush(); lists += ListKind.NUMBERED }
                "blockquote" -> { flush(); quoteDepth++ }
                "pre" -> { flush(); pre = true }
                "b", "strong" -> bold++
                "i", "em" -> italic++
                "u" -> underline++
                "a" -> { href = attrs.getValue("href"); linkStart = line.length }
                "en-todo" -> { flush(); task = attrs.getValue("checked").equals("true", ignoreCase = true) }
                "en-media" -> {
                    flush()
                    val hash = attrs.getValue("hash")
                    if (hash != null && attrs.getValue("type").orEmpty().startsWith("image/")) {
                        assembler.image(RESOURCE_PREFIX + hash.lowercase())
                    }
                }
                "table" -> { flush(); table = ArrayList() }
                "tr" -> table?.add(ArrayList())
                "td", "th" -> cell = StringBuilder()
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            when (qName.lowercase()) {
                "div", "p", "li", "section" -> flush()
                "h1", "h2", "h3", "h4", "h5", "h6" -> { flush(); heading = TextLevel.PARAGRAPH }
                "ul", "ol" -> { flush(); if (lists.isNotEmpty()) lists.removeAt(lists.lastIndex) }
                "blockquote" -> { flush(); if (quoteDepth > 0) quoteDepth-- }
                "pre" -> { flush(); pre = false }
                "b", "strong" -> bold = (bold - 1).coerceAtLeast(0)
                "i", "em" -> italic = (italic - 1).coerceAtLeast(0)
                "u" -> underline = (underline - 1).coerceAtLeast(0)
                "a" -> {
                    val target = href
                    href = null
                    val shown = line.build().text
                    if (target != null && target.startsWith("http") && !shown.endsWith(target) && !shown.endsWith(")")) {
                        line.append(" ($target)")
                    }
                }
                "td", "th" -> {
                    flush()
                    table?.lastOrNull()?.add(cell?.toString().orEmpty())
                    cell = null
                }
                "table" -> {
                    flush()
                    table?.let { assembler.table(it) }
                    table = null
                }
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            val raw = String(ch, start, length)
            val piece = if (pre) raw else raw.replace(Regex("\\s+"), " ")
            if (piece.isEmpty()) return
            line.append(piece, bold > 0, italic > 0, underline > 0)
        }
    }
}
