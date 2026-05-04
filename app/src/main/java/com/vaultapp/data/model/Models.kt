package com.vaultapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ── Note ──────────────────────────────────────────────────────────────────────

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: NoteColor = NoteColor.DEFAULT,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val tags: List<String> = emptyList(),
    val mediaUris: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val reminderAt: Long? = null
)

enum class NoteColor(val hex: String) {
    DEFAULT("#1E1E2E"),
    PURPLE("#2D2460"),
    TEAL("#0D3D30"),
    PINK("#3D1A2E"),
    AMBER("#3A2300"),
    BLUE("#0D2A45"),
    GREEN("#1A3010"),
    RED("#3A1010"),
    INDIGO("#1A1A40"),
    // Light variants
    LIGHT_YELLOW("#FFFDE7"),
    LIGHT_GREEN("#E8F5E9"),
    LIGHT_BLUE("#E3F2FD"),
    LIGHT_PINK("#FCE4EC")
}

// ── Password Entry ─────────────────────────────────────────────────────────────

@Entity(tableName = "passwords")
@TypeConverters(Converters::class)
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val username: String = "",
    val encryptedPassword: String,  // AES encrypted
    val website: String = "",
    val category: PasswordCategory = PasswordCategory.OTHER,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val passwordStrength: PasswordStrength = PasswordStrength.MEDIUM
)

enum class PasswordCategory { SOCIAL, FINANCE, ENTERTAINMENT, WORK, SHOPPING, OTHER }
enum class PasswordStrength(val label: String, val segments: Int) {
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    MEDIUM("Medium", 3),
    STRONG("Strong", 4)
}

// ── App Settings ───────────────────────────────────────────────────────────────

data class AppSettings(
    val theme: AppTheme = AppTheme.MIDNIGHT,
    val lockTimeout: LockTimeout = LockTimeout.IMMEDIATELY,
    val useBiometrics: Boolean = true,
    val usePin: Boolean = true,
    val gridColumns: Int = 2,
    val backupEnabled: Boolean = false,
    val lastBackupAt: Long? = null,
    val isFirstLaunch: Boolean = true,
    val recoveryEmail: String = "",
    val autoUpdateEnabled: Boolean = true
)

enum class AppTheme(
    val displayName: String,
    val backgroundHex: String,
    val surfaceHex: String,
    val primaryHex: String
) {
    MIDNIGHT("Midnight", "#0F0F14", "#1E1E2E", "#7C6AF5"),
    CLOUD("Cloud", "#F8F8FF", "#FFFFFF", "#6B5CE7"),
    FOREST("Forest", "#0A1F18", "#0D3D30", "#1D9E75"),
    ROSE("Rose", "#1A0610", "#3D1A2E", "#D4537E"),
    OCEAN("Ocean", "#060D1A", "#0D2A45", "#378ADD"),
    AMBER("Amber", "#1A1000", "#3A2300", "#BA7517"),
    VIOLET("Violet", "#100A20", "#2A1A50", "#9B59B6"),
    ABYSS("Abyss", "#030810", "#001A2A", "#1D6FA4"),
    MONO("Mono", "#0A0A0A", "#1A1A1A", "#888780"),
    SUNSET("Sunset", "#1A0800", "#3A1800", "#E87040"),
    CHERRY("Cherry", "#1A0008", "#3A0018", "#E05070"),
    ARCTIC("Arctic", "#0A1218", "#152030", "#4DC8E0")
}

enum class LockTimeout(val label: String, val millis: Long) {
    IMMEDIATELY("Immediately", 0L),
    ONE_MIN("1 minute", 60_000L),
    FIVE_MIN("5 minutes", 300_000L),
    FIFTEEN_MIN("15 minutes", 900_000L),
    ONE_HOUR("1 hour", 3_600_000L)
}

// ── Type Converters ────────────────────────────────────────────────────────────

class Converters {
    private val gson = Gson()

    @TypeConverter fun fromStringList(value: List<String>): String = gson.toJson(value)
    @TypeConverter fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter fun fromNoteColor(value: NoteColor): String = value.name
    @TypeConverter fun toNoteColor(value: String): NoteColor =
        runCatching { NoteColor.valueOf(value) }.getOrDefault(NoteColor.DEFAULT)

    @TypeConverter fun fromPasswordCategory(value: PasswordCategory): String = value.name
    @TypeConverter fun toPasswordCategory(value: String): PasswordCategory =
        runCatching { PasswordCategory.valueOf(value) }.getOrDefault(PasswordCategory.OTHER)

    @TypeConverter fun fromPasswordStrength(value: PasswordStrength): String = value.name
    @TypeConverter fun toPasswordStrength(value: String): PasswordStrength =
        runCatching { PasswordStrength.valueOf(value) }.getOrDefault(PasswordStrength.MEDIUM)
}
