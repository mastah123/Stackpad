package com.stackpad.app.ui.components

import com.stackpad.app.ui.theme.look
import com.stackpad.app.ui.theme.BlockAccent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Compact rounded container shared by the card-like blocks (checklist, table, link, prompt). */
@Composable
fun BlockSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    border: BorderStroke? = null,
    /** When set, the card takes this block type's color instead of [color]. */
    accent: BlockAccent? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val look = accent?.look()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = look?.container ?: color,
        border = border ?: look?.border?.let { BorderStroke(1.dp, it) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            content = content,
        )
    }
}
