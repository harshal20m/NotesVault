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
        pinHash: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val backup = VaultBackup(notes = notes, passwords = passwords)
            val json = gson.toJson(backup)
            // Encrypt the entire JSON with the derived key
            val encrypted = CryptoManager.encrypt(json)
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
        uri: Uri
    ): Result<VaultBackup> = withContext(Dispatchers.IO) {
        runCatching {
            val encrypted = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: throw IllegalStateException("Could not read file")

            val json = CryptoManager.decrypt(encrypted)
            gson.fromJson(json, VaultBackup::class.java)
        }
    }

    fun generateBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "vault_backup_$timestamp.vbk"
    }
}
