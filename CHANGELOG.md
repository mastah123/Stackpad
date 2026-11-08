# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Side menu** in the note list. The top-left title is now a menu button that opens a thin side bar with the logo and shortcuts for All notes, Pinned, Folders, Tags and Trash. Folders and Tags open a picker; Pinned shows only pinned notes. The shortcut for the current view is highlighted.
- The top bar now shows the current view (Notes, Pinned, a folder name, or a `#tag`).

### Changed
- The Trash button moved from the top bar into the side menu.

## [1.3.0] - 2026-11-08

### Added
- **Table functions**: `SUM`, `AVERAGE`, `MIN`, `MAX` and `COUNT`, over single cells, ranges such as `A1:A5` or `A1:B4`, and expressions, for example `=SUM(A1:A4, 5, B1*2)`. Blank and text cells inside a range are skipped. New error `#NAME?` for unknown functions.
- **Audio seek and speed**: tap or drag the waveform to move the playhead, and tap the speed chip to cycle 1x, 1.5x and 2x.
- **Checklist due dates**: set a date on any checklist item with the calendar button. Overdue unchecked items are highlighted. A **reminder notification** is shown at 9:00 on the due day, and tapping it opens the note. Reminders are removed when an item is checked, loses its date, or its note is trashed, and are restored with the note. They are recreated after a restart or an app update.
- **Image zoom**: tap an image to view it full screen. Pinch to zoom, drag to pan, double-tap to zoom in and out.
- **Image captions**: add a caption under any image with the pencil button. Captions are searchable.
- Notification permission is requested the first time a due date is set (Android 13 and later).

### Changed
- The "x" and pencil buttons on images sit in opposite corners of the image.
- Notes from 1.2.0 open unchanged: new fields (due date, caption) default to empty, and no database upgrade is needed.

## [1.2.0] - 2026-11-07

### Added
- **Undo and redo** in the editor for typing, formatting, and adding, removing or editing blocks. Typing is grouped, so one undo reverts a burst of typing. Removed images and audio can be brought back.
- **Quote**, **Code** and **Divider** blocks. Code is monospace, is not auto-capitalized, and has a copy button.
- **Folders**: create, rename and delete folders, file a note in one folder, and filter the note list by folder or by "No folder". Deleting a folder keeps its notes.
- **Tags**: add up to 10 tags per note (with suggestions from other notes) and filter the note list by tag. Tags are searchable.
- **Note colors**: eight colors, shown as a tint on the note card.
- **Trash**: deleting a note moves it to the trash, with an Undo option in the list. Restore it, delete it forever, or empty the trash. Notes are erased automatically after 30 days.
- **Sort** the note list by last edited, date created or title. The choice is remembered. Pinned notes stay on top.
- A "Folder, tags and color" sheet in the editor.

### Changed
- **List** and **Image** now open an option row above the add bar, styled like the text format toolbar, instead of a drop-down menu. The chip is highlighted while its row is open.
- Tapping a link in text now opens it. The "Open link" button was removed.
- The delete buttons in the note list and editor now move the note to the trash instead of deleting it immediately.
- The copy message now reads "Copied to clipboard." because it also applies to code blocks.
- Database upgraded to version 2 with a migration. Existing notes are kept unchanged.

### Fixed
- Images and audio left behind by removed blocks are cleaned up when a note is closed and at app start.

## [1.1.0] - 2026-11-07

### Added
- **List** button in the add bar. One menu now offers Bullets, Numbers, Letters and Checklist.
- Bold, italic and underline can be switched on at the caret and apply to the text typed next. New text also continues the style of the character before it.
- Backspace at the start of a line merges it into the line above.
- Sentence auto-capitalization in the title, text, list, checklist, link title and prompt fields.

### Changed
- Every line of text is now its own block. Enter starts a new line, so H1, H2 and H3 apply to one line at a time. Notes from 1.0.0 that contain multi-line text are split automatically when opened.
- The separate Bullets, Numbers, Letters and Checklist chips were replaced by the single List chip.
- Format buttons stay enabled when nothing is selected.

### Fixed
- Bold, italic and underline did nothing unless text was selected, and the buttons were disabled without a selection.

## [1.0.0] - 2026-11-06

### Added
- Block-based note editor with compact, chat-style spacing.
- Text blocks with H1/H2/H3 headings and bold, italic and underline formatting.
- Automatic highlighting of URLs in text, with an "Open link" action.
- Bullet, numbered and lettered lists with hanging-indent wrapping.
- Checklist blocks with progress tracker, rounded corners, drag-to-reorder handles and an "x" to remove.
- Full-width image blocks from the gallery or camera, with an "x" to remove.
- Table blocks with `+ - * /`, parentheses and cell references (`=A1+B1*2`), error codes, and add/remove rows and columns.
- In-app audio recording with live waveform, and voice-message style playback (play/pause, animated waveform, duration, "x").
- Link blocks with URL validation that open in the browser.
- Prompt blocks with a copy button.
- Note list with search, pinning and delete with confirmation.
- Autosave and local-only storage (Room), with images and audio stored as private files.
- Light and dark themes that follow the system by default, with a manual override.
- Adaptive app icon.
- GitHub Actions workflow for tests, debug builds and signed release builds using GitHub Secrets.
- Unit tests for formulas, span editing, URLs, list markers, table operations, waveform helpers and serialization.

[Unreleased]: https://github.com/mastah123/stackpad/compare/v1.3.0...HEAD
[1.3.0]: https://github.com/mastah123/stackpad/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/mastah123/stackpad/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/mastah123/stackpad/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/mastah123/stackpad/releases/tag/v1.0.0
