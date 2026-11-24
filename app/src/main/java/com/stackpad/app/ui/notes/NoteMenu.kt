package com.stackpad.app.ui.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
 * Frame of the side menu: logo, app name and Settings at the top, the tab's [content] in the middle,
 * and the Home / Folders / Tags icons along the bottom with a tab-specific [trailing] action on the right.
 */
@Composable
fun NoteMenu(
    tab: MenuTab,
    onTab: (MenuTab) -> Unit,
    onSettings: () -> Unit,
    trailing: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.stackpad_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.menu_settings))
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabButton(Icons.Filled.Home, R.string.menu_home, tab == MenuTab.HOME) { onTab(MenuTab.HOME) }
                TabButton(Icons.Filled.Folder, R.string.menu_folders, tab == MenuTab.FOLDERS) { onTab(MenuTab.FOLDERS) }
                TabButton(Icons.Filled.Sell, R.string.menu_tags, tab == MenuTab.TAGS) { onTab(MenuTab.TAGS) }
                Spacer(Modifier.weight(1f))
                trailing()
            }
        }
    }
}

@Composable
private fun TabButton(icon: ImageVector, label: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) colors.secondaryContainer else Color.Transparent,
            contentColor = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        ),
    ) {
        Icon(icon, contentDescription = stringResource(label))
    }
}
