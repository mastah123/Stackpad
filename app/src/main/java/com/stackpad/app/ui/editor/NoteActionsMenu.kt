package com.stackpad.app.ui.editor

/** What the Share and Export pages of the note options sheet can do with the open note. [OPTIONS] opens the share options without sharing. */
enum class ShareTarget { COPY, TEXT, MARKDOWN, IMAGE, PDF, OPTIONS }

/** The actions on the note options sheet that the editor screen carries out itself (sharing goes through [ShareTarget]). */
enum class NoteOption { PIN, ARCHIVE, LOCK, HISTORY, DUPLICATE, ADD_TO_HOME, SELECT_BLOCKS, TEMPLATES, SAVE_TEMPLATE, TRASH }
