package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block

/** Plain `.txt` files: the file name is the title, and blank lines separate paragraphs. */
object PlainTextParser {

    fun parse(text: String): List<Block> {
        val assembler = BlockAssembler()
        text.replace("\r\n", "\n").replace('\r', '\n')
            .split(Regex("\n\\s*\n"))
            .forEach { assembler.text(it.trim()) }
        return assembler.build()
    }

    /** "shopping list.txt" -> "shopping list". */
    fun titleFromFileName(fileName: String): String =
        fileName.substringAfterLast('/').substringBeforeLast('.').trim()
}
