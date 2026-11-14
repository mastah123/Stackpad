# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.8.0] - 2026-11-14

### Added
- **"+" chip** in the editor's add bar before Text. It opens a row with Image (gallery), Camera, Audio, Link, Table and Divider, which are no longer separate chips.
- **Tag chip** beside Cards. It opens a scrolling row of every tag you use: the note's tags are highlighted, tapping one adds or removes it, and "+" creates a new tag.
- **Share image and PDF**: the 3-dot menu now has one Share item with Copy, Text, Markdown, Image and PDF. Image is one tall PNG and PDF is paginated A4; both include pictures, tables (with calculated values) and checklists. A note too long for one image shows a message suggesting PDF.
- **Caret stays visible**: the text cursor scrolls into view when it would sit behind the add bar, an option row or the keyboard.

### Changed
- The editor's "Note options" popup is gone. **Color** is now in the 3-dot menu, and tags are managed with the Tag chip; the tags above the title are chips you tap to remove.
- The editor no longer has a folder choice (folders are managed from the note list; multi-select can still move notes).
- Select blocks mode shows a checkbox of one fixed size in its own column instead of an icon sized relative to the block.
- Share as Markdown, Share as plain text and Copy note moved into the Share submenu.

## [1.7.0] - 2026-11-10

### Added
- **Reorder blocks**: in the editor's Select blocks mode, new Move up and Move down buttons move the selected blocks one step (several blocks move together and keep their order). It can be undone with the undo button.
- **Better search**: every word you type must appear somewhere in the note (title, content or tags), in any order. Matches are highlighted in the list, and the preview shows the part of the note around the first match.
- **Search filters**: while searching, chips under the search box narrow results by folder, tag and color, with a Clear filters button. Color is a new filter.
- **Select several notes**: long-press a note in the list to start selecting, tap to add or remove more. The top bar offers Select all, Pin or Unpin, Move to trash (with one Undo for all), and a menu to move the selected notes to a folder or add a tag to all of them. Back or the close button leaves selection mode.

### Changed
- Searching no longer treats the whole query as one phrase; "milk buy" finds a note that says "buy some milk".
- Locked notes still hide their content from search; only their title can match.

## [1.6.0] - 2026-11-09

### Added
- **Select blocks**: choose "Select blocks" in the editor's 3-dot menu, tap blocks to select them (or Select all), and copy them as plain text. Back or the close button leaves selection mode.
- **Templates**: "Insert template" in the editor menu adds a template to the note (it replaces the note while it is still empty). Four are built in (Meeting notes, To-do list, Daily journal, Project plan). "Save as template" keeps the current note as your own template (up to 20); saved templates can be deleted from the picker. Images and audio are not saved in templates, and checklist items are saved unchecked without due dates.
- **Note lock**: "Lock note" in the editor menu makes a single note need your fingerprint, face or phone screen lock to open, even if app lock is off. Locked notes show a lock icon in the list with no preview, their content is left out of search, reminder notifications and the widget show no text from them, and the editor locks again after 30 seconds away. "Remove note lock" needs the screen lock too.

### Changed
- Database version 3 (adds the lock flag). Existing notes are migrated and stay unlocked.
- Backups now include the lock flag.
- New editor menu entries: Select blocks, Insert template, Save as template, Lock note.

## [1.5.0] - 2026-11-08

### Added
- **Cards** chip in the editor's add bar. It opens an option row with Prompt, Quote and Code, which moved out of the main row.
- **Copy note** in the editor menu: copies the whole note as plain text.
- **Checklist sort**: a button on the checklist moves done items to the bottom (order inside each group is kept). It can be undone and is disabled when nothing would change.
- A note's **tags** now show in a row above its title; tapping one opens the note options.
- **Folders view**: folders are shown as a small grid (All, No folder, each folder, Edit, New folder).
- **Tags view**: tags are shown in one horizontally scrolling row.
- The side menu now shows the real Stackpad logo (new PNG).

### Changed
- The note list title for the "All" view is now **All notes**.
- The **Pinned** view shows only pinned notes, with no folder or tag bars. The **All notes** view has no bars either; folders and tags have their own views in the side menu.
- The side menu is thinner (72dp).
- **Note options** (folder, tags, color) moved from their own button and bottom sheet into the editor's 3-dot menu, as a small popup directly below the dots.
- While the List, Image or Cards option row is open, or while recording, the text formatting row is hidden.

### Removed
- The folder and tag picker sheets of the side menu, replaced by the views above.

## [1.4.0] - 2026-11-08

### Added
- **Share a note** as Markdown or plain text from the new menu in the editor. Headings, bold, italic, lists, checklists (with due dates), tables (formulas calculated), links, quotes, code and dividers are all carried over. Images are shown as a placeholder with their caption.
- **Backup and restore** in the new Settings screen. Export saves every note (trash included), folders, images and audio to a zip file you choose. Restore merges a backup: missing notes are added, a note is replaced only if the backup copy is newer, and nothing is ever deleted. Restoring checks the file first (size limits, safe file names) and reports what was added, updated and skipped.
- **App lock**: ask for your fingerprint, face or phone screen lock when Stackpad opens and after 30 seconds in the background. Turning it on or off requires unlocking. While it is on, Stackpad is hidden in the recent-apps screen and screenshots are blocked.
- **Home-screen widget** with a new-note button and your most recently edited note (tap to open). It shows "Locked" instead of the title while app lock is on.
- **Side menu** in the note list. The top-left title is now a menu button that opens a thin side bar with the logo and shortcuts for All notes, Pinned, Folders, Tags, Trash and Settings. Folders and Tags open a picker; Pinned shows only pinned notes. The shortcut for the current view is highlighted.
- The top bar now shows the current view (Notes, Pinned, a folder name, or a `#tag`).
- Settings screen with backup, app lock and the app version.

### Changed
- The Trash button moved from the top bar into the side menu. The editor's delete button moved into its new overflow menu.
- A launch from a reminder or the widget is handled only once, so rotating the phone no longer reopens the note.
- New library: AndroidX Biometric (app lock).

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

[Unreleased]: https://github.com/mastah123/stackpad/compare/v1.7.0...HEAD
[1.7.0]: https://github.com/mastah123/stackpad/compare/v1.6.0...v1.7.0
[1.6.0]: https://github.com/mastah123/stackpad/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/mastah123/stackpad/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/mastah123/stackpad/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/mastah123/stackpad/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/mastah123/stackpad/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/mastah123/stackpad/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/mastah123/stackpad/releases/tag/v1.0.0
