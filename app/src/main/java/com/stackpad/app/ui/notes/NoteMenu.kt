package com.stackpad.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stackpad.app.R

/**
 * Thin side menu opened from the top-left button: the logo, then icon shortcuts for All notes,
 * Pinned, Folders, Tags and Trash. Folders and Tags open a picker.
 */
@Composable
fun NoteMenu(
    mode: ListMode,
    onAll: () -> Unit,
    onPinned: () -> Unit,
    onFolders: () -> Unit,
    onTags: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit,
) {
    // A plain Surface instead of ModalDrawerSheet, which cannot be narrower than 240dp.
    Surface(
        modifier = Modifier.width(MENU_WIDTH).fillMaxHeight(),
        shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight().navigationBarsPadding(),
            containerColor = Color.Transparent,
            header = {
                Image(
                    painter = painterResource(R.drawable.stackpad_logo),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).size(34.dp),
                )
            },
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MenuItem(Icons.Filled.Notes, R.string.menu_all, mode == ListMode.ALL, onAll)
                MenuItem(Icons.Filled.PushPin, R.string.menu_pinned, mode == ListMode.PINNED, onPinned)
                MenuItem(Icons.Filled.Folder, R.string.menu_folders, mode == ListMode.FOLDERS, onFolders)
                MenuItem(Icons.Filled.Sell, R.string.menu_tags, mode == ListMode.TAGS, onTags)
                MenuItem(Icons.Filled.Delete, R.string.menu_trash, false, onTrash)
                MenuItem(Icons.Filled.Settings, R.string.menu_settings, false, onSettings)
            }
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, label: Int, selected: Boolean, onClick: () -> Unit) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(stringResource(label), style = MaterialTheme.typography.labelSmall, maxLines = 1) },
        alwaysShowLabel = true,
    )
}

private val MENU_WIDTH = 72.dp
