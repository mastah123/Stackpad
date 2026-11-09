package com.stackpad.app.domain.export

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.data.model.isBlank
import com.stackpad.app.domain.formula.SheetEvaluator
import com.stackpad.app.domain.text.ListMarkers
import com.stackpad.app.util.formatDuration

/** How a note is turned into text for sharing. */
enum class ExportFormat { MARKDOWN, PLAIN }

/**
 * Turns a note into shareable text. Images and audio cannot travel as text, so they appear as
 * short placeholders such as `[Image: caption]`. Table formulas are replaced by their results.
 */
object NoteExporter {

    /** Exports just [blocks], without a title (used to copy a selection). */
    fun exportBlocks(blocks: List<Block>, format: ExportFormat): String =
        export(Note(id = "", title = "", blocks = blocks, pinned = false, createdAt = 0, updatedAt = 0), format)

    fun export(note: Note, format: ExportFormat): String {
        val markdown = format == ExportFormat.MARKDOWN
        val parts = ArrayList<Pair<String, Boolean>>() // text, isPlainTextLine

        if (note.title.isNotBlank()) {
            parts += (if (markdown) "# ${note.title.trim()}" else note.title.trim()) to false
        }
        note.blocks.filterNot { it.isBlank() }.forEach { block ->
            parts += render(block, markdown) to (block is TextBlock && !markdown)
        }

        val out = StringBuilder()
        parts.forEachIndexed { index, (text, isLine) ->
            if (index > 0) out.append(if (isLine && parts[index - 1].second) "\n" else "\n\n")
            out.append(text)
        }
        return out.toString().trim()
    }

    private fun render(block: Block, markdown: Boolean): String = when (block) {
        is TextBlock -> renderText(block, markdown)
        is ListBlock -> renderList(block, markdown)
        is ChecklistBlock -> renderChecklist(block, markdown)
        is ImageBlock -> {
            val label = if (block.caption.isBlank()) "Image" else "Image: ${block.caption.trim()}"
            if (markdown) "*[$label]*" else "[$label]"
        }
        is SheetBlock -> renderSheet(block, markdown)
        is AudioBlock -> "[Audio note ${formatDuration(block.durationMs)}]"
        is LinkBlock -> renderLink(block, markdown)
        is PromptBlock -> if (markdown) fenced(block.text) else block.text.trim()
        is CodeBlock -> if (markdown) fenced(block.text) else block.text.trimEnd()
        is QuoteBlock -> block.text.trim().lines().joinToString("\n") { "> $it" }
        is DividerBlock -> "---"
    }

    private fun renderText(block: TextBlock, markdown: Boolean): String {
        if (!markdown) return block.text
        val prefix = when (block.level) {
            TextLevel.PARAGRAPH -> ""
            TextLevel.H1 -> "# "
            TextLevel.H2 -> "## "
            TextLevel.H3 -> "### "
        }
        return prefix + applySpans(block.text, block.spans)
    }

    private fun renderList(block: ListBlock, markdown: Boolean): String =
        block.items.filter { it.text.isNotBlank() }.mapIndexed { index, item ->
            val marker = when {
                !markdown -> ListMarkers.label(block.kind, index)
                block.kind == ListKind.BULLET -> "-"
                else -> ListMarkers.label(block.kind, index)
            }
            "$marker ${item.text.trim()}"
        }.joinToString("\n")

    private fun renderChecklist(block: ChecklistBlock, markdown: Boolean): String {
        val lines = ArrayList<String>()
        if (block.title.isNotBlank()) lines += if (markdown) "**${block.title.trim()}**" else block.title.trim()
        block.items.filter { it.text.isNotBlank() }.forEach { item ->
            val box = if (item.checked) "[x]" else "[ ]"
            val due = item.dueDate?.let { " (due $it)" }.orEmpty()
            lines += (if (markdown) "- " else "") + "$box ${item.text.trim()}$due"
        }
        return lines.joinToString("\n")
    }

    private fun renderSheet(block: SheetBlock, markdown: Boolean): String {
        val evaluator = SheetEvaluator(block.cells)
        val rows = block.cells.indices.map { r ->
            block.cells[r].indices.map { c -> evaluator.display(r, c).trim() }
        }.filter { row -> row.any { it.isNotEmpty() } }
        if (rows.isEmpty()) return ""
        if (!markdown) return rows.joinToString("\n") { it.joinToString(" | ") }

        val width = rows.maxOf { it.size }
        fun line(row: List<String>) =
            "| " + (0 until width).joinToString(" | ") { row.getOrElse(it) { "" }.replace("|", "\\|") } + " |"
        val separator = "| " + (0 until width).joinToString(" | ") { "---" } + " |"
        return (listOf(line(rows.first()), separator) + rows.drop(1).map(::line)).joinToString("\n")
    }

    private fun renderLink(block: LinkBlock, markdown: Boolean): String {
        val title = block.title.trim()
        val url = block.url.trim()
        return when {
            url.isEmpty() -> title
            title.isEmpty() -> url
            markdown -> "[$title]($url)"
            else -> "$title: $url"
        }
    }

    /** A code fence that is longer than any run of backticks inside the text. */
    private fun fenced(text: String): String {
        val longest = Regex("`+").findAll(text).maxOfOrNull { it.value.length } ?: 0
        val fence = "`".repeat(maxOf(3, longest + 1))
        return "$fence\n${text.trimEnd()}\n$fence"
    }

    /** Wraps styled ranges in Markdown markers (`**`, `*`) and `<u>` for underline. */
    internal fun applySpans(text: String, spans: List<TextSpan>): String {
        if (text.isEmpty() || spans.isEmpty()) return text
        val valid = spans.map { TextSpan(it.start.coerceIn(0, text.length), it.end.coerceIn(0, text.length), it.type) }
            .filter { it.start < it.end }
        if (valid.isEmpty()) return text

        val cuts = (valid.flatMap { listOf(it.start, it.end) } + 0 + text.length).toSortedSet().toList()
        data class Segment(var start: Int, var end: Int, val styles: Set<SpanType>)
        val segments = ArrayList<Segment>()
        for (i in 0 until cuts.size - 1) {
            val start = cuts[i]
            val end = cuts[i + 1]
            val styles = valid.filter { it.start <= start && it.end >= end }.map { it.type }.toSet()
            val last = segments.lastOrNull()
            if (last != null && last.styles == styles) last.end = end else segments += Segment(start, end, styles)
        }

        return segments.joinToString("") { seg ->
            val piece = text.substring(seg.start, seg.end)
            if (seg.styles.isEmpty() || piece.isBlank()) return@joinToString piece
            val lead = piece.takeWhile { it.isWhitespace() }
            val trail = piece.takeLastWhile { it.isWhitespace() }
            var core = piece.trim()
            if (SpanType.ITALIC in seg.styles) core = "*$core*"
            if (SpanType.BOLD in seg.styles) core = "**$core**"
            if (SpanType.UNDERLINE in seg.styles) core = "<u>$core</u>"
            lead + core + trail
        }
    }
}
