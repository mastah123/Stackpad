package com.stackpad.app.domain.notes

import com.stackpad.app.data.model.NoteSummary

/** The note list split into the PINNED and RECENT sections, keeping the incoming order inside each. */
data class NoteSections(val pinned: List<NoteSummary>, val recent: List<NoteSummary>) {
    val isEmpty: Boolean get() = pinned.isEmpty() && recent.isEmpty()

    companion object {
        fun of(notes: List<NoteSummary>): NoteSections {
            val (pinned, recent) = notes.partition { it.pinned }
            return NoteSections(pinned, recent)
        }
    }
}
