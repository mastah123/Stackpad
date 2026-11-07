# Testing

## Automated

```bash
./gradlew testDebugUnitTest
```

Covers: formula evaluator, span editing, URL detection/validation, list markers, table operations, waveform helpers, and block JSON serialization.

## Manual checklist (run on a device or emulator, Android 8.0+)

### Notes
- [ ] Create a note, type a title and some text, press back. It appears in the list.
- [ ] Create a note and leave it empty. It is **not** saved.
- [ ] Reopen a note. Content is exactly as left.
- [ ] Search by a word from the title, then by a word from the body. Matching notes show; clearing search shows all.
- [ ] Search for `%` or `_`. It matches literally (no wildcard behaviour).
- [ ] Pin a note from the list and from the editor. Pinned notes move to the top under "Pinned".
- [ ] Delete from the list and from the editor. A confirmation appears; confirming removes the note.

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
- [ ] Type `https://example.com` or `www.example.com`. It is highlighted. With the cursor in it, "Open link" appears and opens the browser.
- [ ] Press Backspace in an empty text line. It is removed and focus moves up.

### Lists
- [ ] Tap **List**. A menu offers Bullets, Numbers, Letters and Checklist, and each one adds the right block.
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
- [ ] Link: enter a title and `example.com`. The open button works. `not a url` shows an error and disables open.
- [ ] Prompt: write text, tap copy, paste somewhere else. The text matches.

### Appearance
- [ ] Theme menu: System, Light and Dark each apply immediately and persist after restarting the app.
- [ ] Spacing is compact; text is small but readable; layouts fit in portrait and landscape.
- [ ] TalkBack announces buttons with meaningful labels.
