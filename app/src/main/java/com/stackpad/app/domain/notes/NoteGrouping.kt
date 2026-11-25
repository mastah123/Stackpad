package com.stackpad.app.domain.notes

import com.stackpad.app.data.model.NoteSummary
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** What a section header says; the screen turns the fixed ones into translated text. */
sealed interface SectionLabel {
    data object Pinned : SectionLabel
    data object Recent : SectionLabel
    data object All : SectionLabel
    data object ThisWeek : SectionLabel
    data object LastWeek : SectionLabel
    data class Text(val text: String) : SectionLabel
}

/** A run of notes under one header. [label] is null when no header is shown. */
data class NoteSection(val key: String, val label: SectionLabel?, val notes: List<NoteSummary>)

/** Splits an already ordered note list into sections. Pure logic, unit tested. */
object NoteGrouping {

    /**
     * Pinned notes come first in their own section (except with [NoteGroup.NONE]); the rest follow
     * [group]. Groups keep the order their first note appears in. Dates follow the sort field
     * (created, otherwise last edited).
     */
    fun group(
        notes: List<NoteSummary>,
        order: ListOrder,
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
        today: LocalDate = LocalDate.now(zone),
    ): List<NoteSection> {
        if (notes.isEmpty()) return emptyList()
        if (order.group == NoteGroup.NONE) return listOf(NoteSection("all", SectionLabel.All, notes))
        val (pinned, rest) = notes.partition { it.pinned }
        val sections = mutableListOf<NoteSection>()
        if (pinned.isNotEmpty()) sections += NoteSection("pinned", SectionLabel.Pinned, pinned)
        if (rest.isEmpty()) return sections
        if (order.group == NoteGroup.DEFAULT) {
            sections += NoteSection("recent", SectionLabel.Recent, rest)
            return sections
        }
        val groups = LinkedHashMap<String, MutableList<NoteSummary>>()
        val labels = HashMap<String, SectionLabel>()
        for (note in rest) {
            val millis = if (order.sort == NoteSort.CREATED) note.createdAt else note.updatedAt
            val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
            val (key, label) = keyAndLabel(note, order.group, date, today, locale)
            groups.getOrPut(key) { mutableListOf() } += note
            labels.putIfAbsent(key, label)
        }
        groups.forEach { (key, list) -> sections += NoteSection(key, labels.getValue(key), list) }
        return sections
    }

    private fun keyAndLabel(
        note: NoteSummary,
        group: NoteGroup,
        date: LocalDate,
        today: LocalDate,
        locale: Locale,
    ): Pair<String, SectionLabel> = when (group) {
        NoteGroup.ABC -> {
            val first = note.title.trim().firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar()?.toString() ?: "#"
            first to SectionLabel.Text(first)
        }
        NoteGroup.YEAR -> date.year.toString() to SectionLabel.Text(date.year.toString())
        NoteGroup.MONTH -> {
            val text = date.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
            "${date.year}-${date.monthValue}" to SectionLabel.Text(text)
        }
        else -> {
            val start = weekStart(date)
            val thisWeek = weekStart(today)
            val label = when (start) {
                thisWeek -> SectionLabel.ThisWeek
                thisWeek.minusWeeks(1) -> SectionLabel.LastWeek
                else -> {
                    val short = DateTimeFormatter.ofPattern("MMM d", locale)
                    SectionLabel.Text("${start.format(short)} – ${start.plusDays(6).format(short)}")
                }
            }
            start.toString() to label
        }
    }

    private fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
