package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.ui.components.highlightMatches

/** A pin icon (when [pinned]) followed by one outlined chip per tag. Wraps onto more lines when needed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChipsRow(
    tags: List<String>,
    pinned: Boolean,
    highlight: List<String>,
    mark: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (pinned) {
            Icon(
                AppIcons.PushPin,
                contentDescription = stringResource(R.string.section_pinned),
                modifier = Modifier.size(16.dp),
                tint = colors.primary,
            )
        }
        tags.forEach { tag ->
            Text(
                text = highlightMatches("#$tag", highlight, mark),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .border(1.dp, colors.primary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}
