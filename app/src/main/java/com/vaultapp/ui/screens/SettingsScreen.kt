package com.vaultapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultapp.data.model.AppTheme
import com.vaultapp.data.model.LockTimeout
import com.vaultapp.ui.components.ToastManager
import com.vaultapp.ui.theme.toVaultColors
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemes: () -> Unit, onBackup: () -> Unit, onTrash: () -> Unit,
    onTags: () -> Unit, onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val vc           = MaterialTheme.vaultColors
    val useBio       by viewModel.useBiometrics.collectAsStateWithLifecycle()
    val autoUpdate   by viewModel.autoUpdateEnabled.collectAsStateWithLifecycle()
    val lockTimeout  by viewModel.lockTimeout.collectAsStateWithLifecycle()
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val recovEmail   by viewModel.recoveryEmail.collectAsStateWithLifecycle()
    var showTimeoutPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = vc.background,
            topBar = {
                TopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                    title = { Text("Settings", color = vc.onBackground, fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().background(vc.background).padding(padding).verticalScroll(rememberScrollState())) {
                SettingsSection("Security") {
                    SettingsRow(Icons.Outlined.Lock, "Change PIN", "Update your unlock PIN") { ToastManager.info("PIN change coming soon") }
                    SettingsSwitchRow(Icons.Outlined.Fingerprint, "Biometrics", "Fingerprint / Face ID", useBio) {
                        viewModel.setBiometrics(it)
                        ToastManager.success(if (it) "Biometrics enabled" else "Biometrics disabled")
                    }
                    SettingsRow(Icons.Outlined.Timer, "Lock timeout", lockTimeout.label) { showTimeoutPicker = true }
                    SettingsRow(Icons.Outlined.Email, "Recovery email", recovEmail.ifEmpty { "Not set — tap to add" }) {}
                }
                SettingsSection("Updates") {
                    SettingsSwitchRow(Icons.Outlined.Update, "Auto-update", "Check for updates automatically", autoUpdate) {
                        viewModel.setAutoUpdate(it)
                    }
                    SettingsRow(Icons.Outlined.SystemUpdate, "Check for updates", "Search for new version on GitHub") {
                        viewModel.checkForUpdates()
                        ToastManager.info("Checking for updates...")
                    }
                }
                SettingsSection("Appearance") {
                    SettingsRow(Icons.Outlined.Palette, "Themes", currentTheme.displayName, trailing = {
                        Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(Color(android.graphics.Color.parseColor(currentTheme.primaryHex))))
                    }) { onThemes() }
                }
                SettingsSection("Organisation") {
                    SettingsRow(Icons.Outlined.Tag, "Tags", "Rename, delete and browse tags") { onTags() }
                }
                SettingsSection("Data") {
                    SettingsRow(Icons.Outlined.CloudUpload, "Backup & Restore", "Export or import your encrypted vault") { onBackup() }
                    SettingsRow(Icons.Outlined.Delete, "Trash", "View and restore deleted notes") { onTrash() }
                }
                SettingsSection("About") {
                    SettingsRow(Icons.Outlined.Info, "Version", "1.0.0 · Kotlin + Compose") {}
                    SettingsRow(Icons.Outlined.PrivacyTip, "Privacy", "Fully offline · AES-256-GCM · No telemetry") {}
                }
                Spacer(Modifier.height(100.dp))
            }
        }

    if (showTimeoutPicker) {
        AlertDialog(onDismissRequest = { showTimeoutPicker = false }, containerColor = vc.surface,
            title = { Text("Lock timeout", color = vc.onBackground) },
            text = {
                Column {
                    LockTimeout.values().forEach { t ->
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setLockTimeout(t); showTimeoutPicker = false; ToastManager.success("Timeout set: ${t.label}") }
                            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = lockTimeout == t, onClick = { viewModel.setLockTimeout(t); showTimeoutPicker = false },
                                colors = RadioButtonDefaults.colors(selectedColor = vc.primary))
                            Spacer(Modifier.width(8.dp))
                            Text(t.label, color = vc.onSurface)
                        }
                    }
                }
            }, confirmButton = {}
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val vc = MaterialTheme.vaultColors
    Text(title, color = vc.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = vc.surface), border = BorderStroke(.5.dp, vc.outline)) { Column(content = content) }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    val vc = MaterialTheme.vaultColors
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(vc.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = vc.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) { Text(title, color = vc.onBackground, fontSize = 14.sp); Text(subtitle, color = vc.onSurfaceVariant, fontSize = 12.sp) }
        trailing?.invoke() ?: Icon(Icons.Default.ChevronRight, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsSwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val vc = MaterialTheme.vaultColors
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(vc.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = vc.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) { Text(title, color = vc.onBackground, fontSize = 14.sp); Text(subtitle, color = vc.onSurfaceVariant, fontSize = 12.sp) }
        Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = vc.primary))
    }
}

// ── Themes Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val vc           = MaterialTheme.vaultColors
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                title = { Text("Themes", color = vc.onBackground, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement   = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().background(vc.background).padding(padding)
        ) {
            items(AppTheme.values()) { theme ->
                ThemeSwatch(theme = theme, isSelected = theme == currentTheme) {
                    viewModel.setTheme(theme)
                    ToastManager.success("${theme.displayName} theme applied")
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(theme: AppTheme, isSelected: Boolean, onSelect: () -> Unit) {
    val primary = Color(android.graphics.Color.parseColor(theme.primaryHex))
    val bg      = Color(android.graphics.Color.parseColor(theme.backgroundHex))
    val surface = Color(android.graphics.Color.parseColor(theme.surfaceHex))
    Box(modifier = Modifier.aspectRatio(.85f).clip(RoundedCornerShape(18.dp)).background(bg)
        .border(if (isSelected) 2.dp else .5.dp, if (isSelected) primary else Color.White.copy(.1f), RoundedCornerShape(18.dp))
        .clickable(onClick = onSelect)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)).background(surface))
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth(.7f).height(8.dp).clip(RoundedCornerShape(3.dp)).background(surface.copy(.7f)))
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(2) { Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(surface)) }
            }
        }
        Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(.3f)).padding(vertical = 6.dp)) {
            Text(theme.displayName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Center))
        }
        if (isSelected) Box(Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).clip(RoundedCornerShape(10.dp)).background(primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
