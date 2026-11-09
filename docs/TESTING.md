# Testing

## Automated

```bash
./gradlew testDebugUnitTest
```

Covers: formula evaluator, span editing, URL detection/validation, list markers, table operations, waveform helpers, block JSON serialization, table functions, due dates and reminder planning, image zoom limits, audio speed, note sorting and filtering, tags, trash timing, and undo history.

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

### Folders, tags and colors
- [ ] In a note, open the 3-dot menu and choose Note options. A small popup opens directly below the dots with Folder, Tags and Color. Create a folder there: the note is filed in it.
- [ ] Side menu, Folders: a small grid of tiles (All, No folder, each folder, New folder) appears above the notes, with no tag row. Selecting a tile filters the list and the title shows the folder name.
- [ ] With a folder selected, the Edit tile offers Rename and Delete. After deleting, its notes remain under "No folder".
- [ ] With many folders the grid scrolls on its own without pushing the notes off screen.
- [ ] Add tags to a note (type and press Done or Add; also try `#Work` and `Two Words`). They appear lowercase as `#work` and `#two-words`. Adding the same tag again does nothing. The 11th tag cannot be added. Tapping the X on a tag in the popup removes it.
- [ ] A note with tags shows them in a row above its title; tapping one opens the options popup.
- [ ] Tags from other notes appear as suggestions; tapping one adds it.
- [ ] Side menu, Tags: tags appear in one horizontal scrolling row with counts, and no folder grid. Selecting one filters the list and the title shows `#tag`; selecting it again clears it.
- [ ] Searching for a tag name finds the note.
- [ ] Pick each color. The note card in the list is tinted; "No color" removes the tint. Colors look right in light and dark theme.
- [ ] The 3-dot icon is tinted when the note has a folder, tag or color.
- [ ] An empty filter result shows "No notes here. Try another folder or tag."

### Trash
- [ ] Delete from the list: a confirmation appears; the note leaves the list and a snackbar offers Undo. Undo brings it back.
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

### Share
- [ ] In a note with a title, headings, bold/italic, a list, a checklist with a due date, a table with a formula, a link, a quote, code and a divider, open the top-right menu and choose Share as Markdown. The share sheet opens; paste into a notes app: `#` title, `-` bullets, `- [ ]` checklist, a table with the calculated value, fenced code, `>` quote and `---`.
- [ ] Share as plain text: no Markdown symbols except what you typed, lists use their own markers.
- [ ] An image shows as `[Image: caption]`. An audio note shows its length.
- [ ] Sharing an empty note shows "This note is empty" and opens nothing.
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

### Image
- [ ] Add from the gallery. The image is full width.
- [ ] Add from the camera. Cancelling the camera adds nothing.
- [ ] The "x" removes the image.
- [ ] Tap the image: it opens full screen. Pinch to zoom, drag to pan while zoomed (the image cannot be dragged away from the screen edges), double-tap to zoom in and out. Close button and the back gesture both close it.
- [ ] Tap the pencil: a caption field appears and is focused. The caption stays under the image, shows in the full-screen view, and is found by search. Clearing it and leaving the field hides the field again.

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

### Cards, copy and checklist sort
- [ ] The add bar has a Cards chip instead of separate Prompt, Quote and Code chips. Tapping it opens a row with Prompt, Quote and Code; each adds its block and closes the row.
- [ ] While the List, Image or Cards row is open, the text formatting row (H1, H2, H3, B, I, U) is hidden; it returns when the row closes. It is also hidden while recording audio.
- [ ] In the 3-dot menu, Copy note copies the whole note as plain text; paste it elsewhere to check it includes every block.
- [ ] In a checklist with a mix of done and open items, the sort button moves done items to the bottom, keeping the order inside each group. The button is disabled when already sorted. Undo restores the order.

### Link and prompt
- [ ] Link block: enter a title and `example.com`. The open button works. `not a url` shows an error and disables open.
- [ ] Prompt: write text, tap copy, paste somewhere else. The text matches.

### Appearance
- [ ] Theme menu: System, Light and Dark each apply immediately and persist after restarting the app.
- [ ] Spacing is compact; text is small but readable; layouts fit in portrait and landscape.
- [ ] TalkBack announces buttons with meaningful labels.
