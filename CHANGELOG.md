# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.22.1] - 2026-11-25

### Changed
- The Text chip now works like the other chips: tap it to highlight it and open its row (H1, H2, H3, bold, italic, underline), tap it again to hide the row. It no longer adds a text block.
- Tapping the space just below a card (table, checklist, image...) puts the cursor in the text under it, adding a blank text block there if the next block is not text. Tapping the empty space under the last block puts the cursor at the end of the note.
- The Format chip holds only Font, Size, Highlight and Line height (the Styles button is gone).

## [2.22.0] - 2026-11-25

### Added
- Format chip between Text and List. It opens Styles (headings, bold, italic, underline) and the drop-downs Font, Size, Highlight and Line height, which change the plain text block you are editing: font (Default, Serif, Monospace, Handwriting), size (Small to Huge), highlight color (six colors) and line spacing (Tight to Loose). Headings inside the block keep their own size. The choices are saved with the note.
- The note options sheet now also opens from the 3 dots on a note in Home, folders, tags and search. There it offers Pin, Archive, Duplicate, Add to home, Move to folder and Move to trash, plus Add tag and Add color. Lock, History, Copy, Share, Export and Select blocks stay in the editor's sheet.

### Changed
- The note options sheet opens fully expanded.
- The text style toolbar (H1, H2, H3, bold, italic, underline) is hidden until you open it from the Text chip.
- Colors: buttons, icons, links and the cursor are navy instead of coral. Every selected item (menu rows, chips, toolbar buttons, selected blocks) uses the soft pink pill from the menu.
- Quote, Prompt and Code blocks use new colors: cream, lavender and dark navy, in light and dark themes.

### Removed
- Templates (Insert template, Save as template and the built-in templates). Templates you had saved are no longer shown.

## [2.21.0] - 2026-11-25

### Changed
- New look, same layout: Plus Jakarta Sans replaces Inter for all text (with a real italic), the Material icons are replaced by thin rounded Lucide line icons, and the colors move to a warm off-white and navy light theme with a coral accent and lavender and green secondary hues. Dark theme uses the same hues on deep navy. The window background, the widget and the note colors follow.
- Note colors are softer (coral, orange, yellow, green, teal, blue, purple, pink); notes keep the color index they had.

### Removed
- Inter font file; the Inter license is replaced by the Plus Jakarta Sans and Lucide licenses in `docs/licenses/`.

## [2.20.0] - 2026-11-25

### Added
- Note options sheet: the editor's 3-dot menu now opens a bottom sheet with the note's title, tags, Created at and Last edited at dates, **Add tag** and **Add color**, and a grid of actions: Pin, Archive, Lock, History, Copy, Share, Export, Duplicate, Add to home, Select blocks, Templates, Save template and Move to trash. Export opens a second page with Text, Markdown, Image, PDF and Share options.
- Archive: archived notes leave the main list, search and tags but are not deleted. They are listed under **Archive** in the side menu's Home tab (with a count), where a note can be opened or unarchived.
- Duplicate: saves a copy titled "... (copy)" that is not pinned or archived. The copy shares its pictures and recordings with the original.
- Add to home: asks your launcher to pin a shortcut that opens the note (only shown when the launcher supports it).

### Changed
- Database version 5 (adds the archived flag); existing notes are not archived.
- Erasing a note no longer deletes pictures or recordings that another note still uses.
- Backups keep the archived flag.

## [2.19.0] - 2026-11-25

### Added
- Table handles: while you type in a top-row cell a small handle sits on the right border of that column, and in a left-most cell a handle sits on the bottom border of that row (cell A1 shows both). The handles float over the table and take no space. Tap one for a menu: insert left/right (above/below), move, clear or delete the column (row). Drag the column handle sideways or the row handle up and down to resize.
- Column widths and row heights are saved with the table.
- Formulas follow their cells when rows or columns are inserted, moved or deleted. A formula that pointed at a deleted cell shows `#REF!`.

### Changed
- The "+ Row", "− Row", "+ Col" and "− Col" buttons in the table header are replaced by the handles. To add a row or column at the end, use "Insert below" or "Insert right" on the last one.
- Importing a CSV resets column widths and row heights.

## [2.18.0] - 2026-11-25

### Added
- Checkbox list: a simple list with a tick box on each item. Tap the box to check it (the text is struck through); Enter adds a new item and Backspace on an empty item removes it. Exports as `- [ ]` / `- [x]` in Markdown and `[ ]` / `[x]` in text, PDF and Word.

### Changed
- The earlier checklist (due dates, reminders, progress, sorting) is now called **Task list**. In the List row, Checklist opens a third row with Checkbox list and Task list.
- Reminders and related texts say "task list" instead of "checklist".

## [2.17.0] - 2026-11-25

### Added
- Sort by / Group by sheet (opened from the sort icon): date edited, date created or title with a direction button (New - old / Old - new, A - Z / Z - A), and grouping by default (Pinned/Recent), none, Abc, year, week or month. The choice is remembered.

### Changed
- Editing a picture always starts from the untouched original, so you can crop it again from scratch (the edited picture is still what the note shows).
- More space between the note info line and the first text.
- The "Text options" chip was removed; the add bar chips are back to Add, Text, List and Cards.
- "Last edited" is now called "Date edited".

## [2.16.0] - 2026-11-24

### Added
- Letters is back in the List row (a. A. α.); Numbers now offers 1. I. i.
- Add > Image opens a third row with Gallery and Photo.
- A first chip in the add bar shows or hides the text options (H1, H2, H3, bold, italic, underline).

### Changed
- Inter is used for all text; Fraunces was removed.
- Selected buttons in the add bar and text toolbar are now filled with the accent color so they stand out.

### Fixed
- Bullet styles are drawn as shapes of the same size, centered on the first line; the hollow bullet no longer looks like a "C".
- Roman and Greek lists no longer indent: the marker column fits the widest marker.

## [2.15.0] - 2026-11-24

### Changed
- New typography: Inter is used for all text in the app. The font is bundled (about 0.9 MB added to the app) and licensed under the SIL Open Font License (`docs/licenses/`).

## [2.14.0] - 2026-11-24

### Added
- List styles: choosing Bullets or Numbers in the List row opens a third row of styles above it (round, hollow and square bullets; 1. A. a. I. i. and Greek α. numbering).
- Add tag chip above the note title that opens a pop-up to add or remove tags, or create a new one.

### Changed
- The tags row above the title is always shown (it was hidden when the note had no tags); the Tag button moved out of the add-block bar.
- The separate "Letters" list button is replaced by the Numbers styles.
- Markdown export writes every bullet style as "-".

## [2.13.0] - 2026-11-24

### Added
- Notes without a title get one from the date and time they were created, e.g. "Note 24-11-2026 04:09 PM" (saved with the note; older untitled notes show the same name).
- Reminders in the side menu's Home tab, with a count: a screen listing every unchecked checklist item that has a due date, overdue first. Tap one to open its note.

### Changed
- List layout: rounded cards like the grid, two lines of text preview only (no pictures), more space under the title.

### Fixed
- The search bar and menu button on Home were drawn behind the status bar.

## [2.12.0] - 2026-11-24

### Added
- Home: rounded search bar with the menu button inside, PINNED and RECENT sections, and the layout and sort icons on the first section header.
- Empty Home shows a rotating tip and a "Create a new note" button.
- Three-dot menu on every note: Pin, Move to folder, Add tag, Move to trash.
- Smooth motion: spring menu slide that follows the finger (with a light dim on the visible notes), animated note add/remove/move, cross-fade between list and grid and between empty and filled lists, animated selection color, scaling new-note button, slide and fade between screens, animated Trash list.

### Changed
- Note list rows are flat with the time, a bold title, a preview, outlined tag chips and a left accent bar for pinned or colored notes; the grid uses the same content in rounded cards.
- Folder and tag views use a top bar with Search, layout and sort icons; folders add a three-dot menu with Rename and Delete.
- The pin button on a note moved into its three-dot menu (a pin icon still marks pinned notes).

## [2.11.0] - 2026-11-24

### Changed
- New side menu: it slides in at 80% of the screen width and pushes the notes to the right instead of covering them. Tap the visible notes, press back or swipe left to close; swipe right to open.
- The menu has Home, Folders and Tags tabs (icons along the bottom). Home lists All notes, Pinned and Trash with counts; Folders lists No folder and your folders with a + button; Tags lists tags with note counts, a "Filter tags" box and a sort button (name or most notes).
- Choosing a folder or tag shows its notes and closes the menu. Settings moved to a gear in the menu header.
- The theme button moved from the top bar to the Home tab's bottom bar.

### Removed
- The full-width Folders grid and Tags row above the note list (replaced by the menu tabs).

## [2.10.0] - 2026-11-24

### Added
- **Mark up** tab in the picture editor: pen, highlighter, blur, pixelate and text, with colours and three sizes. Undo and "remove all marks" are included. Cropping or rotating after marking removes the marks (a message says so), because they are placed on the cropped picture.
- **Colour looks** in the Adjust tab: Original, Mono, Sepia, Vivid, Cool, Warm.
- **Saved size**: Original, Large (2048 px), Medium (1280 px) or Small (800 px) with matching JPEG compression. Pictures are never enlarged.
- **Copy text from picture** (picture menu): reads the text on the phone and adds it below the picture. Uses Google ML Kit text recognition (Latin alphabet, adds about 5 MB to the app).

### Changed
- The picture menu now always opens, with Edit picture, Copy text from picture and (after an edit) Restore original.

## [2.9.0] - 2026-11-23

### Added
- **Version history** in the note menu: earlier versions of a note (one about every 10 minutes of editing, newest 20 kept), with a preview and **Restore this version**. Restoring keeps the current text as a version and can be undone. Deleting a note deletes its history; pictures in older versions are kept until those versions are trimmed.
- **Quick Settings tile "New note"**: opens Stackpad on a blank note in one tap.

### Changed
- The database moves to version 4 (a new table for versions). Existing notes keep all data and start with no history. History is not included in backups.

## [2.8.0] - 2026-11-23

### Changed
- **Plain text is now one block per run of text**, not one block per line. Enter adds a line inside the same block, so many lines can be selected and copied at once. A table, list, checklist, picture or card ends a run (text, table, text is three blocks).
- **Headings are per line**: H1, H2 and H3 apply to the lines the cursor or selection touches, and tapping the active heading again turns it off. On an empty line the choice applies to what you type next.
- Existing notes are joined automatically when opened (neighbouring text blocks become one; old heading levels become line headings). Nothing is lost, and the change is saved with the next edit.
- Adding a block (table, list, picture...) with the cursor inside text splits the text at the cursor. Removing a block between two texts joins them.
- Share, PDF/image export, Markdown/text export and note previews show each line as before.

### Added
- **Select everything between** in block selection: selects all blocks from the first to the last one you picked, for copying text and tables together.

## [2.7.0] - 2026-11-23

### Added
- **Share into Stackpad**: text, links and pictures shared from other apps become a new note that opens right away. A line that is only a web address becomes a link card.
- **Export all notes as Markdown** in Settings: zip with one `.md` per note, a folder per Stackpad folder, and a `media/` folder with pictures. Tags are added as `#tag` at the end of each note. Locked and trashed notes are skipped.
- **Automatic backup** in Settings: choose a folder and a daily or weekly schedule, with "Back up now" and the last result shown. Keeps the 7 newest automatic backups.

### Changed
- Added the AndroidX WorkManager library for the scheduled backup.

## [2.6.0] - 2026-11-23

### Added
- **Checklist menu** (three dots on the card):
  - **Auto-sort done items**: ticking an item slides it to the bottom with a short animation; new items are added above the done ones.
  - **Hide completed**: done items fold away and a "N done · Show" line brings them back. They stay in the note.
  - **Sort by due date**: soonest first, undated items next, done items last.
  - **Uncheck all** and **Delete completed**, each its own undo step with an Undo button in the message.
- A **percentage** next to the done count (for example `2/5 · 40%`).
- **Table CSV**: import a CSV file (comma, semicolon or tab separated; quoted fields and line breaks inside quotes are supported; up to 50 rows and 12 columns) and export the table as a CSV you can share.

### Changed
- The checklist sort button moved into the checklist menu as "Done items to the bottom".

## [2.5.0] - 2026-11-18

### Added
- **Share options** for image and PDF, in a sheet with three collapsible sections and one tick per choice:
  - **Look:** page Light, Dark or Match app; note color on the page; text size (small, medium, large); include pictures; hide completed tasks; PDF page size (A4 or Letter); PDF page numbers.
  - **Clean up:** empty lines, empty list items, empty checklist items and checklists, empty table rows and tables, empty quote/prompt/code/link cards.
  - **Details:** title, word count, dates (created and edited), tags, and a "Made with Stackpad" footer.
  - The sheet opens before sharing. **Don't show this again** makes later shares use the saved choices; **Share > Share options** (below PDF) opens it again. Choices are remembered.

### Changed
- **Image and PDF copy the note's layout:** the app's text and heading sizes, the list marker column (right-aligned markers, long items wrap under the text), full-width pictures with rounded corners (small pictures are enlarged), checklist cards with the done count and progress bar. No buttons, "x" or drag handles appear.
- Empty lines, empty list items and empty cards are now kept by default, as in the note; use the Clean up ticks to remove them.
- The image chips are now **Gallery** and **Photo**.

### Removed
- **Image captions.** The pencil on an image now opens the picture editor (or, after an edit, a menu with Edit picture and Restore original). Old captions are no longer shown, exported or searched; their text stays in the data.

### Fixed
- Pictures in exported images and PDFs are upright for photos taken with the camera turned (rotation is respected).

## [2.4.0] - 2026-11-17

### Added
- **Picture editor.** The pencil on an image now opens a menu with **Edit picture**; the full-screen viewer has an edit button too.
  - **Crop** with draggable corners and edges, a thirds grid, and shapes: Free, Original, 1:1, 4:3, 3:4, 16:9, 9:16 (fixed shapes resize by the corners).
  - **Rotate** left and right, **flip** horizontally and vertically, and **straighten** from -45° to 45° (the picture is cut so there are no empty corners).
  - **Undo** each step and **Reset** to the opened picture. Closing with unsaved edits asks first.
  - **Save** writes the result as a new JPEG in the note. The first time, the untouched picture is kept, and **Restore original** in the image menu brings it back. Backups include the original too.

### Changed
- Pictures in thumbnails and the editor share one decoder that respects the photo's rotation.

## [2.3.0] - 2026-11-16

### Added
- **More imports** (Settings > Import). You can pick several files at once:
  - **Markdown and text**: `.md` and `.txt` files, or a `.zip` of them (Obsidian vaults and Notion Markdown exports work). Headings, bold, italic, underline, lists, tasks, quotes, code, rules and tables are kept; front matter gives the title and tags; pictures are imported when they are inside the zip (a loose file's picture links cannot be reached and are counted as skipped).
  - **Evernote**: `.enex` files. Text, checkboxes, lists, tables, tags, dates and pictures are kept; other attachments are counted as skipped.
  - **Word**: `.docx` files. Headings, bold, italic, underline, bullet and numbered lists, tables and pictures are kept.
  - **PDF**: text is imported as headings, paragraphs and lists (repeated page headers and page numbers are dropped). A PDF without text, such as a scan, becomes a note with its pages as pictures (first 50 pages).
- Google Keep import accepts several Takeout zips at once.

### Changed
- Import messages no longer mention Google Keep only; the error texts are generic.
- The app is about 5 MB larger because of the PDF text library (PdfBox-Android).

## [2.2.0] - 2026-11-16

### Added
- **Import from Google Keep** (Settings > Import > Google Keep): choose a Google Takeout zip. Text notes, list notes (as checklists), labels (as tags), colors, pinned state, links, pictures and the original created and edited dates are imported. Notes in Keep's trash are skipped, and so are audio and drawing attachments (counted in the result message).
- Importing the same zip twice never duplicates notes or overwrites later edits.
- The importer is built so other sources (Markdown, plain text, other apps) can be added later.

## [2.1.0] - 2026-11-15

### Added
- **Formatted note previews** in the list and grid: the first blocks of each note appear as they look in the editor, with headings, bold/italic/underline, list markers, checklists with boxes, quotes, code, prompts, links, tables (first rows), audio and a picture. A fade marks where a preview is cut off. The grid shows taller previews than the list.
- **Picture thumbnails**: previews use small JPEG copies (480 px, kept in the cache folder and re-made if the system clears them), so lists with many photos stay smooth.

### Changed
- While searching, cards still show a plain text snippet with the matches highlighted. Locked notes show no preview.

## [2.0.0] - 2026-11-15

### Changed
- **Share, Image** now saves the picture to the gallery (Pictures/Stackpad), shows a confirmation, and then opens the share sheet. On Android 9 and older the app asks for storage permission first; if you decline, the image is still shared.
- **Share, PDF** now opens the system file picker to choose where to save the PDF, then opens the share sheet.
- Exported images and PDFs match the app's look: cards use the block colors (checklist green, table teal, link blue, prompt purple, quote amber, code slate, audio rose), checklists have real boxes with ticks, tables have a tinted header and grid, and the note's color tints the page. Exports are always on a light page so they print and read well.

## [1.9.0] - 2026-11-15

### Added
- **List / Grid** button in the note list's top bar. Grid shows two columns of cards at their natural height, with longer previews. The choice is remembered.
- **Info line** under the editor title: word count and the date and time the note was created, small and faded.
- **Block colors by function**: checklist green, table teal, link blue, prompt purple, quote amber, code slate, audio rose.
- A note created while a folder is selected starts in that folder.

### Changed
- Folder **rename** and **delete** are now icons next to the sort button while a folder is selected; the "Edit" tile is gone.

### Removed
- The delete button on note cards. Use long-press to select notes and move them to the trash (with Undo), or delete from inside the note.

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
