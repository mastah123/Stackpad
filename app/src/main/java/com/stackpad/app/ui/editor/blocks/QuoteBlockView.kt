package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.QuoteBlock
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

/** Indented, italic quotation with an accent bar on the left. */
@Composable
fun QuoteBlockView(
    block: QuoteBlock,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onBlockFocused: () -> Unit,
    onEdit: ((QuoteBlock) -> QuoteBlock) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            runCatching { focusRequester.requestFocus() }
            onFocusHandled()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(colors.primary.copy(alpha = 0.7f)),
        )
        InlineTextField(
            text = block.text,
            onTextChange = { text -> onEdit { it.copy(text = text) } },
            placeholder = stringResource(R.string.quote_hint),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                color = colors.onSurfaceVariant,
            ),
            focusRequester = focusRequester,
            onFocusChange = { if (it) onBlockFocused() },
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 2.dp),
        )
        SmallIconButton(
            icon = Icons.Filled.Close,
            contentDescription = stringResource(R.string.cd_remove_quote),
            onClick = onRemove,
            size = 28.dp,
            iconSize = 16.dp,
        )
    }
}
