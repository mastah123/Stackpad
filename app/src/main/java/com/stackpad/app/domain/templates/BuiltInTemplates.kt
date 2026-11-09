package com.stackpad.app.domain.templates

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.BlockFactory
import com.stackpad.app.data.model.ChecklistBlock
import com.stackpad.app.data.model.ChecklistItem
import com.stackpad.app.data.model.ListBlock
import com.stackpad.app.data.model.ListItem
import com.stackpad.app.data.model.ListKind
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.data.model.TextLevel

/** The templates that ship with the app. Ids are stable; block ids are replaced when a template is applied. */
object BuiltInTemplates {
    private fun heading(text: String) = TextBlock(id = BlockFactory.newId(), level = TextLevel.H2, text = text)
    private fun body() = BlockFactory.text()
    private fun bullets(count: Int): ListBlock =
        ListBlock(BlockFactory.newId(), ListKind.BULLET, List(count) { ListItem(BlockFactory.newId()) })
    private fun checklist(title: String, count: Int): ChecklistBlock =
        ChecklistBlock(BlockFactory.newId(), title, List(count) { ChecklistItem(BlockFactory.newId()) })

    fun all(): List<NoteTemplate> = listOf(
        NoteTemplate(
            id = "builtin-meeting",
            name = "Meeting notes",
            title = "Meeting notes",
            blocks = listOf<Block>(
                heading("Date and attendees"), body(),
                heading("Agenda"), bullets(3),
                heading("Notes"), body(),
                heading("Action items"), checklist("", 3),
            ),
            builtIn = true,
        ),
        NoteTemplate(
            id = "builtin-todo",
            name = "To-do list",
            title = "To-do",
            blocks = listOf<Block>(checklist("Today", 5), checklist("Later", 3)),
            builtIn = true,
        ),
        NoteTemplate(
            id = "builtin-journal",
            name = "Daily journal",
            title = "Journal",
            blocks = listOf<Block>(
                heading("Today"), body(),
                heading("Grateful for"), bullets(3),
                heading("Tomorrow"), body(),
            ),
            builtIn = true,
        ),
        NoteTemplate(
            id = "builtin-project",
            name = "Project plan",
            title = "Project plan",
            blocks = listOf<Block>(
                heading("Goal"), body(),
                heading("Milestones"), checklist("", 4),
                heading("Risks"), bullets(2),
                heading("Links"), bullets(2),
            ),
            builtIn = true,
        ),
    )
}
