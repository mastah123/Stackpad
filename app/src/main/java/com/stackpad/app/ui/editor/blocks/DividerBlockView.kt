package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.ui.components.SmallIconButton

/** A thin horizontal rule with an "x" to remove it. */
@Composable
fun DividerBlockView(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        SmallIconButton(
            icon = Icons.Filled.Close,
            contentDescription = stringResource(R.string.cd_remove_divider),
            onClick = onRemove,
            size = 28.dp,
            iconSize = 16.dp,
        )
    }
}
