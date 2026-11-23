package com.stackpad.app.domain.share

import com.stackpad.app.data.model.Note
import com.stackpad.app.domain.text.WordCount
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** The small lines under the title of an exported note: word count, dates and tags, as chosen. */
object ShareDetails {
    const val FOOTER = "Made with Stackpad"

    fun lines(note: Note, options: ShareOptions, zone: ZoneId, locale: Locale): List<String> {
        val lines = ArrayList<String>()
        val facts = ArrayList<String>()
        if (options.showWordCount) {
            val words = WordCount.of(note.title, note.blocks)
            facts += if (words == 1) "1 word" else "$words words"
        }
        if (options.showDates) {
            val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone)
            facts += "Created ${format.format(Instant.ofEpochMilli(note.createdAt))}"
            facts += "Edited ${format.format(Instant.ofEpochMilli(note.updatedAt))}"
        }
        if (facts.isNotEmpty()) lines += facts.joinToString(" · ")
        val tags = note.tags.filter { it.isNotBlank() }
        if (options.showTags && tags.isNotEmpty()) lines += tags.joinToString(", ", prefix = "Tags: ")
        return lines
    }
}
