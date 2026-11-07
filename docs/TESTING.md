# Testing

## Automated

```bash
./gradlew testDebugUnitTest
```

Covers: formula evaluator, span editing, URL detection/validation, list markers, table operations, waveform helpers, block JSON serialization, note sorting and filtering, tags, trash timing, and undo history.

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
- [ ] In a note, tap the folder/tags/color button. In the sheet, create a folder: the note is filed in it.
- [ ] Back in the list, the folder appears as a chip; selecting it shows only its notes. "No folder" shows unfiled notes. "All" shows everything.
- [ ] With a folder selected, use its edit button to rename it, then to delete it. After deleting, its notes remain under "No folder".
- [ ] Add tags to a note (type and press Done or Add; also try `#Work` and `Two Words`). They appear lowercase as `#work` and `#two-words`. Adding the same tag again does nothing. The 11th tag cannot be added.
- [ ] Tags from other notes appear as suggestions; tapping one adds it. Tapping a tag in the note removes it.
- [ ] Tag chips in the list show counts. Selecting one filters the list; selecting it again clears the filter. Folder and tag filters combine.
- [ ] Searching for a tag name finds the note.
- [ ] Pick each color. The note card in the list is tinted; "No color" removes the tint. Colors look right in light and dark theme.
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

### Image
- [ ] Add from the gallery. The image is full width.
- [ ] Add from the camera. Cancelling the camera adds nothing.
- [ ] The "x" removes the image.

### Table
- [ ] Tap a cell and type `2`, in another `3`, in a third `=A1+B1`. The result shows `5` when not editing.
- [ ] Try `=2+3*4` (14), `=(2+3)*4` (20), `=1/0` (`#DIV/0!`), `=A1` inside A1 (`#CYCLE!`), `=2+` (`#ERR!`).
- [ ] Add/remove rows and columns. The table scrolls sideways when wide.

### Audio
- [ ] Tap Audio, allow the microphone. A recording bar shows a live waveform and timer.
- [ ] Stop. An audio row appears: play, waveform, duration, "x".
- [ ] Play, pause and resume. The waveform fills and pulses while playing; the duration counts.
- [ ] Cancel a recording (the "x" in the bar). Nothing is added.
- [ ] Deny the microphone permission. A message with a "Settings" action appears.
- [ ] The "x" removes the audio and its file.

### Link and prompt
- [ ] Link block: enter a title and `example.com`. The open button works. `not a url` shows an error and disables open.
- [ ] Prompt: write text, tap copy, paste somewhere else. The text matches.

### Appearance
- [ ] Theme menu: System, Light and Dark each apply immediately and persist after restarting the app.
- [ ] Spacing is compact; text is small but readable; layouts fit in portrait and landscape.
- [ ] TalkBack announces buttons with meaningful labels.
