package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.image.ImageEdit
import com.stackpad.app.domain.image.ImageFilter
import com.stackpad.app.domain.image.ImageSize

/** Colour look and saved size, shown with the crop and rotate tools. */
@Composable
fun ImageAdjustControls(
    edit: ImageEdit,
    enabled: Boolean,
    onFilter: (ImageFilter) -> Unit,
    onSize: (ImageSize) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ChipRow(R.string.image_filter_label) {
                ImageFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = edit.filter == filter,
                        onClick = { onFilter(filter) },
                        enabled = enabled,
                        label = { Text(stringResource(filter.label())) },
                    )
                }
            }
            ChipRow(R.string.image_size_label) {
                ImageSize.entries.forEach { size ->
                    FilterChip(
                        selected = edit.size == size,
                        onClick = { onSize(size) },
                        enabled = enabled,
                        label = { Text(stringResource(size.label())) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipRow(title: Int, chips: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 4.dp))
        chips()
    }
}

private fun ImageFilter.label(): Int = when (this) {
    ImageFilter.NONE -> R.string.image_filter_none
    ImageFilter.MONO -> R.string.image_filter_mono
    ImageFilter.SEPIA -> R.string.image_filter_sepia
    ImageFilter.VIVID -> R.string.image_filter_vivid
    ImageFilter.COOL -> R.string.image_filter_cool
    ImageFilter.WARM -> R.string.image_filter_warm
}

private fun ImageSize.label(): Int = when (this) {
    ImageSize.ORIGINAL -> R.string.image_size_original
    ImageSize.LARGE -> R.string.image_size_large
    ImageSize.MEDIUM -> R.string.image_size_medium
    ImageSize.SMALL -> R.string.image_size_small
}
