<p align="center">
  <img src="docs/assets/logo.svg" width="96" alt="Stackpad logo">
</p>

<h1 align="center">Stackpad</h1>

<p align="center">
  A block-based note app for Android. Mix text, lists, checklists, images, tables, voice notes, links and prompts in one note.
</p>

<p align="center">
  <a href="https://github.com/mastah123/stackpad/actions/workflows/android.yml"><img alt="Build" src="https://github.com/mastah123/stackpad/actions/workflows/android.yml/badge.svg"></a>
  <img alt="Version" src="https://img.shields.io/badge/version-1.3.0-4F46E5">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0-7F52FF">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue">
</p>

## Features

Every note is a stack of compact blocks, laid out tightly like chat messages.

| Block | What it does |
| --- | --- |
| **Text** | One line per block, so each line can be H1, H2, H3 or normal. **Bold**, *italic*, underline. URLs are highlighted and open when tapped. |
| **List** (menu) | **Bullets**, **Numbers** and **Letters**: long items wrap under the text, never under the marker. **Checklist**: rounded card with a progress tracker, drag handles to reorder, due dates with reminder notifications, and an "x" to remove it. |
| **Image** | Full width, from the gallery or camera. Tap for full-screen zoom, add a caption, or remove it with the "x". |
| **Table** | Small spreadsheet with `+ - * /`, parentheses, cell references such as `=A1+B1*2`, and `SUM`, `AVERAGE`, `MIN`, `MAX`, `COUNT` over ranges such as `=SUM(A1:A5)`. |
| **Audio** | Voice-message style row: play, animated waveform you can tap or drag to seek, duration, 1x / 1.5x / 2x speed, and "x". Recorded in app. |
| **Link** | Title and URL with validation; opens in the browser. |
| **Prompt** | Monospace card with a one-tap copy button. |
| **Quote** | Indented italic quotation with an accent bar. |
| **Code** | Monospace block with a copy button. |
| **Divider** | A horizontal rule between parts of a note. |

Also:

- **Side menu** (top-left) with shortcuts for All notes, Pinned, Folders, Tags and Trash.
- **Undo / redo** in the editor.
- **Folders**, **tags** and **colors** to organize notes, with filters in the note list.
- **Trash** with restore; notes are erased after 30 days.
- **Sort** by last edited, date created or title.
- Note list with **search** and **pin**, autosave, and a light/dark theme that follows the system by default with a manual override.

## Tech stack

- Kotlin, Jetpack Compose (Material 3)
- Room for local storage, DataStore for settings
- kotlinx.serialization for block content
- Coil for images, MediaRecorder / MediaPlayer for audio
- GitHub Actions for CI and release signing

All data stays on the device. There is no account, no cloud sync and no analytics.

## Architecture

```
app/src/main/java/com/stackpad/app/
├── data/            Room database, repository, media files, settings
│   ├── model/       Block types and note models
│   ├── db/          Entities, DAOs, database and migrations
│   ├── media/       Image and audio file storage
│   └── prefs/       Theme and sort preferences
├── domain/          Pure Kotlin logic (unit tested)
│   ├── formula/     Spreadsheet formula evaluator
│   ├── history/     Undo/redo stacks
│   ├── image/       Zoom and pan limits
│   ├── reminders/   Due dates and reminder planning
│   ├── notes/       Sorting, filtering, tags, folder names, trash policy
│   ├── sheet/       Table edit operations
│   └── text/        Span editing, URL detection, list markers
├── audio/           Recorder, player, playback speed, waveform helpers
├── notifications/   Reminder alarms and notifications
├── ui/
│   ├── theme/       Colors, compact typography
│   ├── components/  Shared widgets
│   ├── notes/       Note list screen, side menu, filters, sort
│   ├── trash/       Trash screen
│   ├── editor/      Editor screen, toolbars, one view per block type
│   └── navigation/
└── util/            Formatting and intent helpers
```

Each block type has its own view file and its own pure logic, so a block can change without touching the others.

## Build

Requirements: JDK 17 and the Android SDK (Android Studio sets this up).

```bash
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # debug APK in app/build/outputs/apk/debug/
```

The app version lives in `gradle.properties` (`VERSION_NAME`, `VERSION_CODE`).

### CI and releases

`.github/workflows/android.yml` runs on every push and pull request to `master`. It runs the unit tests, builds a debug APK, and uploads it as a workflow artifact. When the signing secrets exist, it also builds a signed release APK and AAB.

To enable signed builds, add four repository secrets. Step-by-step instructions are in [docs/SIGNING.md](docs/SIGNING.md). No secret is ever stored in the repository.

## Testing

Unit tests cover the formula evaluator, span editing, URL handling, list markers, table operations, waveform helpers, block serialization, table functions, due dates and reminder planning, image zoom limits, audio speed, note sorting and filtering, tags, trash timing and undo history. A manual checklist for everything else (UI, audio, camera) is in [docs/TESTING.md](docs/TESTING.md).

## Known limitations

- Auto-capitalization uses the keyboard's own setting, so it depends on your keyboard app.
- Backspace-at-start merging relies on the keyboard sending a delete key event, which most keyboards do.
- Inline links are detected automatically from typed or pasted URLs. Tapping a link opens it, so to edit a link, tap just outside it.
- Undo and redo cover the title and blocks, not pinning, folder, tags or color. History is cleared when you leave the note.
- Table functions are `SUM`, `AVERAGE`, `MIN`, `MAX` and `COUNT`. There is no `IF`, text or date handling.
- Due dates are whole days. Reminders fire at 9:00 and may arrive a little late when the phone is idle (inexact alarms), and nothing is shown if notifications are off for the app.
- Notes are local only; uninstalling the app deletes them. There is no export or backup yet.
- Recording stops automatically after 10 minutes.

## License

[MIT](LICENSE)
