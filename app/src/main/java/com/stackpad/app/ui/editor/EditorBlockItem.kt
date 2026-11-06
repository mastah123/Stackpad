package com.stackpad.app.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.audio.PlaybackState
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.AudioBlock
import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ImageBlock
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.data.model.SheetBlock
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.ui.editor.blocks.AudioBlockView
import com.stackpad.app.ui.editor.blocks.ChecklistBlockView
import com.stackpad.app.ui.editor.blocks.ImageBlockView
import com.stackpad.app.ui.editor.blocks.LinkBlockView
import com.stackpad.app.ui.editor.blocks.ListBlockView
import com.stackpad.app.ui.editor.blocks.PromptBlockView
import com.stackpad.app.ui.editor.blocks.SpreadsheetBlockView
import com.stackpad.app.ui.editor.blocks.TextBlockView

/** Picks the right view for a [Block] and wires it to the view model. */
@Composable
fun EditorBlockItem(
    block: Block,
    isOnlyBlock: Boolean,
    focusRequested: Boolean,
    playback: PlaybackState,
    viewModel: NoteEditorViewModel,
    media: MediaStorage,
    onOpenUrl: (String) -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val id = block.id
    val onFocusHandled = { viewModel.consumeFocus(id) }
    val onBlockFocused = { viewModel.onBlockFocused(id) }
    val onRemove = { viewModel.removeBlock(id) }

    when (block) {
        is TextBlock -> TextBlockView(
            block = block,
            placeholder = if (isOnlyBlock) stringResource(R.string.editor_start_writing) else null,
            requestFocus = focusRequested,
            onFocusHandled = onFocusHandled,
            onEdit = { transform -> viewModel.edit<TextBlock>(id, transform) },
            onSelectionChange = { start, end -> viewModel.onTextSelection(id, start, end) },
            onBackspaceWhenEmpty = { viewModel.removeBlock(id, viaBackspace = true) },
            modifier = modifier,
        )

        is ListBlock -> ListBlockView(
            block = block,
            requestFocus = focusRequested,
            onFocusHandled = onFocusHandled,
            onBlockFocused = onBlockFocused,
            onEdit = { transform -> viewModel.edit<ListBlock>(id, transform) },
            onRemove = onRemove,
            modifier = modifier,
        )

        is ChecklistBlock -> ChecklistBlockView(
            block = block,
            requestFocus = focusRequested,
            onFocusHandled = onFocusHandled,
            onBlockFocused = onBlockFocused,
            onEdit = { transform -> viewModel.edit<ChecklistBlock>(id, transform) },
            onRemove = onRemove,
            modifier = modifier,
        )

        is ImageBlock -> ImageBlockView(
            file = media.file(block.fileName),
            onRemove = onRemove,
            modifier = modifier,
        )

        is SheetBlock -> SpreadsheetBlockView(
            block = block,
            onBlockFocused = onBlockFocused,
            onEdit = { transform -> viewModel.edit<SheetBlock>(id, transform) },
            onRemove = onRemove,
            modifier = modifier,
        )

        is AudioBlock -> AudioBlockView(
            block = block,
            playback = playback,
            available = viewModel.isAudioAvailable(block),
            onTogglePlay = { viewModel.togglePlayback(block) },
            onRemove = onRemove,
            modifier = modifier,
        )

        is LinkBlock -> LinkBlockView(
            block = block,
            onBlockFocused = onBlockFocused,
            onEdit = { transform -> viewModel.edit<LinkBlock>(id, transform) },
            onOpen = onOpenUrl,
            onRemove = onRemove,
            modifier = modifier,
        )

        is PromptBlock -> PromptBlockView(
            block = block,
            requestFocus = focusRequested,
            onFocusHandled = onFocusHandled,
            onBlockFocused = onBlockFocused,
            onEdit = { transform -> viewModel.edit<PromptBlock>(id, transform) },
            onCopied = onCopied,
            onRemove = onRemove,
            modifier = modifier,
        )
    }
}
