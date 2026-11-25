package com.stackpad.app.ui.notes

import com.stackpad.app.ui.icons.AppIcons
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.stackpad.app.R
import com.stackpad.app.ui.components.SmallIconButton

/** What the three-dot button on a note does: it opens the note options sheet. */
class NoteCardActions(val onOpenOptions: () -> Unit)

/** Three-dot button on a note. */
@Composable
fun NoteCardMenu(actions: NoteCardActions) {
    SmallIconButton(
        icon = AppIcons.MoreVert,
        contentDescription = stringResource(R.string.cd_more_actions),
        onClick = actions.onOpenOptions,
    )
}
