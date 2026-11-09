package com.stackpad.app.domain.templates

import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.data.model.DividerBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock

/** Copies blocks with brand-new ids (also for list and checklist items), so a copy never clashes with the original. */
object BlockIds {
    fun withFreshIds(blocks: List<Block>, newId: () -> String = BlockFactory::newId): List<Block> =
        blocks.map { block ->
            when (block) {
                is TextBlock -> block.copy(id = newId())
                is ListBlock -> block.copy(id = newId(), items = block.items.map { it.copy(id = newId()) })
                is ChecklistBlock -> block.copy(id = newId(), items = block.items.map { it.copy(id = newId()) })
                is ImageBlock -> block.copy(id = newId())
                is SheetBlock -> block.copy(id = newId())
                is AudioBlock -> block.copy(id = newId())
                is LinkBlock -> block.copy(id = newId())
                is PromptBlock -> block.copy(id = newId())
                is QuoteBlock -> block.copy(id = newId())
                is CodeBlock -> block.copy(id = newId())
                is DividerBlock -> block.copy(id = newId())
            }
        }
}
