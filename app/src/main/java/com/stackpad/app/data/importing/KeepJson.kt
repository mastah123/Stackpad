package com.stackpad.app.data.importing

import com.stackpad.app.domain.importing.KeepNote
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reads one Google Keep Takeout `.json` file. */
internal object KeepJson {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * The note in [text], or null when the file is valid JSON but not a Keep note.
     * Throws [kotlinx.serialization.SerializationException] when it is not readable JSON at all.
     */
    fun parse(text: String): KeepNote? {
        val dto = json.decodeFromString(KeepDto.serializer(), text)
        // Every Keep note has a creation or edit time; other JSON files in a Takeout do not.
        if (dto.createdTimestampUsec <= 0 && dto.userEditedTimestampUsec <= 0) return null
        return KeepNote(
            title = dto.title,
            text = dto.textContent,
            items = dto.listContent.map { KeepNote.Item(it.text, it.isChecked) },
            trashed = dto.isTrashed,
            archived = dto.isArchived,
            pinned = dto.isPinned,
            color = dto.color,
            labels = dto.labels.map { it.name },
            attachments = dto.attachments.map { KeepNote.Attachment(it.filePath, it.mimetype) },
            links = dto.annotations.filter { it.url.isNotBlank() }.map { KeepNote.Link(it.title.ifBlank { it.description }, it.url) },
            createdUsec = dto.createdTimestampUsec,
            editedUsec = dto.userEditedTimestampUsec,
        )
    }

    @Serializable
    private data class KeepDto(
        val title: String = "",
        val textContent: String = "",
        val listContent: List<ItemDto> = emptyList(),
        val isTrashed: Boolean = false,
        val isArchived: Boolean = false,
        val isPinned: Boolean = false,
        val color: String = "DEFAULT",
        val labels: List<LabelDto> = emptyList(),
        val attachments: List<AttachmentDto> = emptyList(),
        val annotations: List<AnnotationDto> = emptyList(),
        val createdTimestampUsec: Long = 0,
        val userEditedTimestampUsec: Long = 0,
    )

    @Serializable
    private data class ItemDto(val text: String = "", val isChecked: Boolean = false)

    @Serializable
    private data class LabelDto(val name: String = "")

    @Serializable
    private data class AttachmentDto(val filePath: String = "", val mimetype: String = "")

    @Serializable
    private data class AnnotationDto(val title: String = "", val description: String = "", val url: String = "")
}
