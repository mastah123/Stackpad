package com.stackpad.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextLevel

/** What the toolbar needs to know about the focused text block. */
data class FormatState(
    val level: TextLevel,
    val bold: Boolean,
    val italic: Boolean,
    val underline: Boolean,
)

/** Heading and inline-format controls, shown while a text block is focused. */
@Composable
fun FormatToolbar(
    state: FormatState,
    onToggleLevel: (TextLevel) -> Unit,
    onToggleSpan: (SpanType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(TextLevel.H1 to "H1", TextLevel.H2 to "H2", TextLevel.H3 to "H3").forEach { (level, label) ->
            FormatButton(
                isSelected = state.level == level,
                description = stringResource(R.string.cd_heading, label),
                onClick = { onToggleLevel(level) },
            ) {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.width(6.dp))
        FormatButton(
            isSelected = state.bold,
            description = stringResource(R.string.cd_bold),
            onClick = { onToggleSpan(SpanType.BOLD) },
        ) { FormatIcon(Icons.Filled.FormatBold) }
        FormatButton(
            isSelected = state.italic,
            description = stringResource(R.string.cd_italic),
            onClick = { onToggleSpan(SpanType.ITALIC) },
        ) { FormatIcon(Icons.Filled.FormatItalic) }
        FormatButton(
            isSelected = state.underline,
            description = stringResource(R.string.cd_underline),
            onClick = { onToggleSpan(SpanType.UNDERLINE) },
        ) { FormatIcon(Icons.Filled.FormatUnderlined) }
    }
}

@Composable
private fun FormatIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
}

@Composable
private fun FormatButton(
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = when {
        !enabled -> colors.onSurfaceVariant.copy(alpha = 0.38f)
        isSelected -> colors.onPrimaryContainer
        else -> colors.onSurface
    }
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = modifier
                .height(32.dp)
                .padding(horizontal = 1.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) colors.primaryContainer else colors.surface.copy(alpha = 0f))
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics {
                    contentDescription = description
                    selected = isSelected
                }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}
