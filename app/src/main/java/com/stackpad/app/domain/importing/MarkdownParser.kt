package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextLevel

/**
 * Turns Markdown (also Obsidian and Notion exports) into note blocks: headings, bold/italic,
 * bullet, numbered and task lists, quotes, code, tables, rules, links and images. Anything it does
 * not know is kept as plain text.
 */
object MarkdownParser {

    /** @property title from front matter or the first top-level heading, or null. */
    data class Result(val title: String?, val tags: List<String>, val blocks: List<Block>)

    private val heading = Regex("^(#{1,6})\\s+(.*?)\\s*#*\\s*$")
    private val rule = Regex("^\\s*([-*_])(\\s*\\1){2,}\\s*$")
    private val listItem = Regex("^(\\s*)([-*+]|\\d+[.)])\\s+(.*)$")
    private val taskItem = Regex("^\\[([ xX])]\\s+(.*)$")
    private val imageLine = Regex("^!\\[(.*?)]\\((.+?)\\)\\s*$")
    private val obsidianEmbed = Regex("^!\\[\\[(.+?)(\\|.*)?]]\\s*$")
    private val tableSeparator = Regex("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?\\s*$")
    private val fence = Regex("^\\s*(```|~~~)")

    fun parse(markdown: String): Result {
        val lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        val (front, start) = FrontMatter.read(lines)
        val assembler = BlockAssembler()
        var title = front.title
        val paragraph = ArrayList<String>()
        var lastItemIsTask = false
        var hasItem = false

        fun flushParagraph() {
            if (paragraph.isEmpty()) return
            assembler.text(inline(paragraph.joinToString("\n")))
            paragraph.clear()
        }

        var i = start
        while (i < lines.size) {
            val line = lines[i]
            when {
                fence.containsMatchIn(line) -> {
                    flushParagraph()
                    val marker = fence.find(line)!!.groupValues[1]
                    val code = ArrayList<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith(marker)) code += lines[i++]
                    assembler.code(code.joinToString("\n"))
                }
                line.isBlank() -> {
                    flushParagraph()
                    hasItem = false
                }
                heading.matches(line) -> {
                    flushParagraph()
                    val match = heading.find(line)!!
                    val level = match.groupValues[1].length
                    val text = inline(match.groupValues[2])
                    if (title == null && level == 1 && assembler.isEmpty) {
                        title = text.text
                    } else {
                        assembler.text(text, if (level == 1) TextLevel.H1 else if (level == 2) TextLevel.H2 else TextLevel.H3)
                    }
                    hasItem = false
                }
                rule.matches(line) -> {
                    flushParagraph()
                    assembler.divider()
                    hasItem = false
                }
                isTableStart(lines, i) -> {
                    flushParagraph()
                    val rows = ArrayList<List<String>>()
                    rows += cells(line)
                    i += 2 // header and separator
                    while (i < lines.size && lines[i].contains('|') && lines[i].isNotBlank()) rows += cells(lines[i++])
                    i--
                    assembler.table(rows)
                    hasItem = false
                }
                line.trimStart().startsWith(">") -> {
                    flushParagraph()
                    val quote = ArrayList<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                        quote += lines[i].trimStart().removePrefix(">").removePrefix(" ")
                        i++
                    }
                    i--
                    assembler.quote(inline(quote.joinToString("\n")).text)
                    hasItem = false
                }
                imageLine.matches(line) -> {
                    flushParagraph()
                    val (alt, target) = imageLine.find(line)!!.destructured
                    image(assembler, alt, target)
                }
                obsidianEmbed.matches(line) -> {
                    flushParagraph()
                    assembler.image(obsidianEmbed.find(line)!!.groupValues[1].trim())
                }
                listItem.matches(line) -> {
                    flushParagraph()
                    val (_, marker, rest) = listItem.find(line)!!.destructured
                    val task = taskItem.find(rest)
                    if (task != null) {
                        assembler.task(inline(task.groupValues[2]).text, task.groupValues[1] != " ")
                        lastItemIsTask = true
                    } else {
                        val kind = if (marker.first().isDigit()) ListKind.NUMBERED else ListKind.BULLET
                        assembler.listItem(kind, inline(rest).text)
                        lastItemIsTask = false
                    }
                    hasItem = true
                }
                hasItem && line.startsWith(" ") -> {
                    // A wrapped line of the previous list item: keep its text by adding it as a new item line.
                    val extra = inline(line.trim()).text
                    if (lastItemIsTask) assembler.task(extra, false) else assembler.listItem(ListKind.BULLET, extra)
                }
                else -> {
                    hasItem = false
                    paragraph += line.trimEnd()
                }
            }
            i++
        }
        flushParagraph()
        return Result(title?.takeIf { it.isNotBlank() }, front.tags, assembler.build())
    }

    private fun image(assembler: BlockAssembler, alt: String, target: String) {
        val path = target.trim().substringBefore(" \"").trim('<', '>')
        if (path.startsWith("http://") || path.startsWith("https://")) {
            assembler.link(alt.ifBlank { path }, path)
        } else {
            assembler.image(percentDecode(path))
        }
    }

    private fun percentDecode(path: String): String = runCatching { java.net.URLDecoder.decode(path.replace("+", "%2B"), "UTF-8") }.getOrDefault(path)

    private fun isTableStart(lines: List<String>, index: Int): Boolean =
        lines[index].contains('|') && index + 1 < lines.size && tableSeparator.matches(lines[index + 1]) && lines[index + 1].contains('-')

    private fun cells(row: String): List<String> {
        val trimmed = row.trim().removePrefix("|").removeSuffix("|")
        val out = ArrayList<String>()
        val cell = StringBuilder()
        var i = 0
        while (i < trimmed.length) {
            val c = trimmed[i]
            if (c == '\\' && i + 1 < trimmed.length && trimmed[i + 1] == '|') {
                cell.append('|'); i += 2; continue
            }
            if (c == '|') { out += inline(cell.toString()).text; cell.clear() } else cell.append(c)
            i++
        }
        out += inline(cell.toString()).text
        return out
    }

    /** Inline Markdown to styled text: bold, italic, underline; links become "text (url)". */
    fun inline(source: String): StyledText {
        val out = StyledTextBuilder()
        scan(source, out, bold = false, italic = false, underline = false)
        return out.build()
    }

    private const val ESCAPABLE = "\\`*_{}[]()#+-.!|~<>"

    private fun scan(src: String, out: StyledTextBuilder, bold: Boolean, italic: Boolean, underline: Boolean) {
        val plain = StringBuilder()
        fun flush() {
            if (plain.isNotEmpty()) { out.append(plain.toString(), bold, italic, underline); plain.clear() }
        }
        var i = 0
        while (i < src.length) {
            val c = src[i]
            when {
                c == '\\' && i + 1 < src.length && src[i + 1] in ESCAPABLE -> { plain.append(src[i + 1]); i += 2 }
                src.startsWith("**", i) || src.startsWith("__", i) -> {
                    val delimiter = src.substring(i, i + 2)
                    val end = src.indexOf(delimiter, i + 2)
                    if (end > i + 2) { flush(); scan(src.substring(i + 2, end), out, true, italic, underline); i = end + 2 }
                    else { plain.append(delimiter); i += 2 }
                }
                src.startsWith("~~", i) -> {
                    val end = src.indexOf("~~", i + 2)
                    if (end > i + 2) { flush(); scan(src.substring(i + 2, end), out, bold, italic, underline); i = end + 2 }
                    else { plain.append("~~"); i += 2 }
                }
                (c == '*' || (c == '_' && (i == 0 || !src[i - 1].isLetterOrDigit()))) &&
                    i + 1 < src.length && !src[i + 1].isWhitespace() -> {
                    val end = closer(src, c, i + 1)
                    if (end > i + 1) { flush(); scan(src.substring(i + 1, end), out, bold, true, underline); i = end + 1 }
                    else { plain.append(c); i++ }
                }
                c == '`' -> {
                    val end = src.indexOf('`', i + 1)
                    if (end > i) { plain.append(src, i + 1, end); i = end + 1 } else { plain.append(c); i++ }
                }
                src.startsWith("<u>", i) -> {
                    val end = src.indexOf("</u>", i + 3)
                    if (end > 0) { flush(); scan(src.substring(i + 3, end), out, bold, italic, true); i = end + 4 }
                    else { plain.append("<u>"); i += 3 }
                }
                src.startsWith("![", i) || c == '[' -> i = link(src, i, plain)
                else -> { plain.append(c); i++ }
            }
        }
        flush()
    }

    /** Index of the closing single [delimiter] that is not part of a double one, or -1. */
    private fun closer(src: String, delimiter: Char, from: Int): Int {
        var j = from
        while (j < src.length) {
            if (src[j] == delimiter && !src[j - 1].isWhitespace() && (j + 1 >= src.length || src[j + 1] != delimiter)) return j
            j++
        }
        return -1
    }

    /** Handles `[text](url)`, `![alt](url)` and `[[wiki|alias]]` starting at [i]; returns the next index. */
    private fun link(src: String, i: Int, plain: StringBuilder): Int {
        val image = src[i] == '!'
        val open = if (image) i + 1 else i
        if (src.startsWith("[[", open)) {
            val end = src.indexOf("]]", open + 2)
            if (end > 0) {
                val inner = src.substring(open + 2, end)
                plain.append(inner.substringAfter('|').substringBefore('#'))
                return end + 2
            }
        }
        val close = src.indexOf(']', open + 1)
        if (close > 0 && close + 1 < src.length && src[close + 1] == '(') {
            val end = src.indexOf(')', close + 2)
            if (end > 0) {
                val label = src.substring(open + 1, close)
                val url = src.substring(close + 2, end).trim().substringBefore(" \"")
                plain.append(
                    when {
                        image -> label
                        label.isBlank() || label == url -> url
                        url.startsWith("http://") || url.startsWith("https://") || url.startsWith("mailto:") -> "$label ($url)"
                        else -> label
                    },
                )
                return end + 1
            }
        }
        plain.append(src[i])
        return i + 1
    }
}

/** Front matter (`---` block at the top, as written by Obsidian and Jekyll): only `title` and `tags` are used. */
internal object FrontMatter {
    data class Data(val title: String?, val tags: List<String>)

    /** Returns the front matter and the index of the first body line. */
    fun read(lines: List<String>): Pair<Data, Int> {
        if (lines.firstOrNull()?.trim() != "---") return Data(null, emptyList()) to 0
        val end = (1 until lines.size).firstOrNull { lines[it].trim() == "---" || lines[it].trim() == "..." }
            ?: return Data(null, emptyList()) to 0
        var title: String? = null
        val tags = ArrayList<String>()
        var inTags = false
        for (line in lines.subList(1, end)) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("title:") -> { title = unquote(trimmed.removePrefix("title:")); inTags = false }
                trimmed.startsWith("tags:") -> {
                    val rest = trimmed.removePrefix("tags:").trim()
                    inTags = rest.isEmpty()
                    if (rest.isNotEmpty()) tags += rest.trim('[', ']').split(',', ' ').map(::unquote).filter { it.isNotBlank() }
                }
                inTags && trimmed.startsWith("-") -> tags += unquote(trimmed.removePrefix("-"))
                trimmed.isNotEmpty() && !trimmed.startsWith("-") -> inTags = false
            }
        }
        return Data(title?.takeIf { it.isNotBlank() }, tags.filter { it.isNotBlank() }) to end + 1
    }

    private fun unquote(raw: String): String = raw.trim().trim('"', '\'').trim()
}
