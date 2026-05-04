package com.vaultapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.vaultapp.data.model.AppTheme
import com.vaultapp.data.model.LockTimeout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStoreInternal: DataStore<Preferences> by preferencesDataStore(name = "vault_prefs")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    val dataStore: DataStore<Preferences> get() = context.dataStoreInternal

    companion object {
        val PIN_HASH             = stringPreferencesKey("pin_hash")
        val IS_SETUP_COMPLETE    = booleanPreferencesKey("is_setup_complete")
        val USE_BIOMETRICS       = booleanPreferencesKey("use_biometrics")
        val APP_THEME            = stringPreferencesKey("app_theme")
        val LOCK_TIMEOUT         = stringPreferencesKey("lock_timeout")
        val GRID_COLUMNS         = intPreferencesKey("grid_columns")
        val RECOVERY_EMAIL       = stringPreferencesKey("recovery_email")
        val RECOVERY_CODE_HASH   = stringPreferencesKey("recovery_code_hash")
        val LAST_BACKUP_AT       = longPreferencesKey("last_backup_at")
        val LAST_ACTIVE_AT       = longPreferencesKey("last_active_at")
        val AUTO_BACKUP_ENABLED  = booleanPreferencesKey("auto_backup_enabled")
    }

    private fun <T> flow(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { it[key] ?: default }

    val pinHash: Flow<String?>       = dataStore.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { it[PIN_HASH] }
    val isSetupComplete: Flow<Boolean> = flow(IS_SETUP_COMPLETE, false)
    val useBiometrics: Flow<Boolean>   = flow(USE_BIOMETRICS, true)
    val gridColumns: Flow<Int>         = flow(GRID_COLUMNS, 2)
    val recoveryEmail: Flow<String>    = flow(RECOVERY_EMAIL, "")
    val lastActiveAt: Flow<Long>       = flow(LAST_ACTIVE_AT, 0L)
    val autoBackupEnabled: Flow<Boolean> = flow(AUTO_BACKUP_ENABLED, false)

    val appTheme: Flow<AppTheme> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[APP_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.MIDNIGHT) } ?: AppTheme.MIDNIGHT }

    val lockTimeout: Flow<LockTimeout> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[LOCK_TIMEOUT]?.let { runCatching { LockTimeout.valueOf(it) }.getOrDefault(LockTimeout.IMMEDIATELY) } ?: LockTimeout.IMMEDIATELY }

    suspend fun savePin(hash: String)           = dataStore.edit { it[PIN_HASH] = hash }
    suspend fun setSetupComplete(v: Boolean)    = dataStore.edit { it[IS_SETUP_COMPLETE] = v }
    suspend fun setBiometrics(v: Boolean)       = dataStore.edit { it[USE_BIOMETRICS] = v }
    suspend fun setTheme(t: AppTheme)           = dataStore.edit { it[APP_THEME] = t.name }
    suspend fun setLockTimeout(t: LockTimeout)  = dataStore.edit { it[LOCK_TIMEOUT] = t.name }
    suspend fun setGridColumns(c: Int)          = dataStore.edit { it[GRID_COLUMNS] = c }
    suspend fun setRecoveryEmail(e: String)     = dataStore.edit { it[RECOVERY_EMAIL] = e }
    suspend fun setRecoveryCodeHash(h: String)  = dataStore.edit { it[RECOVERY_CODE_HASH] = h }
    suspend fun updateLastActiveAt()            = dataStore.edit { it[LAST_ACTIVE_AT] = System.currentTimeMillis() }
    suspend fun setLastBackupAt(t: Long)        = dataStore.edit { it[LAST_BACKUP_AT] = t }
    suspend fun setAutoBackup(v: Boolean)       = dataStore.edit { it[AUTO_BACKUP_ENABLED] = v }
}
