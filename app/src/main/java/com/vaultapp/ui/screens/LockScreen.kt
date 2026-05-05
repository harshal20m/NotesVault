package com.vaultapp.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultapp.ui.theme.VaultColors
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.LockViewModel

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onSetup: () -> Unit,
    onRecover: () -> Unit,
    viewModel: LockViewModel = hiltViewModel()
) {
    val vc     = MaterialTheme.vaultColors
    val ctx    = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val useBio by viewModel.useBiometrics.collectAsStateWithLifecycle(false)

    var pin        by remember { mutableStateOf("") }
    var errorShake by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf("") }
    var attempts   by remember { mutableStateOf(0) }

    val shakeOffset by animateFloatAsState(
        targetValue      = if (errorShake) 10f else 0f,
        animationSpec    = spring(dampingRatio = .3f, stiffness = 600f),
        finishedListener = { errorShake = false },
        label            = "shake"
    )

    fun tryBiometric() {
        val activity = ctx as? FragmentActivity ?: return
        if (BiometricManager.from(ctx).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            != BiometricManager.BIOMETRIC_SUCCESS) return
        BiometricPrompt(activity, ContextCompat.getMainExecutor(ctx),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) = onUnlocked()
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Vault")
                .setSubtitle("Use your biometric credential")
                .setNegativeButtonText("Use PIN")
                .build()
        )
    }

    LaunchedEffect(useBio) { if (useBio) tryBiometric() }

    Box(modifier = Modifier.fillMaxSize().background(vc.background), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                // Logo
                Box(modifier = Modifier.size(84.dp).clip(RoundedCornerShape(26.dp))
                    .background(vc.surfaceVariant).border(1.dp, vc.primary.copy(.4f), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center) { Text("🔐", fontSize = 38.sp) }

                Spacer(Modifier.height(22.dp))
                Text("Notes Vault", color = vc.onBackground, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                Text("Enter your PIN to unlock", color = vc.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 40.dp))

                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.offset(x = shakeOffset.dp)
                ) {
                    repeat(6) { i ->
                        Box(modifier = Modifier.size(14.dp).clip(CircleShape)
                            .background(if (i < pin.length) vc.primary else Color.Transparent)
                            .border(2.dp, if (i < pin.length) vc.primary else vc.onSurfaceVariant.copy(.4f), CircleShape))
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                if (attempts >= 3) {
                    Spacer(Modifier.height(6.dp))
                    Text("${6 - attempts} attempts remaining", color = MaterialTheme.colorScheme.error.copy(.7f), fontSize = 12.sp)
                }

                Spacer(Modifier.height(40.dp))

                // Numpad
                val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    keys.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            row.forEach { key ->
                                NumKey(key, vc) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (key) {
                                        "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        ""  -> Unit
                                        else -> if (pin.length < 6) {
                                            pin += key
                                            if (pin.length == 6) {
                                                if (viewModel.verifyPin(pin)) {
                                                    onUnlocked()
                                                } else {
                                                    errorShake = true
                                                    attempts++
                                                    errorMsg = "Incorrect PIN"
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    pin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                if (useBio) {
                    OutlinedButton(
                        onClick = { tryBiometric() },
                        shape  = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = vc.primary),
                        border = BorderStroke(1.dp, vc.primary.copy(.5f))
                    ) {
                        Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Use biometrics", fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                TextButton(onClick = onRecover) {
                    Text("Forgot PIN? Recover account", color = vc.primary, fontSize = 13.sp)
                }
            }
        }
}

@Composable
private fun NumKey(key: String, vc: VaultColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(72.dp).clip(CircleShape)
            .then(if (key.isNotEmpty()) Modifier.background(vc.surfaceVariant).clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "⌫" -> Text("⌫", color = vc.primary, fontSize = 22.sp)
            ""  -> Unit
            else -> Text(key, color = vc.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
    }
}
