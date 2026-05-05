package com.vaultapp.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vaultapp.data.model.Note
import com.vaultapp.data.model.PasswordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

data class VaultBackup(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: List<Note> = emptyList(),
    val passwords: List<PasswordEntry> = emptyList()
)

object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportBackup(
        context: Context,
        notes: List<Note>,
        passwords: List<PasswordEntry>,
        password: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val backup = VaultBackup(notes = notes, passwords = passwords)
            val json = gson.toJson(backup)
            // Encrypt with password for portability
            val encrypted = CryptoManager.encryptWithPassword(json, password)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "vault_backup_$timestamp.vbk"

            val file = java.io.File(context.getExternalFilesDir(null), fileName)
            file.writeText(encrypted)

            androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.provider", file
            )
        }
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        password: String
    ): Result<VaultBackup> = withContext(Dispatchers.IO) {
        runCatching {
            // Validate file name if available
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else null
                } else null
            }
            
            if (fileName != null && !fileName.endsWith(".vbk")) {
                throw IllegalArgumentException("Invalid file type. Please select a .vbk backup file")
            }

            // Read file content
            val encrypted = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: throw IllegalStateException("Could not read file. Please check file permissions")

            if (encrypted.isBlank()) {
                throw IllegalArgumentException("File is empty or corrupted")
            }

            // Decrypt with password
            val json = try {
                CryptoManager.decryptWithPassword(encrypted, password)
            } catch (e: Exception) {
                throw IllegalStateException("Decryption failed. Wrong password or corrupted backup file")
            }

            // Parse JSON
            try {
                gson.fromJson(json, VaultBackup::class.java) ?: throw IllegalArgumentException("Invalid backup format")
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid backup file structure. File may be corrupted")
            }
        }
    }

    fun generateBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "vault_backup_$timestamp.vbk"
    }
}
