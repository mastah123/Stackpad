package com.stackpad.app.ui.navigation

import androidx.compose.runtime.Composable
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

private const val ROUTE_NOTES = "notes"
private const val ARG_NOTE_ID = "noteId"
private const val ROUTE_EDITOR = "editor/{$ARG_NOTE_ID}"

@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_NOTES) {
        composable(ROUTE_NOTES) {
            val viewModel: NoteListViewModel = viewModel(factory = NoteListViewModel.factory(container))
            NoteListScreen(
                viewModel = viewModel,
                onOpenNote = { id -> navController.navigate("editor/$id") },
                onNewNote = { navController.navigate("editor/${BlockFactory.newId()}") },
            )
        }
        composable(
            route = ROUTE_EDITOR,
            arguments = listOf(navArgument(ARG_NOTE_ID) { type = NavType.StringType }),
        ) { entry ->
            val noteId = entry.arguments?.getString(ARG_NOTE_ID) ?: return@composable
            val viewModel: NoteEditorViewModel = viewModel(
                key = noteId,
                factory = NoteEditorViewModel.factory(container, noteId),
            )
            NoteEditorScreen(
                viewModel = viewModel,
                audioPlayer = container.audioPlayer,
                media = container.mediaStorage,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
