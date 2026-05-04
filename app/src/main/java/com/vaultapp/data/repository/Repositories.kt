package com.vaultapp.data.repository

import com.vaultapp.data.local.NoteDao
import com.vaultapp.data.local.PasswordDao
import com.vaultapp.data.model.Note
import com.vaultapp.data.model.PasswordEntry
import com.vaultapp.util.CryptoManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val dao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    fun searchNotes(query: String): Flow<List<Note>> = dao.searchNotes(query)
    fun getPinnedNotes(): Flow<List<Note>> = dao.getPinnedNotes()
    fun getDeletedNotes(): Flow<List<Note>> = dao.getDeletedNotes()
    fun getArchivedNotes(): Flow<List<Note>> = dao.getArchivedNotes()
    fun getNoteCount(): Flow<Int> = dao.getNoteCount()

    suspend fun getNoteById(id: Long): Note? = dao.getNoteById(id)
    suspend fun saveNote(note: Note): Long = dao.insertNote(note.copy(updatedAt = System.currentTimeMillis()))
    suspend fun updateNote(note: Note) = dao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    suspend fun deleteNote(id: Long) = dao.softDeleteNote(id)
    suspend fun permanentlyDelete(id: Long) = dao.permanentlyDeleteNote(id)
    suspend fun emptyTrash() = dao.emptyTrash()
    suspend fun setPinned(id: Long, pinned: Boolean) = dao.setPinned(id, pinned)
    suspend fun setArchived(id: Long, archived: Boolean) = dao.setArchived(id, archived)
}

@Singleton
class PasswordRepository @Inject constructor(private val dao: PasswordDao) {

    fun getAllPasswords(): Flow<List<PasswordEntry>> = dao.getAllPasswords()
    fun searchPasswords(query: String): Flow<List<PasswordEntry>> = dao.searchPasswords(query)
    fun getByCategory(category: String): Flow<List<PasswordEntry>> = dao.getByCategory(category)
    fun getPasswordCount(): Flow<Int> = dao.getPasswordCount()

    suspend fun getPasswordById(id: Long): PasswordEntry? = dao.getPasswordById(id)

    suspend fun savePassword(entry: PasswordEntry): Long {
        val encrypted = entry.copy(
            encryptedPassword = CryptoManager.encrypt(entry.encryptedPassword),
            updatedAt = System.currentTimeMillis()
        )
        return dao.insertPassword(encrypted)
    }

    suspend fun getDecryptedPassword(id: Long): String? {
        val entry = dao.getPasswordById(id) ?: return null
        return runCatching { CryptoManager.decrypt(entry.encryptedPassword) }.getOrNull()
    }

    suspend fun updatePassword(entry: PasswordEntry) {
        val encrypted = entry.copy(
            encryptedPassword = CryptoManager.encrypt(entry.encryptedPassword),
            updatedAt = System.currentTimeMillis()
        )
        dao.updatePassword(encrypted)
    }

    suspend fun deletePassword(entry: PasswordEntry) = dao.deletePassword(entry)
    suspend fun setFavorite(id: Long, fav: Boolean) = dao.setFavorite(id, fav)
}
