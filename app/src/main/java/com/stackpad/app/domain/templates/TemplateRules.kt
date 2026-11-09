package com.stackpad.app.domain.templates

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.isBlank

/** Rules for saving and applying templates. Pure, so it is unit tested. */
object TemplateRules {
    const val MAX_NAME_LENGTH = 40
    const val MAX_USER_TEMPLATES = 20

    /** A clean template name, or null when nothing usable is left. */
    fun normalizeName(raw: String): String? =
        raw.trim().replace(Regex("\\s+"), " ").take(MAX_NAME_LENGTH).trim().ifEmpty { null }

    /**
     * What is kept when a note becomes a template: no images or audio (their files belong to the
     * note), checklist items unchecked and without due dates, and no blank blocks.
     */
    fun storableBlocks(blocks: List<Block>): List<Block> =
        blocks
            .filterNot { it is ImageBlock || it is AudioBlock }
            .filterNot { it.isBlank() }
            .map { block ->
                if (block is ChecklistBlock) {
                    block.copy(items = block.items.map { it.copy(checked = false, dueDate = null) })
                } else {
                    block
                }
            }

    /**
     * The blocks of a note after applying [template]: a note that is still empty is replaced by the
     * template, otherwise the template is added at the end. Ids are always fresh.
     */
    fun apply(existing: List<Block>, template: NoteTemplate): List<Block> {
        val added = BlockIds.withFreshIds(template.blocks)
        val isEmpty = existing.all { it.isBlank() }
        return if (isEmpty) added else existing + added
    }
}
