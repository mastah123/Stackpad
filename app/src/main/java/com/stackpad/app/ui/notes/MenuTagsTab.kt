package com.stackpad.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.notes.TagCount
import com.stackpad.app.domain.notes.TagSort

/**
 * Tags tab: a "Filter tags" box and the tags with their note counts. [tags] is already filtered and
 * sorted; [hasAnyTags] tells "no tags yet" apart from "no tag matches the filter".
 */
@Composable
fun MenuTagsTab(
    tags: List<TagCount>,
    hasAnyTags: Boolean,
    filterText: String,
    onFilterText: (String) -> Unit,
    selectedTag: String?,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.menu_tags),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
            )
        }
        item {
            OutlinedTextField(
                value = filterText,
                onValueChange = onFilterText,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(stringResource(R.string.menu_filter_tags)) },
                trailingIcon = {
                    if (filterText.isNotEmpty()) {
                        IconButton(onClick = { onFilterText("") }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_filter))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
        items(tags, key = { it.tag }) { entry ->
            MenuRow(Icons.Filled.Sell, entry.tag, selectedTag == entry.tag, { onSelect(entry.tag) }, entry.count)
        }
        if (tags.isEmpty()) {
            item {
                Text(
                    text = stringResource(if (hasAnyTags) R.string.menu_no_matching_tags else R.string.menu_no_tags),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** Sort button for the Tags tab's bottom bar. */
@Composable
fun TagSortMenu(current: TagSort, onSelect: (TagSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.SwapVert, contentDescription = stringResource(R.string.cd_sort_tags))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            TagSort.values().forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(if (sort == TagSort.NAME) R.string.tag_sort_name else R.string.tag_sort_count))
                    },
                    trailingIcon = { if (sort == current) Icon(Icons.Filled.Check, contentDescription = null) },
                    onClick = {
                        open = false
                        onSelect(sort)
                    },
                )
            }
        }
    }
}
