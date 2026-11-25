package com.stackpad.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** 4 -> 5 (app 2.20.0): the archive. Existing notes are not archived. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notes ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
    }
}

/** 3 -> 4 (app 2.9.0): earlier versions of notes. Existing notes simply start with no history. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS note_versions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "noteId TEXT NOT NULL, " +
                "savedAt INTEGER NOT NULL, " +
                "capturedAt INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "blocksJson TEXT NOT NULL)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_note_versions_noteId ON note_versions (noteId)")
    }
}

/** 2 -> 3 (app 1.6.0): per-note lock. Existing notes are unlocked. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notes ADD COLUMN locked INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * 1 -> 2 (app 1.2.0): folders, tags, note colors and the trash.
 * Existing notes keep all their data: they get no folder, no tags, no color, and are not trashed.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notes ADD COLUMN folderId TEXT")
        db.execSQL("ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE notes ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE notes ADD COLUMN trashedAt INTEGER")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS folders (" +
                "id TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "createdAt INTEGER NOT NULL, " +
                "PRIMARY KEY(id))",
        )
    }
}
