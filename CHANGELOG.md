# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/mastah123/stackpad/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/mastah123/stackpad/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/mastah123/stackpad/releases/tag/v1.0.0
