package com.stackpad.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.stackpad.app.ui.components.LocalThumbnails
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stackpad.app.AppContainer
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.ui.editor.NoteEditorScreen
import com.stackpad.app.ui.editor.NoteEditorViewModel
import com.stackpad.app.ui.notes.NoteListScreen
import com.stackpad.app.ui.notes.NoteListViewModel
import com.stackpad.app.security.AppLockController
import com.stackpad.app.ui.settings.SettingsScreen
import com.stackpad.app.ui.settings.SettingsViewModel
import com.stackpad.app.ui.reminders.RemindersScreen
import com.stackpad.app.ui.reminders.RemindersViewModel
import com.stackpad.app.ui.trash.TrashScreen
import com.stackpad.app.ui.trash.TrashViewModel

private const val ROUTE_NOTES = "notes"
private const val ROUTE_TRASH = "trash"
private const val ROUTE_REMINDERS = "reminders"
private const val ROUTE_SETTINGS = "settings"
private const val ARG_NOTE_ID = "noteId"
private const val ARG_FOLDER_ID = "folderId"
private const val ROUTE_EDITOR = "editor/{$ARG_NOTE_ID}?$ARG_FOLDER_ID={$ARG_FOLDER_ID}"

@Composable
fun AppNavigation(
    container: AppContainer,
    appLock: AppLockController,
    noteToOpen: String? = null,
    onNoteOpened: () -> Unit = {},
) {
    val navController = rememberNavController()

    // A tapped reminder notification opens its note on top of the list.
    LaunchedEffect(noteToOpen) {
        noteToOpen?.let { id ->
            navController.navigate("editor/$id") { launchSingleTop = true }
            onNoteOpened()
        }
    }

    CompositionLocalProvider(LocalThumbnails provides container.thumbnailStore) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_NOTES,
        enterTransition = screenEnter,
        exitTransition = screenExit,
        popEnterTransition = screenPopEnter,
        popExitTransition = screenPopExit,
    ) {
        composable(ROUTE_NOTES) {
            val viewModel: NoteListViewModel = viewModel(factory = NoteListViewModel.factory(container))
            NoteListScreen(
                viewModel = viewModel,
                onOpenNote = { id -> navController.navigate("editor/$id") },
                onNewNote = { folderId ->
                    val id = BlockFactory.newId()
                    navController.navigate(
                        if (folderId == null) "editor/$id" else "editor/$id?$ARG_FOLDER_ID=${Uri.encode(folderId)}",
                    )
                },
                onOpenTrash = { navController.navigate(ROUTE_TRASH) },
                onOpenReminders = { navController.navigate(ROUTE_REMINDERS) },
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
            )
        }
        composable(ROUTE_SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
            SettingsScreen(viewModel = viewModel, appLock = appLock, onBack = { navController.popBackStack() })
        }
        composable(ROUTE_REMINDERS) {
            val viewModel: RemindersViewModel = viewModel(factory = RemindersViewModel.factory(container))
            RemindersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenNote = { id -> navController.navigate("editor/$id") },
            )
        }
        composable(ROUTE_TRASH) {
            val viewModel: TrashViewModel = viewModel(factory = TrashViewModel.factory(container))
            TrashScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = ROUTE_EDITOR,
            arguments = listOf(
                navArgument(ARG_NOTE_ID) { type = NavType.StringType },
                navArgument(ARG_FOLDER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val noteId = entry.arguments?.getString(ARG_NOTE_ID) ?: return@composable
            val folderId = entry.arguments?.getString(ARG_FOLDER_ID)
            val viewModel: NoteEditorViewModel = viewModel(
                key = noteId,
                factory = NoteEditorViewModel.factory(container, noteId, folderId),
            )
            NoteEditorScreen(
                viewModel = viewModel,
                audioPlayer = container.audioPlayer,
                media = container.mediaStorage,
                appLock = appLock,
                onBack = { navController.popBackStack() },
            )
        }
    }
    }
}
