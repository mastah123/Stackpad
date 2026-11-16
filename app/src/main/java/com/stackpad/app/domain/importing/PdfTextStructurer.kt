package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextLevel

/**
 * Makes readable blocks out of the plain text a PDF library extracts: lines that belong to one
 * paragraph are joined (also across words split with a hyphen), bullet and numbered lines become
 * lists, short capitalised lines become headings, and page numbers and repeated page headers or
 * footers are dropped. PDFs do not store structure, so this is a best effort.
 */
object PdfTextStructurer {

    private val bullet = Regex("^[•●▪◦‣·–-]\\s+(.+)$")
    private val numbered = Regex("^\\d{1,3}[.)]\\s+(.+)$")
    private val sectionNumber = Regex("^\\d+(\\.\\d+)*\\.?\\s+\\p{Lu}.*")
    private val pageNumber = Regex("^(page\\s+)?\\d{1,4}(\\s*(/|of)\\s*\\d{1,4})?$", RegexOption.IGNORE_CASE)
    private val sentenceEnd = Regex("[.!?:\"”)]$")

    /** True when the extracted text has enough real words to be worth importing as text (scans give none). */
    fun hasText(pages: List<String>): Boolean = pages.sumOf { page -> page.count { it.isLetterOrDigit() } } >= MIN_CHARACTERS

    fun blocks(pages: List<String>): List<Block> {
        val perPage = pages.map { page -> page.replace("\u000c", "\n").replace("\r", "").split('\n').map { it.trim() } }
        val repeated = repeatedLines(perPage)
        val lines = ArrayList<String>()
        perPage.forEach { page ->
            page.filterNot { it in repeated || pageNumber.matches(it) }.forEach { lines += it }
            lines += ""
        }

        val longest = lines.maxOfOrNull { it.length } ?: 0
        val assembler = BlockAssembler()
        val paragraph = ArrayList<String>()

        fun flush() {
            if (paragraph.isEmpty()) return
            val joined = join(paragraph)
            assembler.text(joined)
            paragraph.clear()
        }

        for (line in lines) {
            when {
                line.isEmpty() -> flush()
                bullet.matches(line) -> { flush(); assembler.listItem(ListKind.BULLET, bullet.find(line)!!.groupValues[1]) }
                numbered.matches(line) -> { flush(); assembler.listItem(ListKind.NUMBERED, numbered.find(line)!!.groupValues[1]) }
                else -> {
                    val previous = paragraph.lastOrNull()
                    val level = headingLevel(line)
                    if (level != null && (previous == null || sentenceEnd.containsMatchIn(previous))) {
                        flush()
                        assembler.text(line, level)
                        continue
                    }
                    // A short line that ends a sentence also ends the paragraph.
                    if (previous != null && sentenceEnd.containsMatchIn(previous) && previous.length < longest * 0.7) flush()
                    paragraph += line
                }
            }
        }
        flush()
        return assembler.build()
    }

    /** Lines that appear on at least half of the pages (when there are three or more): headers and footers. */
    private fun repeatedLines(pages: List<List<String>>): Set<String> {
        if (pages.size < 3) return emptySet()
        val counts = HashMap<String, Int>()
        pages.forEach { page -> page.filter { it.isNotEmpty() }.toSet().forEach { counts[it] = (counts[it] ?: 0) + 1 } }
        return counts.filter { it.value * 2 >= pages.size && it.key.length < 80 }.keys
    }

    private fun join(lines: List<String>): String {
        val out = StringBuilder()
        lines.forEach { line ->
            when {
                out.isEmpty() -> out.append(line)
                out.endsWith("-") && line.firstOrNull()?.isLowerCase() == true -> out.setLength(out.length - 1).also { out.append(line) }
                else -> out.append(' ').append(line)
            }
        }
        return out.toString()
    }

    private fun headingLevel(line: String): TextLevel? {
        if (line.length > MAX_HEADING || line.length < 3) return null
        if (line.last() in ".,;:") return null
        val letters = line.filter { it.isLetter() }
        return when {
            letters.length >= 3 && letters.all { it.isUpperCase() } -> TextLevel.H2
            sectionNumber.matches(line) && line.split(' ').size <= 10 -> TextLevel.H2
            else -> null
        }
    }

    private const val MAX_HEADING = 70
    private const val MIN_CHARACTERS = 40
}
