package com.stackpad.app.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.prefs.ThemeMode
import com.stackpad.app.domain.notes.NoteSort
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.ui.components.SmallIconButton

/** Always-visible search box with a clear button. */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.onSurfaceVariant,
            )
            InlineTextField(
                text = query,
                onTextChange = onQueryChange,
                placeholder = stringResource(R.string.search_hint),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 6.dp),
            )
            if (query.isNotEmpty()) {
                SmallIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_clear_search),
                    onClick = { onQueryChange("") },
                )
            }
        }
    }
}

/** Top-bar menu to choose system, light or dark theme. */
@Composable
fun ThemeMenu(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.BrightnessMedium, contentDescription = stringResource(R.string.cd_theme))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ThemeMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text(stringResource(mode.labelRes())) },
                    trailingIcon = {
                        if (mode == current) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                    onClick = {
                        open = false
                        onSelect(mode)
                    },
                )
            }
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

/** Top-bar menu to choose how notes are sorted. */
@Composable
fun SortMenu(
    current: NoteSort,
    onSelect: (NoteSort) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Filled.SwapVert, contentDescription = stringResource(R.string.cd_sort))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            NoteSort.values().forEach { sort ->
                DropdownMenuItem(
                    text = { Text(stringResource(sort.labelRes())) },
                    trailingIcon = {
                        if (sort == current) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                    onClick = {
                        open = false
                        onSelect(sort)
                    },
                )
            }
        }
    }
}

private fun NoteSort.labelRes(): Int = when (this) {
    NoteSort.LAST_EDITED -> R.string.sort_last_edited
    NoteSort.CREATED -> R.string.sort_created
    NoteSort.TITLE -> R.string.sort_title
}
