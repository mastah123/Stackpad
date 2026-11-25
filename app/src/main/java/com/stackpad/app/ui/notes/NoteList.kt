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
import com.stackpad.app.domain.notes.ListOrder
import com.stackpad.app.domain.notes.NoteGrouping
import com.stackpad.app.domain.notes.NoteSection
import com.stackpad.app.domain.notes.SectionLabel

/**
 * The notes under section headers (PINNED, RECENT, or the chosen grouping), as a list or a two-column
 * grid (the change cross-fades). [headerActions] (sort and layout icons) are drawn on the right of the
 * first header. With the default grouping the RECENT header only shows when something is pinned,
 * unless [alwaysShowHeaders] is set.
 */
@Composable
fun NoteList(
    notes: List<NoteSummary>,
    order: ListOrder,
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
    val sections = remember(notes, order) { NoteGrouping.group(notes, order) }
    val hasPinned = sections.any { it.label == SectionLabel.Pinned }
    // The label to draw above a section, or null for none.
    fun headerOf(section: NoteSection): SectionLabel? = when {
        section.label == SectionLabel.All && !alwaysShowHeaders -> null
        section.label == SectionLabel.Recent && !alwaysShowHeaders && !hasPinned -> null
        else -> section.label
    }
    val firstHeaderKey = sections.firstOrNull { headerOf(it) != null }?.key

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

        @Composable
        fun header(section: NoteSection, label: SectionLabel, modifier: Modifier) = SectionHeader(
            title = label.text(),
            modifier = modifier,
            horizontalInset = 4.dp,
            trailing = if (section.key == firstHeaderKey) headerActions else null,
        )

        if (grid) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 88.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEach { section ->
                    headerOf(section)?.let { label ->
                        item(key = "header_${section.key}", span = StaggeredGridItemSpan.FullLine) {
                            header(section, label, Modifier.animateItem())
                        }
                    }
                    items(section.notes, key = { it.id }) { note -> card(note, Modifier.animateItem()) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEach { section ->
                    headerOf(section)?.let { label ->
                        item(key = "header_${section.key}") {
                            header(section, label, Modifier.animateItem())
                        }
                    }
                    items(section.notes, key = { it.id }) { note -> card(note, Modifier.animateItem()) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel.text(): String = when (this) {
    SectionLabel.Pinned -> stringResource(R.string.section_pinned)
    SectionLabel.Recent -> stringResource(R.string.section_recent)
    SectionLabel.All -> stringResource(R.string.section_all_notes)
    SectionLabel.ThisWeek -> stringResource(R.string.section_this_week)
    SectionLabel.LastWeek -> stringResource(R.string.section_last_week)
    is SectionLabel.Text -> text
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
