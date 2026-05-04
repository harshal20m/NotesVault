package com.vaultapp.ui.screens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onThemes: () -> Unit,
    onBackup: () -> Unit,
    onTrash: () -> Unit,
    onTags: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val vc = MaterialTheme.vaultColors
    val useBio by viewModel.useBiometrics.collectAsStateWithLifecycle()
    val autoUpdate by viewModel.autoUpdateEnabled.collectAsStateWithLifecycle()
    val lockTimeout by viewModel.lockTimeout.collectAsStateWithLifecycle()
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val recovEmail by viewModel.recoveryEmail.collectAsStateWithLifecycle()
    var showTimeoutPicker by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = vc.onBackground
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        color = vc.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = vc.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(vc.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection("Security") {
                SettingsRow(
                    icon = Icons.Outlined.Lock,
                    title = "Change PIN",
                    subtitle = "Update your unlock PIN"
                ) {
                    ToastManager.info("PIN change coming soon")
                }
                SettingsSwitchRow(
                    icon = Icons.Outlined.Fingerprint,
                    title = "Biometrics",
                    subtitle = "Fingerprint / Face ID",
                    checked = useBio
                ) {
                    viewModel.setBiometrics(it)
                    ToastManager.success(if (it) "Biometrics enabled" else "Biometrics disabled")
                }
                SettingsRow(
                    icon = Icons.Outlined.Timer,
                    title = "Lock timeout",
                    subtitle = lockTimeout.label
                ) {
                    showTimeoutPicker = true
                }
                SettingsRow(
                    icon = Icons.Outlined.Email,
                    title = "Recovery email",
                    subtitle = recovEmail.ifEmpty { "Not set — tap to add" }
                ) {
                }
            }
            SettingsSection("Updates") {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Update,
                    title = "Auto-update",
                    subtitle = "Check for updates automatically",
                    checked = autoUpdate
                ) {
                    viewModel.setAutoUpdate(it)
                }
                SettingsRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Check for updates",
                    subtitle = "Search for new version on GitHub"
                ) {
                    viewModel.checkForUpdates()
                    ToastManager.info("Checking for updates...")
                }
            }
            SettingsSection("Appearance") {
                SettingsRow(
                    icon = Icons.Outlined.Palette,
                    title = "Themes",
                    subtitle = currentTheme.displayName,
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(currentTheme.toVaultColors().primary)
                                .border(
                                    width = 1.dp,
                                    color = vc.outline.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                ) {
                    onThemes()
                }
            }
            SettingsSection("Organisation") {
                SettingsRow(
                    icon = Icons.Outlined.Tag,
                    title = "Tags",
                    subtitle = "Rename, delete and browse tags"
                ) {
                    onTags()
                }
            }
            SettingsSection("Data") {
                SettingsRow(
                    icon = Icons.Outlined.CloudUpload,
                    title = "Backup & Restore",
                    subtitle = "Export or import your encrypted vault"
                ) {
                    onBackup()
                }
                SettingsRow(
                    icon = Icons.Outlined.Delete,
                    title = "Trash",
                    subtitle = "View and restore deleted notes"
                ) {
                    onTrash()
                }
            }
            SettingsSection("About") {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.0 · Kotlin + Compose"
                ) {
                }
                SettingsRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy",
                    subtitle = "Fully offline · AES-256-GCM · No telemetry"
                ) {
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    if (showTimeoutPicker) {
        AlertDialog(
            onDismissRequest = { showTimeoutPicker = false },
            containerColor = vc.surface,
            title = {
                Text(
                    text = "Lock timeout",
                    color = vc.onBackground
                )
            },
            text = {
                Column {
                    LockTimeout.values().forEach { timeout ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setLockTimeout(timeout)
                                    showTimeoutPicker = false
                                    ToastManager.success("Timeout set: ${timeout.label}")
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = lockTimeout == timeout,
                                onClick = {
                                    viewModel.setLockTimeout(timeout)
                                    showTimeoutPicker = false
                                    ToastManager.success("Timeout set: ${timeout.label}")
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = vc.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = timeout.label,
                                color = vc.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val vc = MaterialTheme.vaultColors
    Text(
        text = title,
        color = vc.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = vc.surface
        ),
        border = BorderStroke(
            1.dp,
            vc.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(content = content)
    }
    Spacer(modifier = Modifier.height(12.dp))
}
@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val vc = MaterialTheme.vaultColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(vc.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = vc.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = vc.onBackground,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = vc.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        trailing?.invoke() ?: Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = vc.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    val vc = MaterialTheme.vaultColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(vc.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = vc.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = vc.onBackground,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = vc.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = vc.onPrimary,
                checkedTrackColor = vc.primary,
                uncheckedThumbColor = vc.surface,
                uncheckedTrackColor = vc.surfaceVariant,
                uncheckedBorderColor = vc.outline
            )
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val vc = MaterialTheme.vaultColors
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = vc.onBackground
                        )
                    }
                },
                title = {
                    Text(
                        text = "Themes",
                        color = vc.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = vc.surface
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp,
                bottom = 100.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(vc.background)
                .padding(padding)
        ) {
            items(AppTheme.values()) { theme ->
                ThemeSwatch(
                    theme = theme,
                    isSelected = theme == currentTheme
                ) {
                    viewModel.setTheme(theme)
                    ToastManager.success("${theme.displayName} theme applied")
                }
            }
        }
    }
}
@Composable
private fun ThemeSwatch(
    theme: AppTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val colors = theme.toVaultColors()
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.background)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onSelect)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surface)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.noteCard1)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.noteCard2)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.noteCard3)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.noteCard4)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(colors.surface.copy(alpha = 0.96f))
                .padding(vertical = 6.dp)
        ) {
            Text(
                text = theme.displayName,
                color = colors.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}