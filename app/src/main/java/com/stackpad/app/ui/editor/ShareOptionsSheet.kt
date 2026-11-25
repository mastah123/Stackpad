package com.stackpad.app.ui.editor

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.stackpad.app.R
import com.stackpad.app.domain.share.ShareCleanUp
import com.stackpad.app.domain.share.ShareOptions
import com.stackpad.app.domain.share.SharePageSize
import com.stackpad.app.domain.share.ShareTextSize
import com.stackpad.app.domain.share.ShareTheme

/**
 * Choices for sharing a note as an image or a PDF, in three collapsible sections (Look, Clean up,
 * Details) with one tick for every choice. "Don't show this again" makes later shares skip this
 * sheet and use the saved choices; Share > Share options opens it again.
 *
 * @param confirmLabel text of the main button: "Share" when a share follows, "Save" when only the choices are edited.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareOptionsSheet(
    initial: ShareOptions,
    dontShowAgain: Boolean,
    confirmLabel: Int,
    onDismiss: () -> Unit,
    onConfirm: (options: ShareOptions, dontShowAgain: Boolean) -> Unit,
) {
    var options by remember { mutableStateOf(initial) }
    var hide by remember { mutableStateOf(dontShowAgain) }
    var look by remember { mutableStateOf(true) }
    var cleanUp by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Text(stringResource(R.string.share_options), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.share_options_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Section(R.string.share_section_look, look, { look = !look }) {
                ChipRow(R.string.share_theme) {
                    Choice(R.string.share_theme_light, ShareTheme.LIGHT, options.theme) { options = options.copy(theme = it) }
                    Choice(R.string.share_theme_dark, ShareTheme.DARK, options.theme) { options = options.copy(theme = it) }
                    Choice(R.string.share_theme_app, ShareTheme.APP, options.theme) { options = options.copy(theme = it) }
                }
                ChipRow(R.string.share_text_size) {
                    Choice(R.string.share_size_small, ShareTextSize.SMALL, options.textSize) { options = options.copy(textSize = it) }
                    Choice(R.string.share_size_medium, ShareTextSize.MEDIUM, options.textSize) { options = options.copy(textSize = it) }
                    Choice(R.string.share_size_large, ShareTextSize.LARGE, options.textSize) { options = options.copy(textSize = it) }
                }
                ChipRow(R.string.share_page_size) {
                    Choice(R.string.share_page_a4, SharePageSize.A4, options.pageSize) { options = options.copy(pageSize = it) }
                    Choice(R.string.share_page_letter, SharePageSize.LETTER, options.pageSize) { options = options.copy(pageSize = it) }
                }
                Tick(R.string.share_note_tint, options.noteTint) { options = options.copy(noteTint = it) }
                Tick(R.string.share_include_pictures, options.includePictures) { options = options.copy(includePictures = it) }
                Tick(R.string.share_hide_done, options.hideCompletedTasks) { options = options.copy(hideCompletedTasks = it) }
                Tick(R.string.share_page_numbers, options.pageNumbers) { options = options.copy(pageNumbers = it) }
            }

            Section(R.string.share_section_cleanup, cleanUp, { cleanUp = !cleanUp }) {
                fun change(update: (ShareCleanUp) -> ShareCleanUp) { options = options.copy(cleanUp = update(options.cleanUp)) }
                Tick(R.string.share_clean_lines, options.cleanUp.emptyLines) { v -> change { it.copy(emptyLines = v) } }
                Tick(R.string.share_clean_items, options.cleanUp.emptyListItems) { v -> change { it.copy(emptyListItems = v) } }
                Tick(R.string.share_clean_checklists, options.cleanUp.emptyChecklists) { v -> change { it.copy(emptyChecklists = v) } }
                Tick(R.string.share_clean_tables, options.cleanUp.emptyTables) { v -> change { it.copy(emptyTables = v) } }
                Tick(R.string.share_clean_cards, options.cleanUp.emptyCards) { v -> change { it.copy(emptyCards = v) } }
            }

            Section(R.string.share_section_details, details, { details = !details }) {
                Tick(R.string.share_show_title, options.showTitle) { options = options.copy(showTitle = it) }
                Tick(R.string.share_show_words, options.showWordCount) { options = options.copy(showWordCount = it) }
                Tick(R.string.share_show_dates, options.showDates) { options = options.copy(showDates = it) }
                Tick(R.string.share_show_tags, options.showTags) { options = options.copy(showTags = it) }
                Tick(R.string.share_show_footer, options.showFooter) { options = options.copy(showFooter = it) }
            }

            Tick(R.string.share_dont_show_again, hide) { hide = it }
            Text(
                stringResource(R.string.share_dont_show_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp, bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                Button(onClick = { onConfirm(options, hide) }, modifier = Modifier.padding(start = 8.dp)) {
                    Text(stringResource(confirmLabel))
                }
            }
        }
    }
}

/** A collapsible group of choices with a header that shows or hides them. */
@Composable
private fun Section(title: Int, expanded: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (expanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
            contentDescription = stringResource(if (expanded) R.string.cd_collapse_section else R.string.cd_expand_section),
        )
    }
    if (expanded) content()
}

/** One choice with a checkbox; the whole row is tappable. */
@Composable
private fun Tick(label: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Checkbox) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(12.dp))
        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChipRow(label: Int, chips: @Composable () -> Unit) {
    Text(
        stringResource(label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) { chips() }
}

/** One selectable value in a row of choices. */
@Composable
private fun <T> Choice(label: Int, value: T, selected: T, onSelect: (T) -> Unit) =
    FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(stringResource(label)) })
