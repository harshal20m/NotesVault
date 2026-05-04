package com.vaultapp.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.vaultapp.data.local.PreferencesManager
import com.vaultapp.data.repository.NoteRepository
import com.vaultapp.data.repository.PasswordRepository
import com.vaultapp.service.AutoBackupWorker
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.util.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val noteRepo: NoteRepository,
    private val pwRepo: PasswordRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    val lastBackupAt = prefs.dataStore.data.map { it[PreferencesManager.LAST_BACKUP_AT] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val autoBackupEnabled = prefs.autoBackupEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    var isExporting by mutableStateOf(false); private set
    var isImporting by mutableStateOf(false); private set
    var lastResult  by mutableStateOf<String?>(null)

    fun export(context: android.content.Context) = viewModelScope.launch {
        isExporting = true
        val notes = noteRepo.getAllNotes().first()
        val pwds  = pwRepo.getAllPasswords().first()
        BackupManager.exportBackup(context, notes, pwds, "")
            .onSuccess { uri ->
                prefs.setLastBackupAt(System.currentTimeMillis())
                lastResult = "✅ Backup saved successfully"
                context.startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"; putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Save backup"
                ))
            }
            .onFailure { lastResult = "❌ Export failed: ${it.message}" }
        isExporting = false
    }

    fun import(context: android.content.Context, uri: android.net.Uri) = viewModelScope.launch {
        isImporting = true
        BackupManager.importBackup(context, uri)
            .onSuccess { backup ->
                backup.notes.forEach { noteRepo.saveNote(it.copy(id = 0)) }
                backup.passwords.forEach { pwRepo.savePassword(it.copy(id = 0)) }
                lastResult = "✅ Imported ${backup.notes.size} notes · ${backup.passwords.size} passwords"
            }
            .onFailure { lastResult = "❌ Import failed: ${it.message}" }
        isImporting = false
    }

    fun setAutoBackup(enabled: Boolean, context: android.content.Context) = viewModelScope.launch {
        prefs.setAutoBackup(enabled)
        if (enabled) AutoBackupWorker.scheduleWeekly(context) else AutoBackupWorker.cancel(context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit, viewModel: BackupViewModel = hiltViewModel()) {
    val vc           = MaterialTheme.vaultColors
    val ctx          = LocalContext.current
    val lastBackup   by viewModel.lastBackupAt.collectAsStateWithLifecycle()
    val autoBackup   by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val result       = viewModel.lastResult

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.import(ctx, it) }
    }

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = vc.onBackground) } },
                title = { Text("Backup & Restore", color = vc.onBackground, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(vc.background).padding(padding)
            .verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Last backup
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = vc.surface), border = BorderStroke(.5.dp, vc.outline)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(vc.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CloudDone, null, tint = vc.primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Last backup", color = vc.onSurfaceVariant, fontSize = 12.sp)
                        Text(lastBackup?.let { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(it)) } ?: "Never",
                            color = vc.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Result banner
            result?.let { msg ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(
                    containerColor = if (msg.startsWith("✅")) Color(0xFF0D3D30) else Color(0xFF3A1010))) {
                    Text(msg, color = if (msg.startsWith("✅")) Color(0xFF6EE4C0) else Color(0xFFFF8080), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                }
            }

            Text("Actions", color = vc.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)

            BkpCard(Icons.Outlined.CloudUpload, "Export Backup", "Save encrypted vault to your device", vc.primary, viewModel.isExporting, vc) { viewModel.export(ctx) }
            BkpCard(Icons.Outlined.CloudDownload, "Import Backup", "Restore from a .vbk backup file", Color(0xFF1D9E75), viewModel.isImporting, vc) { importLauncher.launch("*/*") }

            Text("Automation", color = vc.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)

            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = vc.surface), border = BorderStroke(.5.dp, vc.outline)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(vc.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Schedule, null, tint = vc.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-backup", color = vc.onBackground, fontSize = 15.sp)
                        Text("Weekly automatic encrypted backup", color = vc.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(checked = autoBackup, onCheckedChange = { viewModel.setAutoBackup(it, ctx) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = vc.primary))
                }
            }

            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = vc.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Security, null, tint = vc.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Security & Privacy", color = vc.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                    listOf("AES-256-GCM encrypted backup files", "Protected by Android Keystore", "No data ever leaves your device", "Import requires matching device key").forEach { info ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Text("•", color = vc.primary, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp, top = 1.dp))
                            Text(info, color = vc.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BkpCard(icon: ImageVector, title: String, desc: String, color: Color, loading: Boolean, vc: com.vaultapp.ui.theme.VaultColors, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(enabled = !loading, onClick = onClick),
        shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = vc.surface),
        border = BorderStroke(1.dp, color.copy(.3f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(.15f)), contentAlignment = Alignment.Center) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = color, strokeWidth = 2.dp)
                else Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = vc.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(desc, color = vc.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}
