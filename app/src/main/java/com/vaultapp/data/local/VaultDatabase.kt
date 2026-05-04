package com.vaultapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vaultapp.data.model.Converters
import com.vaultapp.data.model.Note
import com.vaultapp.data.model.PasswordEntry

@Database(
    entities = [Note::class, PasswordEntry::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun passwordDao(): PasswordDao
}
