package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.LinkBlock
import com.stackpad.app.domain.text.UrlUtils
import com.stackpad.app.ui.components.BlockSurface
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

/** Standalone link: optional title plus a validated URL that opens in the browser. */
@Composable
fun LinkBlockView(
    block: LinkBlock,
    onBlockFocused: () -> Unit,
    onEdit: ((LinkBlock) -> LinkBlock) -> Unit,
    onOpen: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val validUrl = UrlUtils.normalize(block.url)
    val invalid = block.url.isNotBlank() && validUrl == null

    BlockSurface(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.primary,
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                InlineTextField(
                    text = block.title,
                    onTextChange = { text -> onEdit { it.copy(title = text) } },
                    placeholder = stringResource(R.string.link_title_hint),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    onFocusChange = { if (it) onBlockFocused() },
                )
                InlineTextField(
                    text = block.url,
                    onTextChange = { text -> onEdit { it.copy(url = text) } },
                    placeholder = stringResource(R.string.link_url_hint),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = if (invalid) colors.error else colors.primary,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, keyboardType = KeyboardType.Uri),
                    onFocusChange = { if (it) onBlockFocused() },
                )
                if (invalid) {
                    Text(
                        text = stringResource(R.string.link_invalid),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.error,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
            SmallIconButton(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.cd_open_link),
                onClick = { onOpen(block.url) },
                enabled = validUrl != null,
                tint = colors.primary,
                size = 30.dp,
            )
            SmallIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_link),
                onClick = onRemove,
                size = 28.dp,
                iconSize = 16.dp,
            )
        }
    }
}
