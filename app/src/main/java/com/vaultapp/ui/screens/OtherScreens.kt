package com.vaultapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.vaultapp.data.local.PreferencesManager
import com.vaultapp.data.model.PasswordCategory
import com.vaultapp.data.model.PasswordStrength
import com.vaultapp.ui.components.ToastManager
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.PasswordEditViewModel
import com.vaultapp.util.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlin.math.max
import javax.inject.Inject

// ── Setup Screen ──────────────────────────────────────────────────────────────
@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    vm: SetupViewModel = hiltViewModel()
) {
    val vc = MaterialTheme.vaultColors
    var step       by remember { mutableStateOf(0) }
    var pin        by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var recovEmail by remember { mutableStateOf("") }
    var pinError   by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(vc.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 32.dp)) {
            repeat(2) { i ->
                Box(Modifier.size(if (step == i) 24.dp else 8.dp, 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (step >= i) vc.primary else vc.outline))
            }
        }

        Text("🔐", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))

        when (step) {
            0 -> {
                Text("Create your PIN", color = vc.onBackground, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("This PIN protects all your notes and passwords", color = vc.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(32.dp))
                VaultOutlinedField("6-digit PIN", pin, { if (it.length <= 6 && it.all(Char::isDigit)) pin = it }, vc, isPassword = true)
                Spacer(Modifier.height(12.dp))
                VaultOutlinedField("Confirm PIN", confirmPin, { if (it.length <= 6 && it.all(Char::isDigit)) confirmPin = it }, vc,
                    isPassword = true, isError = pinError.isNotEmpty(), errorMsg = pinError)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        when {
                            pin.length < 6      -> pinError = "PIN must be 6 digits"
                            pin != confirmPin   -> pinError = "PINs don't match"
                            else                -> { pinError = ""; step = 1 }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = vc.primary),
                    shape    = RoundedCornerShape(14.dp)
                ) { Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
            }
            1 -> {
                Text("Recovery email", color = vc.onBackground, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Used only if you forget your PIN — stored locally", color = vc.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(32.dp))
                VaultOutlinedField("Recovery email (optional)", recovEmail, { recovEmail = it }, vc)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        vm.finish(pin, recovEmail, onSetupComplete)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = vc.primary),
                    shape    = RoundedCornerShape(14.dp)
                ) { Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                TextButton(onClick = { vm.finish(pin, "", onSetupComplete) }) {
                    Text("Skip", color = vc.onSurfaceVariant)
                }
            }
        }
    }
}

@HiltViewModel
class SetupViewModel @Inject constructor(val prefs: PreferencesManager) : ViewModel() {
    fun finish(pin: String, email: String, onDone: () -> Unit) = viewModelScope.launch {
        prefs.savePin(CryptoManager.hashPin(pin))
        if (email.isNotEmpty()) prefs.setRecoveryEmail(email)
        prefs.setSetupComplete(true)
        onDone()
    }
}

// ── Recover Screen ────────────────────────────────────────────────────────────
@Composable
fun RecoverScreen(onRecovered: () -> Unit, onBack: () -> Unit) {
    val vc = MaterialTheme.vaultColors
    var email by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().background(vc.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("🔑", fontSize = 48.sp); Spacer(Modifier.height(16.dp))
        Text("Account Recovery", color = vc.onBackground, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Enter the recovery email you set during setup", color = vc.onSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))
        VaultOutlinedField("Recovery email", email, { email = it }, vc)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { ToastManager.info("Recovery email sent") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = vc.primary),
            shape    = RoundedCornerShape(14.dp)
        ) { Text("Send Recovery Code", fontSize = 16.sp) }
        TextButton(onClick = onBack) { Text("Back to login", color = vc.primary) }
    }
}

// ── Password Edit Screen ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEditScreen(
    passwordId: Long,
    onBack: () -> Unit,
    viewModel: PasswordEditViewModel = hiltViewModel()
) {
    val vc = MaterialTheme.vaultColors
    var showPw by remember { mutableStateOf(false) }
    var showGenerateConfirm by remember { mutableStateOf(false) }
    LaunchedEffect(passwordId) { viewModel.loadEntry(passwordId) }
    val strength = viewModel.computeStrength(viewModel.password)

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                title = { Text(if (passwordId == -1L) "Add Password" else "Edit Password", color = vc.onBackground, fontWeight = FontWeight.SemiBold) },
                actions = {
                    TextButton(onClick = {
                        viewModel.save {
                            ToastManager.success("Password saved")
                            onBack()
                        }
                    }) { Text("Save", color = vc.primary, fontWeight = FontWeight.Medium) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(vc.background).padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VaultOutlinedField("Title *", viewModel.title, { viewModel.title = it }, vc)
            VaultOutlinedField("Username / Email", viewModel.username, { viewModel.username = it }, vc)

            // Password field with generator
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Password *") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showPw = !showPw }) {
                            Icon(if (showPw) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = vc.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            showGenerateConfirm = true
                        }) {
                            Icon(Icons.Outlined.Refresh, "Generate", tint = vc.primary)
                        }
                    }
                },
                shape  = RoundedCornerShape(14.dp),
                colors = vaultTextFieldColors(vc)
            )

            // Strength bar
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(4) { i ->
                        Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i < strength.segments) when (strength) {
                                    PasswordStrength.WEAK   -> Color(0xFFE24B4A)
                                    PasswordStrength.FAIR   -> Color(0xFFEF9F27)
                                    PasswordStrength.MEDIUM -> Color(0xFF1D9E75)
                                    PasswordStrength.STRONG -> vc.primary
                                } else vc.outline
                            ))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(strength.label, color = vc.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.width(48.dp))
                }
            }

            VaultOutlinedField("Website (optional)", viewModel.website, { viewModel.website = it }, vc)

            // Category selector
            Text("Category", color = vc.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordCategory.values().forEach { cat ->
                    FilterChip(
                        selected = viewModel.category == cat,
                        onClick  = { viewModel.category = cat },
                        label    = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = vc.primary, selectedLabelColor = Color.White,
                            containerColor = vc.surface, labelColor = vc.onSurfaceVariant),
                        border   = FilterChipDefaults.filterChipBorder(enabled = true, selected = viewModel.category == cat,
                            selectedBorderColor = Color.Transparent, borderColor = vc.outline)
                    )
                }
            }
            Text("Category card color (optional)", color = vc.onSurfaceVariant, fontSize = 12.sp)
            val colorPresets = listOf("", "#FCE4EC", "#E3F2FD", "#E8F5E9", "#FFF8E1", "#EDE7F6", "#FFE0B2")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorPresets.forEach { hex ->
                    val selected = viewModel.selectedCategoryColorHex.equals(hex, ignoreCase = true)
                    val swatchColor = if (hex.isBlank()) vc.surface else runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(vc.surface)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(swatchColor)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) vc.primary else vc.outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setSelectedCategoryColor(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (hex.isBlank()) Text("Ø", color = vc.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
            VaultOutlinedField(
                label = "Custom HEX (e.g. #F5F5F5)",
                value = viewModel.selectedCategoryColorHex,
                onChange = { viewModel.setSelectedCategoryColor(it.take(7)) },
                vc = vc
            )
            Text("Saved per category. Leave blank to use default app color.", color = vc.onSurfaceVariant, fontSize = 11.sp)

            if (viewModel.passwordHistory.isNotEmpty()) {
                Text("Password history", color = vc.onSurfaceVariant, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    viewModel.passwordHistory.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(vc.surface).padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("••••••••", color = vc.onBackground, fontSize = 12.sp)
                            Text(relativeTime(item.changedAt), color = vc.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }

            VaultOutlinedField("Notes (optional)", viewModel.notes, { viewModel.notes = it }, vc, minLines = 3)
            Spacer(Modifier.height(32.dp))
        }
    }
    if (showGenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showGenerateConfirm = false },
            title = { Text("Generate new password?") },
            text = { Text("Your current password will be replaced. Continue?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.recordCurrentPasswordInHistory()
                    viewModel.password = viewModel.generatePassword()
                    ToastManager.success("Strong password generated")
                    showGenerateConfirm = false
                }, colors = ButtonDefaults.buttonColors(containerColor = vc.primary)) { Text("Generate") }
            },
            dismissButton = { TextButton(onClick = { showGenerateConfirm = false }) { Text("Cancel") } }
        )
    }
}

private fun relativeTime(timeMs: Long): String {
    val days = max(0, ((System.currentTimeMillis() - timeMs) / (1000 * 60 * 60 * 24)).toInt())
    return when {
        days == 0 -> "Today"
        days == 1 -> "1 day ago"
        else -> "$days days ago"
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────
@Composable
fun VaultOutlinedField(
    label: String, value: String, onChange: (String) -> Unit,
    vc: com.vaultapp.ui.theme.VaultColors,
    isPassword: Boolean = false, isError: Boolean = false,
    errorMsg: String = "", minLines: Int = 1
) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), isError = isError, minLines = minLines,
        supportingText = if (isError && errorMsg.isNotEmpty()) { { Text(errorMsg, color = MaterialTheme.colorScheme.error) } } else null,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp), colors = vaultTextFieldColors(vc), singleLine = minLines == 1
    )
}

@Composable
fun vaultTextFieldColors(vc: com.vaultapp.ui.theme.VaultColors) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = vc.surface,   unfocusedContainerColor = vc.surface,
    focusedTextColor        = vc.onBackground, unfocusedTextColor   = vc.onBackground,
    focusedLabelColor       = vc.primary,   unfocusedLabelColor     = vc.onSurfaceVariant,
    focusedBorderColor      = vc.primary,   unfocusedBorderColor    = vc.outline,
    cursorColor             = vc.primary,   errorBorderColor        = MaterialTheme.colorScheme.error
)
