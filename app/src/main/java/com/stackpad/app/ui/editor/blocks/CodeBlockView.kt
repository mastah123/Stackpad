package com.stackpad.app.ui.editor.blocks

import com.stackpad.app.ui.icons.AppIcons
import com.stackpad.app.ui.theme.BlockAccent
import com.stackpad.app.ui.theme.look
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stackpad.app.R
import com.stackpad.app.data.model.CodeBlock
import com.stackpad.app.ui.components.BlockSurface
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

/** Monospace block for code or commands: no auto-capitalization, with a copy button. */
@Composable
fun CodeBlockView(
    block: CodeBlock,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onBlockFocused: () -> Unit,
    onEdit: ((CodeBlock) -> CodeBlock) -> Unit,
    onCopied: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val look = BlockAccent.CODE.look()
    val clipboard = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            runCatching { focusRequester.requestFocus() }
            onFocusHandled()
        }
    }

    BlockSurface(modifier = modifier.fillMaxWidth(), accent = BlockAccent.CODE) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppIcons.Code,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = look.label,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.block_code),
                style = MaterialTheme.typography.labelSmall,
                color = look.label,
                modifier = Modifier.weight(1f),
            )
            SmallIconButton(
                icon = AppIcons.ContentCopy,
                contentDescription = stringResource(R.string.cd_copy_code),
                onClick = {
                    clipboard.setText(AnnotatedString(block.text))
                    onCopied()
                },
                enabled = block.text.isNotBlank(),
                tint = look.content,
                size = 30.dp,
            )
            SmallIconButton(
                icon = AppIcons.Close,
                contentDescription = stringResource(R.string.cd_remove_code),
                onClick = onRemove,
                tint = look.content,
                size = 28.dp,
                iconSize = 16.dp,
            )
        }
        InlineTextField(
            text = block.text,
            onTextChange = { text -> onEdit { it.copy(text = text) } },
            placeholder = stringResource(R.string.code_hint),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = look.content,
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            focusRequester = focusRequester,
            onFocusChange = { if (it) onBlockFocused() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
