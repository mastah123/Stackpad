# Testing

## Automated

```bash
./gradlew testDebugUnitTest
```

Covers: formula evaluator, span editing, URL detection/validation, list markers, table operations, waveform helpers, block JSON serialization, table functions, due dates and reminder planning, image zoom limits, audio speed, note sorting and filtering, tags, trash timing, and undo history, word counting, note previews, picture editing maths (crop, rotate, flip, straighten, undo), Keep import rules, Markdown, Evernote, Word and PDF import parsing, note export, and the image/PDF layout and pagination.

## Manual checklist (run on a device or emulator, Android 8.0+)

### Notes
- [ ] Create a note, type a title and some text, press back. It appears in the list.
- [ ] Create a note and leave it empty. It is **not** saved.
- [ ] Reopen a note. Content is exactly as left.
- [ ] Search by a word from the title, then by a word from the body. Matching notes show; clearing search shows all.
- [ ] Search for `%` or `_`. It matches literally (no wildcard behaviour).
- [ ] Pin a note from the list and from the editor. Pinned notes move to the top under "Pinned".
- [ ] Upgrade from 1.1.0 (install over it): every existing note is still there, unchanged, with no folder, tags or color.

### Text
- [ ] Type a line, press Enter, type another. Each line is its own block.
- [ ] Make line 1 H1, line 2 H2, line 3 H3 and line 4 normal. Each keeps its own size; changing one does not change the others.
- [ ] Tapping the active heading button again returns that line to normal.
- [ ] Select a word and tap bold, italic, underline. Buttons light up when the selection has the style; tapping again removes it.
- [ ] With only the cursor (nothing selected), tap Bold, then type. The new text is bold. Tap Bold again and type. The text is normal.
- [ ] Type at the end of a bold word. New text stays bold; move the cursor elsewhere and the style choice resets.
- [ ] Paste several lines. They become several blocks.
- [ ] Press Backspace at the start of a line. It merges into the line above, keeping the text and formatting.
- [ ] Start a new sentence in the title, a text line, a list item, a checklist item and a prompt. The first letter is capitalized by the keyboard (if your keyboard has auto-capitalization on).
- [ ] The search box, URL field and table cells do **not** auto-capitalize.
- [ ] Type before, inside and after a styled word. Styling stays on the right characters.
- [ ] Type `https://example.com` or `www.example.com`. It is highlighted. Tapping it opens the browser. Tapping plain text next to it just places the cursor. A long press on a link selects text and does not open it.
- [ ] Press Backspace in an empty text line. It is removed and focus moves up.

### Undo and redo
- [ ] Type a few words, tap Undo. The burst of typing is removed in one step; Redo brings it back.
- [ ] Undo/Redo are disabled when there is nothing to undo/redo.
- [ ] Add a block, remove a block, change a heading level: each can be undone and redone.
- [ ] Remove an image or an audio block, then Undo. The image or audio comes back and still plays.
- [ ] Remove an image, leave the note, reopen it. The image is gone for good.
- [ ] Undo then type something new. Redo becomes disabled.
- [ ] Undo and redo with a checklist, a list and a table in the note. They show the restored content correctly.

### Quote, code and divider
- [ ] Add a Quote: it is italic with an accent bar, and the "x" removes it.
- [ ] Add a Code block: monospace, no auto-capitalization. Copy puts the text on the clipboard and shows "Copied to clipboard."; the "x" removes it.
- [ ] Add a Divider: a thin line appears and the "x" removes it. A note that only has a divider is saved.
- [ ] Quote and code text is found by search.

### List and grid, cards, info line
- [ ] Note list top bar: the layout button switches between list and a two-column grid. Cards in the grid have different heights (longer previews). Pinned and Notes labels span both columns. Selection by long-press, pin and opening work in both. The choice survives restarting the app.
- [ ] Note cards have a pin button but no delete button. Long-press, select, and the trash icon in the selection bar still move notes to the trash with Undo.
- [ ] Editor: under the title a small faded line shows "N words · Created <date, time>". Typing updates the count; "1 word" is singular. Old notes show their original creation date.
- [ ] Block colors: checklist green, table teal, link blue, prompt purple, quote amber, code slate, audio rose. Check both light and dark themes: text stays readable.

### More imports (Markdown, text, Evernote, Word, PDF)
- [ ] Settings > Import > Markdown and text: pick two `.md` files and a `.txt` file at once. Three notes appear: `# Title` or front matter title is the note title (a `.txt` uses the file name); headings, bold/italic, bullet/numbered lists, `- [x]` tasks, quotes, code blocks, rules and tables look right.
- [ ] Pick a `.zip` of Markdown with an `images` folder (Obsidian or Notion export): notes import and their pictures show. Names with spaces or `%20` work.
- [ ] Pick a loose `.md` whose pictures are local files: the note imports and the message counts the pictures as skipped.
- [ ] Import the same files again: "0 imported, N already imported". Change a file's text and import it: it is added as a new note.
- [ ] Evernote: export a notebook as `.enex`, pick it. Checkboxes keep their ticks, lists, tables, tags and dates carry over, pictures show; PDFs or audio attached in Evernote are counted as skipped.
- [ ] Word: pick a `.docx` with headings, bold text, a bullet list, a numbered list, a table and a picture. All appear in order. A damaged `.docx` is counted as "could not be read" and the others still import.
- [ ] PDF with text (an article or a letter): headings, paragraphs and lists are readable; page numbers and repeated headers are not there; a word split by a hyphen at a line end is joined. A multi-column or table-heavy PDF may come out in a mixed order (known limit).
- [ ] PDF without text (a scan): a note with one picture per page. A PDF with more than 50 pages is cut at 50 and the message says how many were left out.
- [ ] A password-protected or damaged PDF is counted as "could not be read" and nothing crashes.
- [ ] Pick a file of the wrong type (for example a photo) for any row: "No notes were found in the chosen files".
- [ ] Release build: PDF import still works (it uses a library that minification must not break).

### Google Keep import
- [ ] Export Keep from takeout.google.com (only Keep) and copy the zip to the phone. Settings > Import > Google Keep > pick the zip. A message says how many notes and pictures were imported.
- [ ] Text notes keep title and text; list notes become a checklist with the same ticks; labels become tags; colors map (red, orange, yellow, green, teal, blue/cerulean, purple, pink; others have no color); pinned notes are pinned; links appear as link blocks; photos appear at the top of the note.
- [ ] Dates: sort by last edited and open a note's info line: they show Keep's original dates, not today.
- [ ] Import the same zip again: "0 imported, N already imported"; nothing is duplicated and edits you made after the first import stay.
- [ ] Notes in Keep's trash, audio notes and drawings are skipped and counted in the message. Archived notes are imported.
- [ ] Choose a non-zip file (a photo, a Stackpad backup): a clear error and no new notes. Choose a Stackpad backup zip: "No notes were found in the chosen files".
- [ ] Imported notes with photos show thumbnails in the list and open normally; deleting an imported note leaves no stray files.

### Note previews
- [ ] A note with a heading, bold text, a bullet list, a checklist, a quote, code, a prompt, a link, a table, an audio clip and a picture shows each of them in its own style on its card (checklist green with boxes, table teal grid, quote amber bar, code slate, prompt purple, link blue, audio rose pill). Done checklist items are struck through.
- [ ] Long notes are cut off with a fade at the bottom; grid cards show more than list cards. Very long tables show only the first rows, long checklists the first four tasks.
- [ ] A note with a photo shows a small cropped picture; scroll quickly through 50+ notes with photos: no stutter, and pictures appear within a moment. The first time is slower (thumbnails are made), later times are instant.
- [ ] Rotated camera photos appear upright in the preview.
- [ ] Edit a note and go back: its preview updates. Locked notes show only the lock icon and title. While searching, cards show the highlighted text snippet instead.

### Folders, tags and colors
- [ ] The 3-dot menu has no Note options and no folder choice. Color opens in place below the dots, with a Back row; picking a color applies it and closes the menu.
- [ ] Side menu, Folders: a small grid of tiles (All, No folder, each folder, New folder) appears above the notes, with no tag row. Selecting a tile filters the list and the title shows the folder name.
- [ ] With a folder selected, Rename and Delete icons appear in the top bar next to the sort button (there is no Edit tile). Rename changes the title; Delete asks first, and afterwards its notes remain under "No folder". The icons are gone when no folder is selected.
- [ ] With a folder selected, tap the + button and type something: the new note is in that folder (check in the list). Leaving the new note empty saves nothing. With All or No folder selected, new notes have no folder.
- [ ] With many folders the grid scrolls on its own without pushing the notes off screen.
- [ ] Add tags to a note (type and press Done or Add; also try `#Work` and `Two Words`). They appear lowercase as `#work` and `#two-words`. Adding the same tag again does nothing. The 11th tag cannot be added. Tapping a tag above the title (its X) removes it.
- [ ] A note with tags shows them as chips in a row above its title; tapping one removes it.
- [ ] Tag chip in the add bar: opens a row with "+" and every tag in use. The note's tags are highlighted; tapping a tag adds or removes it; "+" asks for a name and adds the tag. The row stays open after each tap.
- [ ] Side menu, Tags: tags appear in one horizontal scrolling row with counts, and no folder grid. Selecting one filters the list and the title shows `#tag`; selecting it again clears it.
- [ ] Searching for a tag name finds the note.
- [ ] Pick each color. The note card in the list is tinted; "No color" removes the tint. Colors look right in light and dark theme.
- [ ] The 3-dot icon is tinted when the note has a color.
- [ ] An empty filter result shows "No notes here. Try another folder or tag."

### Trash
- [ ] Long-press a note and tap the trash icon in the selection bar: the note leaves the list and a snackbar offers Undo. Undo brings it back.
- [ ] Delete from the editor: a confirmation appears, then you return to the list and the note is in the trash.
- [ ] Open Trash from the top bar. The note shows how many days are left (30 days for a fresh one).
- [ ] Restore returns the note to the list with its content, images, audio, folder, tags and color.
- [ ] Delete forever (with confirmation) removes it; images and audio files are deleted too.
- [ ] Empty (with confirmation) removes everything in the trash. The button is disabled when the trash is empty.
- [ ] Trashed notes do not appear in the list, search or tag counts.
- [ ] A note trashed more than 30 days ago is erased the next time the app starts (set the phone date forward more than 30 days to try this).

### Side menu
- [ ] The top-left of the note list is a menu button. Tapping it, or swiping from the left edge, opens a thin side bar with the Stackpad logo (the same picture as the app icon), All, Pinned, Folders, Tags, Trash and Settings. Tapping outside it closes it.
- [ ] All shows every note with the title "All notes" and no folder or tag bars.
- [ ] Pinned shows only pinned notes, titled "Pinned", with no folder or tag bars.
- [ ] Folders and Tags behave as described under "Folders, tags and colors". Switching views clears the previous filter.
- [ ] Trash opens the trash screen; Settings opens settings.
- [ ] The menu is thin but readable in portrait and landscape, light and dark; TalkBack announces each item with its label.

### Caret and selection checkbox
- [ ] In a long note, type on a line near the bottom with the keyboard open, then open the + or Tag row: the line with the cursor scrolls up so the cursor is never hidden behind the chips or option row. Same in a list, a checklist, a quote and the title.
- [ ] Select blocks: every block (a one-line text, a tall image, a table) has the same 24 dp checkbox in the same left column.

### Select blocks, templates and note lock
- [ ] Menu, Select blocks: the bottom bars disappear, each block gets a checkbox, and tapping a block selects it without opening the keyboard. The top bar shows "N selected".
- [ ] Select all selects every block. Copy selected copies them in note order; paste elsewhere. Copy is disabled with nothing selected. Close or the back button leaves selection mode.
- [ ] Menu, Insert template, Meeting notes on an empty note: the note is filled and the title is set. On a note with content, the template is added at the end and the title is kept.
- [ ] Insert the same template twice: both copies work independently (editing one does not change the other).
- [ ] Menu, Save as template with a name: "Template saved." It appears under Yours in the picker, also after restarting the app. Images and audio are not in it; checklist items are unchecked.
- [ ] Save as template on an empty note shows a message and saves nothing. Delete a saved template (asks to confirm); the built-in ones have no delete.
- [ ] With a screen lock set: Menu, Lock note asks you to unlock, then shows "Note locked". In the list the note has a lock icon and no preview, and searching for words inside it does not find it (its title still does).
- [ ] Open a locked note: a lock screen shows and the prompt opens; the note's text is not visible. Cancel keeps the lock screen; Unlock asks again. Back works.
- [ ] After unlocking, leave the app for over 30 seconds and return: the note is locked again. Under 30 seconds: still open.
- [ ] Menu, Remove note lock asks you to unlock; afterwards the preview and search work again.
- [ ] A locked note with a due-date item shows a generic reminder ("Checklist item", "Due today") without its text; tapping it asks to unlock.
- [ ] With the widget on a locked latest note, it shows "Locked note".
- [ ] Without any phone screen lock, Lock note says to set one first. Updating from 1.5.0 keeps all notes unlocked and nothing is lost.

### Reorder blocks, search and multi-select
- [ ] In a note with several blocks, Menu, Select blocks, select one block in the middle. Move up and Move down move it one step each time; the selection stays so you can press again. At the top, Move up is disabled; at the bottom, Move down is.
- [ ] Select two neighbouring blocks: they move together and keep their order. Select two separate blocks: each moves one step. Undo reverses a move; Redo repeats it.
- [ ] Move an image, a table, an audio note and a checklist: they still work and keep their content afterwards. Leave the note and reopen it: the order is kept.
- [ ] Search with two words that are in different places (for example one in the title and one in the body, or in any order): the note is found. A word that is nowhere makes it disappear.
- [ ] Matches are highlighted in the title, preview and tags. For a word far down a long note, the preview shows the text around it with "…".
- [ ] Searching for `%` or `_` still matches literally.
- [ ] While searching, the chips Folder, Tag and Color appear. Each opens a menu; picking one narrows the results; "Clear filters" removes them. A color filter also works with an empty search box (the chips stay visible while it is set).
- [ ] In the Folders and Tags views the search chips are not shown (those views have their own bars), but search still works.
- [ ] Searching with no results and a filter set says "No notes match … with these filters."
- [ ] Long-press a note: selection starts, the top bar shows "1 selected", the + button and the side-menu swipe are disabled. Tap other notes to add or remove them; unselecting the last one ends selection. Back leaves selection.
- [ ] Select all, then Pin: all become pinned (the icon changes to unpin when all selected are pinned) and selection ends.
- [ ] More menu, Move to folder: pick a folder or "No folder"; the notes move and their order in the list does not change.
- [ ] More menu, Add tag: type `Work`; every selected note gets `#work`. A note that already has it or has 10 tags is left alone, without errors.
- [ ] Trash with several selected: they all go to the trash with one message "N notes moved to trash." and Undo brings all of them back.
- [ ] A locked note can be selected, pinned, moved and trashed without being opened.

### Share
- [ ] In a note with a title, headings, bold/italic, a list, a checklist with a due date, a table with a formula, a link, a quote, code and a divider, open the 3-dot menu, Share, Markdown. The share sheet opens; paste into a notes app: `#` title, `-` bullets, `- [ ]` checklist, a table with the calculated value, fenced code, `>` quote and `---`.
- [ ] Share, Text: no Markdown symbols except what you typed, lists use their own markers.
- [ ] An image shows as `[Image: caption]`. An audio note shows its length.
- [ ] Sharing an empty note shows "This note is empty" and opens nothing.
- [ ] Share, Copy copies the whole note as plain text. Back returns to the main menu.
- [ ] Share, Image: a message says it was saved to the gallery, the share sheet opens, and the picture is in the gallery album "Stackpad". Check title, headings, bold/italic, list markers, checklist card (green, boxes drawn, ticks on done items struck through), table (teal header and grid, calculated values), quote (amber bar), prompt (purple outline), code (slate, monospaced), link (blue), audio (rose pill with play button), divider and pictures with captions. A colored note gives a lightly tinted page. On Android 9 or older: the storage permission is asked once; deny it and the image is still shared with a message.
- [ ] Share, PDF: the file picker opens with the note title as the name; after choosing a place the PDF is saved there ("PDF saved") and the share sheet opens; cancelling the picker does nothing. Same look as the image. A4 pages; blocks are not cut in half unless taller than a page; pictures fit the page width. Try a note with many blocks (several pages) and one empty note (message, nothing shared).
- [ ] A very long note shows "too long for one image" for Image and still works as PDF.
- [ ] The menu also has Move to trash, which still asks for confirmation.

### Backup and restore
- [ ] Side menu, Settings, Export backup. Pick a place; a snackbar says how many notes were saved. Open the zip on a computer: it has `backup.json` and a `media` folder with your images and audio.
- [ ] Trash a note, then export. Make sure the trashed note is also in the backup (it returns to the trash on restore).
- [ ] Edit a note after exporting, then Restore from that backup: the snackbar says it was skipped, and your newer edit is kept.
- [ ] Delete a note forever, then restore the backup: the note, its images and audio come back; counts say 1 added.
- [ ] Restore the same backup twice: the second time everything is skipped.
- [ ] Choose a random file (a photo or a text file) to restore: "This file is not a Stackpad backup." and nothing changes.
- [ ] Notes with checklist due dates in the future get their reminders again after a restore.
- [ ] Backing out of the file picker does nothing and shows no error.

### App lock
- [ ] With no screen lock on the phone, turning on App lock says to set one first.
- [ ] With a screen lock, turn on App lock: you must unlock first, and the switch turns on.
- [ ] Press Home, wait over 30 seconds, return: the lock screen appears and the unlock prompt opens. Unlocking opens the app exactly where you left it.
- [ ] Return within 30 seconds: no lock.
- [ ] Cancel the prompt: the lock screen stays; the Unlock button asks again.
- [ ] The recent-apps screen shows no preview of the app, and a screenshot is blocked while the lock is on.
- [ ] Force-stop and open the app: it opens locked.
- [ ] Turn App lock off in Settings (unlock required): the preview and screenshots work again.
- [ ] Remove the phone's screen lock, return to Stackpad: it opens without a lock and the switch is off.

### Widget
- [ ] Add the Stackpad widget from the home-screen widget list. It shows the logo, "Latest note" with a title, and a + button.
- [ ] Tap the + button: Stackpad opens on a new empty note. Tap the widget body: it opens the latest note.
- [ ] Edit a note's title, press Home: the widget shows the new title within a moment.
- [ ] With no notes it says "No notes yet". With App lock on it says "Locked" and tapping opens the lock screen.
- [ ] Rotate the phone after opening from the widget: no second empty note appears.
- [ ] Dark mode changes the widget colors.

### Sorting
- [ ] Sort menu: Last edited, Date created and Title each reorder the list. Pinned notes always stay on top. Title order ignores upper/lower case and puts untitled notes last.
- [ ] The chosen sort is kept after restarting the app.

### Lists
- [ ] Tap **List**. An option row (same look as the text format toolbar) shows Bullets, Numbers, Letters and Checklist, the List chip is highlighted, and each option adds the right block and closes the row. Tapping List again closes the row.
- [ ] Tap **Image**. An option row shows "Choose from gallery" and "Take a photo"; each works and closes the row. Tapping another chip closes the row.
- [ ] Add bullets, numbers and letters. Press Enter to add items; Backspace on an empty item removes it.
- [ ] Add more than 10 numbered items and more than 26 lettered items (`10.`, `aa.`). Text stays aligned.
- [ ] Type a very long item. Wrapped lines stay under the item text, not under the marker.
- [ ] The "x" removes the whole list.

### Checklist
- [ ] Check and uncheck items. The progress bar and `done/total` count update.
- [ ] Drag an item by its handle to reorder; the order is kept after reopening the note.
- [ ] "Add item" and Enter add items; Backspace on an empty item removes it.
- [ ] The "x" removes the whole checklist.
- [ ] Tap the calendar button on an item, pick a date and Save. Android 13+: a notification permission prompt appears the first time. The date chip shows "Today", "Tomorrow" or e.g. `Nov 20`.
- [ ] Tap the chip to change the date, and "Remove date" to clear it.
- [ ] Set a past date on an unchecked item: the chip is highlighted as overdue. Check the item: the highlight goes away.
- [ ] Deny the notification permission: a message explains that reminders are off, and the date is still saved.
- [ ] Reminder delivery: set a due date for tomorrow, then set the phone clock to 9:00 tomorrow (or wait). A notification shows the item text and note title; tapping it opens the note. (It may arrive a few minutes late when the phone is idle.)
- [ ] Check the item before the reminder time, clear its date, delete its checklist, or move the note to the trash: no notification arrives. Restore the note from the trash: the reminder is back.
- [ ] Restart the phone with a pending reminder: it still fires.

### Picture editor
- [ ] Image block: tap the pencil. A menu shows Edit picture, Edit caption (and Restore original after an edit). Tap the picture, then the crop icon at the top left of the full-screen view: the editor opens too.
- [ ] Crop, free: drag a corner, an edge and the inside of the frame. It cannot leave the picture or become tiny. The area outside is dimmed and a thirds grid shows.
- [ ] Crop shapes: choose 1:1, 4:3, 3:4, 16:9, 9:16 and Original. The frame takes that shape; only the corners resize it and the shape holds. Free allows any shape again.
- [ ] Rotate left and right, flip horizontally and vertically on a photo with text: each does what the icon says, the crop frame follows the picture, and a fixed shape stays selected.
- [ ] Straighten: drag the slider both ways. The picture turns, never shows empty corners, and the label shows the angle; near 0 it snaps to 0°. Flipping after straightening keeps the picture level the same way.
- [ ] Undo steps back one change at a time; Reset returns to the opened picture and can itself be undone. Save is disabled until something changed.
- [ ] Save: the note shows the edited picture (also in the list and grid previews and in image/PDF exports). Edit it again and save: still works.
- [ ] Restore original: the first picture is back; the edit can be undone with the note's undo. After restoring, edit and save again: works.
- [ ] Close the editor with unsaved changes (X or back): "Discard changes?" appears; Keep editing stays, Discard closes.
- [ ] A phone photo taken in portrait (with EXIF rotation) opens upright in the editor and saves upright.
- [ ] A very large photo (12 MP or more) saves without the app closing; if memory runs out a message says the picture could not be saved.
- [ ] Back up and restore: an edited picture comes back edited, and Restore original still works. Delete the note: no stray picture files are left.

### Image
- [ ] Add from the gallery. The image is full width.
- [ ] Add from the camera. Cancelling the camera adds nothing.
- [ ] The "x" removes the image.
- [ ] Tap the image: it opens full screen. Pinch to zoom, drag to pan while zoomed (the image cannot be dragged away from the screen edges), double-tap to zoom in and out. Close button and the back gesture both close it.
- [ ] Tap the pencil, then Edit caption: a caption field appears and is focused. The caption stays under the image, shows in the full-screen view, and is found by search. Clearing it and leaving the field hides the field again.

### Table
- [ ] Tap a cell and type `2`, in another `3`, in a third `=A1+B1`. The result shows `5` when not editing.
- [ ] Try `=2+3*4` (14), `=(2+3)*4` (20), `=1/0` (`#DIV/0!`), `=A1` inside A1 (`#CYCLE!`), `=2+` (`#ERR!`).
- [ ] Add/remove rows and columns. The table scrolls sideways when wide.
- [ ] Put `1`, `2`, `3`, `4` in A1:A4 and `=SUM(A1:A4)` elsewhere: `10`. Try `=AVERAGE(A1:A4)` (2.5), `=MIN(A1:A4)`, `=MAX(A1:A4)`, `=COUNT(A1:A4)` (4), and lower-case `=sum(a1:a4)`.
- [ ] Mixed arguments: `=SUM(A1:A4, 5, A1*2)` (17). Nested: `=MAX(SUM(A1:A2), A4)`. A text or blank cell inside a range is skipped.
- [ ] Errors: `=FOO(A1)` shows `#NAME?`, `=SUM(A1:A4` shows `#ERR!`, `=SUM(A1:A99)` shows `#REF!`, `=AVERAGE(B1:B2)` over blank cells shows `#DIV/0!`, a range that includes its own cell shows `#CYCLE!`.

### Audio
- [ ] Tap Audio, allow the microphone. A recording bar shows a live waveform and timer.
- [ ] Stop. An audio row appears: play, waveform, duration, "x".
- [ ] Play, pause and resume. The waveform fills and pulses while playing; the duration counts.
- [ ] Cancel a recording (the "x" in the bar). Nothing is added.
- [ ] Deny the microphone permission. A message with a "Settings" action appears.
- [ ] Tap the waveform: playback jumps there (and starts if it was idle). Drag across the waveform to scrub. A paused note stays paused after seeking.
- [ ] Tap the speed chip: 1x, 1.5x, 2x, back to 1x. Playback speed changes immediately while playing and applies to the next note you play.
- [ ] The "x" removes the audio and its file.

### Add bar, cards and checklist sort
- [ ] The add bar order is +, Text, List, Cards, Tag. The + chip opens Image, Camera, Audio, Link, Table and Divider; each adds its block and closes the row. Audio is hidden while recording.
- [ ] The add bar has a Cards chip instead of separate Prompt, Quote and Code chips. Tapping it opens a row with Prompt, Quote and Code; each adds its block and closes the row.
- [ ] While the +, List, Cards or Tag row is open, the text formatting row (H1, H2, H3, B, I, U) is hidden; it returns when the row closes. It is also hidden while recording audio.
- [ ] In a checklist with a mix of done and open items, the sort button moves done items to the bottom, keeping the order inside each group. The button is disabled when already sorted. Undo restores the order.

### Link and prompt
- [ ] Link block: enter a title and `example.com`. The open button works. `not a url` shows an error and disables open.
- [ ] Prompt: write text, tap copy, paste somewhere else. The text matches.

### Appearance
- [ ] Theme menu: System, Light and Dark each apply immediately and persist after restarting the app.
- [ ] Spacing is compact; text is small but readable; layouts fit in portrait and landscape.
- [ ] TalkBack announces buttons with meaningful labels.
