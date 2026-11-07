package com.stackpad.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
