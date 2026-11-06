package com.stackpad.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
abstract class StackpadDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
