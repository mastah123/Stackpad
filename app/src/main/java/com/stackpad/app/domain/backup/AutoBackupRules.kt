package com.stackpad.app.domain.backup

import java.time.LocalDate

/** Naming and clean-up rules for automatic backup files. */
object AutoBackupRules {
    /** How many automatic backups are kept in the folder. */
    const val KEEP = 7

    private val pattern = Regex("^stackpad-auto-(\\d{4}-\\d{2}-\\d{2})\\.zip$")

    fun fileName(date: LocalDate): String = "stackpad-auto-$date.zip"

    /** The date in an automatic backup's file name, or null for any other file. */
    fun dateOf(name: String): LocalDate? =
        pattern.matchEntire(name)?.groupValues?.get(1)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** Names of the oldest automatic backups beyond [keep]. Files that are not automatic backups are never listed. */
    fun toDelete(names: List<String>, keep: Int = KEEP): List<String> =
        names.mapNotNull { name -> dateOf(name)?.let { name to it } }
            .sortedByDescending { it.second }
            .drop(keep)
            .map { it.first }
}
