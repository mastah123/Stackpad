package com.stackpad.app.data.backup

import com.stackpad.app.data.model.Block
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Contents of `backup.json` inside a backup zip. */
@Serializable
data class BackupFile(
    val formatVersion: Int,
    val appVersion: String = "",
    val createdAt: Long = 0L,
    val folders: List<BackupFolder> = emptyList(),
    val notes: List<BackupNote> = emptyList(),
)

@Serializable
data class BackupFolder(val id: String, val name: String, val createdAt: Long = 0L)

@Serializable
data class BackupNote(
    val id: String,
    val title: String = "",
    val blocks: List<Block> = emptyList(),
    val pinned: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val folderId: String? = null,
    val tags: List<String> = emptyList(),
    val color: Int = 0,
    val trashedAt: Long? = null,
    val locked: Boolean = false,
)

internal val backupJson = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Counts shown to the user after a restore. */
data class RestoreSummary(val added: Int, val updated: Int, val skipped: Int)

/** Why a backup could not be created or restored. */
enum class BackupError { NOT_A_BACKUP, UNSUPPORTED_VERSION, TOO_LARGE, UNREADABLE, IO }

class BackupException(val error: BackupError, cause: Throwable? = null) : Exception(error.name, cause)
