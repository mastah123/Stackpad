package com.stackpad.app.domain.backup

/** How often the automatic backup runs. */
enum class BackupFrequency(val days: Long) { DAILY(1), WEEKLY(7) }
