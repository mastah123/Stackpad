package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stackpad.app.R
import com.stackpad.app.data.model.PromptBlock
import com.stackpad.app.ui.components.BlockSurface
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

/** Monospace prompt card with a copy-to-clipboard button. */
@Composable
fun PromptBlockView(
    block: PromptBlock,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onBlockFocused: () -> Unit,
    onEdit: ((PromptBlock) -> PromptBlock) -> Unit,
    onCopied: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            runCatching { focusRequester.requestFocus() }
            onFocusHandled()
        }
    }

    BlockSurface(
        modifier = modifier.fillMaxWidth(),
        color = colors.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = colors.primary,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.block_prompt),
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                modifier = Modifier.weight(1f),
            )
            SmallIconButton(
                icon = Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.cd_copy_prompt),
                onClick = {
                    clipboard.setText(AnnotatedString(block.text))
                    onCopied()
                },
                enabled = block.text.isNotBlank(),
                tint = colors.primary,
                size = 30.dp,
            )
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_prompt),
                onClick = onRemove,
                size = 28.dp,
                iconSize = 16.dp,
            )
        }
        InlineTextField(
            text = block.text,
            onTextChange = { text -> onEdit { it.copy(text = text) } },
            placeholder = stringResource(R.string.prompt_hint),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = colors.onSurface,
            ),
            focusRequester = focusRequester,
            onFocusChange = { if (it) onBlockFocused() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
