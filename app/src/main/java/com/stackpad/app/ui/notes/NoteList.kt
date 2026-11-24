package com.stackpad.app.ui.notes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.prefs.ListLayout
import com.stackpad.app.domain.notes.NoteSections

/**
 * The notes as PINNED and RECENT sections, as a list or a two-column grid (the change cross-fades).
 * [headerActions] (sort and layout icons) are drawn on the right of the first section header.
 * Section headers always show when [alwaysShowHeaders] is set; otherwise only when something is pinned.
 */
@Composable
fun NoteList(
    notes: List<NoteSummary>,
    layout: ListLayout,
    highlight: List<String>,
    selecting: Boolean,
    selectedIds: Set<String>,
    alwaysShowHeaders: Boolean,
    onOpen: (String) -> Unit,
    onLongPress: (String) -> Unit,
    actionsFor: (NoteSummary) -> NoteCardActions,
    headerActions: (@Composable () -> Unit)? = null,
) {
    val sections = remember(notes) { NoteSections.of(notes) }
    val showPinnedHeader = sections.pinned.isNotEmpty()
    val showRecentHeader = sections.recent.isNotEmpty() && (alwaysShowHeaders || showPinnedHeader)
    val actionsOnPinned = showPinnedHeader
    val recentTitle = stringResource(R.string.section_recent)
    val pinnedTitle = stringResource(R.string.section_pinned)

    Crossfade(targetState = layout, animationSpec = tween(220), label = "noteLayout") { current ->
        val grid = current == ListLayout.GRID

        @Composable
        fun card(note: NoteSummary, modifier: Modifier) = NoteCard(
            note = note,
            onClick = { onOpen(note.id) },
            actions = actionsFor(note),
            modifier = modifier,
            grid = grid,
            highlight = highlight,
            selecting = selecting,
            selected = note.id in selectedIds,
            onLongClick = { onLongPress(note.id) },
            previewLines = if (grid) GRID_PREVIEW_LINES else LIST_PREVIEW_LINES,
            previewMaxHeight = if (grid) GRID_PREVIEW_HEIGHT else LIST_PREVIEW_HEIGHT,
            previewItems = if (grid) GRID_PREVIEW_ITEMS else LIST_PREVIEW_ITEMS,
        )

        if (grid) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 88.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showPinnedHeader) {
                    item(key = "header_pinned", span = StaggeredGridItemSpan.FullLine) {
                        SectionHeader(pinnedTitle, Modifier.animateItem(), horizontalInset = 4.dp, trailing = headerActions)
                    }
                    items(sections.pinned, key = { it.id }) { note -> card(note, Modifier.animateItem()) }
                }
                if (showRecentHeader) {
                    item(key = "header_recent", span = StaggeredGridItemSpan.FullLine) {
                        SectionHeader(
                            recentTitle,
                            Modifier.animateItem(),
                            horizontalInset = 4.dp,
                            trailing = if (actionsOnPinned) null else headerActions,
                        )
                    }
                }
                items(sections.recent, key = { it.id }) { note -> card(note, Modifier.animateItem()) }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                if (showPinnedHeader) {
                    item(key = "header_pinned") {
                        SectionHeader(pinnedTitle, Modifier.animateItem(), trailing = headerActions)
                    }
                    items(sections.pinned, key = { it.id }) { note -> card(note, Modifier.animateItem()) }
                }
                if (showRecentHeader) {
                    item(key = "header_recent") {
                        SectionHeader(
                            recentTitle,
                            Modifier.animateItem(),
                            trailing = if (actionsOnPinned) null else headerActions,
                        )
                    }
                }
                items(sections.recent, key = { it.id }) { note -> card(note, Modifier.animateItem()) }
            }
        }
    }
}

private const val LIST_PREVIEW_LINES = 2
private const val GRID_PREVIEW_LINES = 6
private val LIST_PREVIEW_HEIGHT = 96.dp
private val GRID_PREVIEW_HEIGHT = 200.dp
private const val LIST_PREVIEW_ITEMS = 5
private const val GRID_PREVIEW_ITEMS = 9

/** Accent-colored section name with an underline; [trailing] icons sit on the right. */
@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    horizontalInset: Dp = 16.dp,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = horizontalInset, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = horizontalInset, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
