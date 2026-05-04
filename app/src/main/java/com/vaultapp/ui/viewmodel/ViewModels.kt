package com.vaultapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultapp.data.local.PreferencesManager
import com.vaultapp.data.model.*
import com.vaultapp.data.repository.NoteRepository
import com.vaultapp.data.repository.PasswordRepository
import com.vaultapp.service.UpdateManager
import com.vaultapp.util.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── LockViewModel ─────────────────────────────────────────────────────────────
@HiltViewModel
class LockViewModel @Inject constructor(private val prefs: PreferencesManager) : ViewModel() {
    val isSetupComplete = prefs.isSetupComplete
    val useBiometrics   = prefs.useBiometrics
    fun verifyPin(pin: String): Boolean {
        val stored = kotlinx.coroutines.runBlocking { prefs.pinHash.first() }
        return stored != null && CryptoManager.verifyPin(pin, stored)
    }
}

// ── HomeViewModel ─────────────────────────────────────────────────────────────
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
    private val passwordRepo: PasswordRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    val isSearchActive = _searchQuery.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { q -> if (q.isEmpty()) noteRepo.getAllNotes() else noteRepo.searchNotes(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val archivedNotes: StateFlow<List<Note>> = noteRepo.getArchivedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val passwordCount = passwordRepo.getPasswordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val gridColumns = prefs.gridColumns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 2)

    fun onSearchQuery(q: String) { _searchQuery.value = q }
    fun togglePin(id: Long, pinned: Boolean) = viewModelScope.launch { noteRepo.setPinned(id, pinned) }
    fun archiveNote(id: Long) = viewModelScope.launch { noteRepo.setArchived(id, true) }
    fun unarchiveNote(id: Long) = viewModelScope.launch { noteRepo.setArchived(id, false) }
    fun deleteNote(id: Long) = viewModelScope.launch { noteRepo.deleteNote(id) }
    fun toggleGridColumns() = viewModelScope.launch {
        prefs.setGridColumns(if (prefs.gridColumns.first() == 2) 1 else 2)
    }
}

// ── NoteEditViewModel ─────────────────────────────────────────────────────────
// FIX: NO setTitle/setContent aliases — only onTitle/onContent to avoid JVM clash
@HiltViewModel
class NoteEditViewModel @Inject constructor(private val repo: NoteRepository) : ViewModel() {
    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    var title   by mutableStateOf(""); private set
    var content by mutableStateOf(""); private set

    fun onTitle(t: String)   { title   = t }
    fun onContent(c: String) { content = c }

    fun loadNote(id: Long) = viewModelScope.launch {
        if (id == -1L) { _note.value = Note(); return@launch }
        repo.getNoteById(id)?.also { n ->
            _note.value = n; title = n.title; content = n.content
        } ?: run { _note.value = Note() }
    }

    fun setColor(c: NoteColor)       { _note.value = _note.value?.copy(color = c) }
    fun setTags(tags: List<String>)  { _note.value = _note.value?.copy(tags = tags) }
    fun setReminder(ms: Long?)       { _note.value = _note.value?.copy(reminderAt = ms) }
    fun addMedia(uri: String) {
        val cur = _note.value?.mediaUris ?: emptyList()
        if (!cur.contains(uri)) _note.value = _note.value?.copy(mediaUris = cur + uri)
    }
    fun removeMedia(uri: String) {
        _note.value = _note.value?.copy(mediaUris = (_note.value?.mediaUris ?: emptyList()) - uri)
    }
    fun togglePin()  { _note.value = _note.value?.copy(isPinned  = !(_note.value?.isPinned  ?: false)) }
    fun toggleLock() { _note.value = _note.value?.copy(isLocked  = !(_note.value?.isLocked  ?: false)) }

    fun saveNote() = viewModelScope.launch {
        val n = _note.value?.copy(title = title, content = content) ?: return@launch
        if (n.id == 0L) repo.saveNote(n) else repo.updateNote(n)
    }
    fun deleteNote() = viewModelScope.launch {
        _note.value?.id?.takeIf { it != 0L }?.let { repo.deleteNote(it) }
    }
}

// ── VaultViewModel ────────────────────────────────────────────────────────────
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repo: PasswordRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val passwords: StateFlow<List<PasswordEntry>> = _searchQuery
        .flatMapLatest { q -> if (q.isEmpty()) repo.getAllPasswords() else repo.searchPasswords(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categoryColors = prefs.getCategoryColors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun onSearchQuery(q: String) { _searchQuery.value = q }
    fun toggleFavorite(id: Long, f: Boolean) = viewModelScope.launch { repo.setFavorite(id, f) }
    fun deletePassword(entry: PasswordEntry) = viewModelScope.launch { repo.deletePassword(entry) }

    suspend fun getDecryptedPassword(id: Long): String? = repo.getDecryptedPassword(id)
}

// ── PasswordEditViewModel ─────────────────────────────────────────────────────
@HiltViewModel
class PasswordEditViewModel @Inject constructor(
    private val repo: PasswordRepository,
    private val prefs: PreferencesManager
) : ViewModel() {
    private val _entry = MutableStateFlow<PasswordEntry?>(null)
    val entry: StateFlow<PasswordEntry?> = _entry

    var title    by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var website  by mutableStateOf("")
    var category by mutableStateOf(PasswordCategory.OTHER)
    var notes    by mutableStateOf("")
    var selectedCategoryColorHex by mutableStateOf("")
    var passwordHistory by mutableStateOf(listOf<PasswordHistoryItem>())

    init {
        viewModelScope.launch {
            snapshotFlow { category }.flatMapLatest { prefs.getCategoryColor(it) }.collect {
                selectedCategoryColorHex = it
            }
        }
    }

    fun loadEntry(id: Long) = viewModelScope.launch {
        if (id == -1L) { _entry.value = null; return@launch }
        repo.getPasswordById(id)?.also { e ->
            _entry.value = e
            title    = e.title
            username = e.username
            password = runCatching { CryptoManager.decrypt(e.encryptedPassword) }.getOrDefault("")
            website  = e.website
            category = e.category
            notes    = e.notes
        }
    }

    fun computeStrength(pw: String): PasswordStrength {
        var s = 0
        if (pw.length >= 8)                   s++
        if (pw.any { it.isUpperCase() })       s++
        if (pw.any { it.isDigit() })           s++
        if (pw.any { !it.isLetterOrDigit() })  s++
        return when (s) { 1 -> PasswordStrength.WEAK; 2 -> PasswordStrength.FAIR; 3 -> PasswordStrength.MEDIUM; else -> PasswordStrength.STRONG }
    }

    fun generatePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*()"
        return (1..16).map { chars.random() }.joinToString("")
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val e = PasswordEntry(
            id = _entry.value?.id ?: 0L,
            title = title, username = username, encryptedPassword = password,
            website = website, category = category, notes = notes,
            passwordStrength = computeStrength(password)
        )
        if (e.id == 0L) repo.savePassword(e) else repo.updatePassword(e)
        prefs.setCategoryColor(category, selectedCategoryColorHex.trim())
        onDone()
    }

    fun setSelectedCategoryColor(hex: String) {
        selectedCategoryColorHex = hex
    }

    fun recordCurrentPasswordInHistory() {
        val current = password.trim()
        if (current.isBlank()) return
        passwordHistory = (listOf(PasswordHistoryItem(current, System.currentTimeMillis())) + passwordHistory)
            .distinctBy { it.password }
            .take(10)
    }
}

data class PasswordHistoryItem(
    val password: String,
    val changedAt: Long
)

// ── SettingsViewModel ─────────────────────────────────────────────────────────
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val updateManager: UpdateManager
) : ViewModel() {
    val useBiometrics     = prefs.useBiometrics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val autoUpdateEnabled = prefs.autoUpdateEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val lockTimeout       = prefs.lockTimeout  .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LockTimeout.IMMEDIATELY)
    val appTheme          = prefs.appTheme     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AppTheme.MIDNIGHT)
    val recoveryEmail     = prefs.recoveryEmail.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val pinHash           = prefs.pinHash.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val gridColumns       = prefs.gridColumns  .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 2)

    fun setBiometrics(v: Boolean)      = viewModelScope.launch { prefs.setBiometrics(v) }
    fun setAutoUpdate(v: Boolean)      = viewModelScope.launch { prefs.setAutoUpdate(v) }
    fun setLockTimeout(t: LockTimeout) = viewModelScope.launch { prefs.setLockTimeout(t) }
    fun setTheme(t: AppTheme)          = viewModelScope.launch { prefs.setTheme(t) }
    fun setGridColumns(c: Int)         = viewModelScope.launch { prefs.setGridColumns(c) }
    fun setRecoveryEmail(e: String)    = viewModelScope.launch { prefs.setRecoveryEmail(e) }
    fun changePin(newPin: String)      = viewModelScope.launch { prefs.savePin(CryptoManager.hashPin(newPin)) }

    fun checkForUpdates() = viewModelScope.launch {
        updateManager.checkForUpdates(force = true)
    }
}

// ── AnalyticsViewModel ────────────────────────────────────────────────────────
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
    private val passwordRepo: PasswordRepository
) : ViewModel() {
    val noteCount = noteRepo.getNoteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val passwordStats = passwordRepo.getAllPasswords().map { passwords ->
        val total = passwords.size
        val weak = passwords.count { it.passwordStrength == PasswordStrength.WEAK }
        val fair = passwords.count { it.passwordStrength == PasswordStrength.FAIR }
        val medium = passwords.count { it.passwordStrength == PasswordStrength.MEDIUM }
        val strong = passwords.count { it.passwordStrength == PasswordStrength.STRONG }
        val mostActiveCategory = passwords.groupBy { it.category }
            .maxByOrNull { it.value.size }?.key?.name ?: "None"
        PasswordStats(total, weak, fair, medium, strong, mostActiveCategory)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PasswordStats())

    val noteStats = noteRepo.getAllNotes().map { notes ->
        val total = notes.size
        val pinned = notes.count { it.isPinned }
        val locked = notes.count { it.isLocked }
        val withMedia = notes.count { it.mediaUris.isNotEmpty() }
        val tagCount = notes.flatMap { it.tags }.distinct().size
        NoteStats(total, pinned, locked, withMedia, tagCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteStats())
}

data class PasswordStats(
    val total: Int = 0,
    val weak: Int = 0,
    val fair: Int = 0,
    val medium: Int = 0,
    val strong: Int = 0,
    val mostActiveCategory: String = "None"
)

data class NoteStats(
    val total: Int = 0,
    val pinned: Int = 0,
    val locked: Int = 0,
    val withMedia: Int = 0,
    val tagCount: Int = 0
)
