package com.vaultapp.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultapp.data.model.PasswordCategory
import com.vaultapp.data.model.PasswordEntry
import com.vaultapp.data.model.PasswordStrength
import com.vaultapp.data.model.NoteColor
import com.vaultapp.ui.components.ToastManager
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    onPasswordClick: (Long) -> Unit,
    onAddPassword: () -> Unit,
    onBack: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val vc        = MaterialTheme.vaultColors
    val passwords by viewModel.passwords.collectAsStateWithLifecycle()
    val searchQ   by viewModel.searchQuery.collectAsStateWithLifecycle()
    var selCat    by remember { mutableStateOf<PasswordCategory?>(null) }
    var selectedPassword by remember { mutableStateOf<PasswordEntry?>(null) }
    val deletingPasswordIds = remember { mutableStateListOf<Long>() }
    // FIX: grid view for vault - 2 cols
    var useGrid   by remember { mutableStateOf(true) }
    val scope     = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    val display = remember(passwords, selCat) {
        if (selCat == null) passwords else passwords.filter { it.category == selCat }
    }

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                title = {
                    Column {
                        Text("Password Vault", color = vc.onBackground, fontWeight = FontWeight.SemiBold)
                        Text("${passwords.size} passwords · AES-256 encrypted", color = vc.onSurfaceVariant, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { useGrid = !useGrid }) {
                        Icon(if (useGrid) Icons.Outlined.ViewList else Icons.Outlined.GridView, null, tint = vc.onSurfaceVariant)
                    }
                    IconButton(onClick = onAddPassword) { Icon(Icons.Default.Add, null, tint = vc.primary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPassword,
                containerColor = vc.primary,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 110.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(vc.background)
                .padding(padding)
                .padding(bottom = 100.dp)
        ) {
            // Search
            OutlinedTextField(
                value = searchQ, onValueChange = viewModel::onSearchQuery,
                placeholder = { Text("Search passwords…", color = vc.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = vc.onSurfaceVariant) },
                trailingIcon = if (searchQ.isNotEmpty()) { { IconButton(onClick = { viewModel.onSearchQuery("") }) { Icon(Icons.Default.Close, null, tint = vc.onSurfaceVariant) } } } else null,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(14.dp), colors = vaultTextFieldColors(vc), singleLine = true
            )
            // Category chips
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selCat == null, onClick = { selCat = null }, label = { Text("All", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = vc.primary, selectedLabelColor = Color.White, containerColor = vc.surface, labelColor = vc.onSurfaceVariant),
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selCat == null, selectedBorderColor = Color.Transparent, borderColor = vc.outline))
                PasswordCategory.values().forEach { cat ->
                    FilterChip(selected = selCat == cat, onClick = { selCat = if (selCat == cat) null else cat },
                        label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = vc.primary, selectedLabelColor = Color.White, containerColor = vc.surface, labelColor = vc.onSurfaceVariant),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selCat == cat, selectedBorderColor = Color.Transparent, borderColor = vc.outline))
                }
            }

            if (display.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔐", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                        Text("No passwords saved", color = vc.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Tap + to add your first password", color = vc.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = onAddPassword, colors = ButtonDefaults.buttonColors(containerColor = vc.primary)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Add password")
                        }
                    }
                }
            } else {
                // FIX: Grid layout for passwords
                if (useGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(display) { pw ->
                            AnimatedVisibility(
                                visible = !deletingPasswordIds.contains(pw.id),
                                enter = fadeIn(),
                                exit = fadeOut() + scaleOut(targetScale = 0.6f)
                            ) {
                                PasswordGridCard(
                                    entry   = pw,
                                    vc      = vc,
                                    categoryColor = categoryColorFor(pw, vc),
                                    onReveal = { viewModel.getDecryptedPassword(pw.id).orEmpty() },
                                    onCopy  = { scope.launch {
                                        val plain = viewModel.getDecryptedPassword(pw.id) ?: ""
                                        clipboard.setText(AnnotatedString(plain))
                                    }},
                                    onClick = { onPasswordClick(pw.id) },
                                    onLongClick = { selectedPassword = pw }
                                )
                            }
                        }
                    }
                } else {
                    // List layout
                    val grouped = remember(display) { display.groupBy { it.category } }
                    // FIX: use non-lazy list inside scrollable column to avoid infinite height crash
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        grouped.forEach { (cat, items) ->
                            Text(cat.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = vc.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                            items.forEach { pw ->
                                PasswordListCard(entry = pw, vc = vc,
                                    categoryColor = categoryColorFor(pw, vc),
                                    onReveal = { viewModel.getDecryptedPassword(pw.id).orEmpty() },
                                    onCopy = { scope.launch {
                                        val plain = viewModel.getDecryptedPassword(pw.id) ?: ""
                                        clipboard.setText(AnnotatedString(plain))
                                    }},
                                    onClick = { onPasswordClick(pw.id) },
                                    onLongClick = { selectedPassword = pw }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    selectedPassword?.let { pw ->
        AlertDialog(
            onDismissRequest = { selectedPassword = null },
            title = { Text("🔐 Password Actions", color = vc.onBackground) },
            text = { Text("Choose what you want to do with “${pw.title}”.", color = vc.onSurfaceVariant) },
            confirmButton = {
                FilledTonalButton(onClick = {
                    viewModel.toggleFavorite(pw.id, !pw.isFavorite)
                    selectedPassword = null
                }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = vc.primaryContainer)) {
                    Icon(Icons.Default.PushPin, null, tint = vc.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(if (pw.isFavorite) "Unpin" else "Pin", color = vc.primary)
                }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    FilledTonalButton(onClick = {
                        deletingPasswordIds.add(pw.id)
                        scope.launch {
                            delay(450)
                            viewModel.deletePassword(pw)
                            deletingPasswordIds.remove(pw.id)
                        }
                        selectedPassword = null
                    }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(.15f))) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { selectedPassword = null }) { Text("Cancel") }
                }
            }
        )
    }
}

// ── Grid card ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasswordGridCard(entry: PasswordEntry, vc: com.vaultapp.ui.theme.VaultColors, categoryColor: Color, onReveal: suspend () -> String, onCopy: () -> Unit, onClick: () -> Unit, onLongClick: () -> Unit) {
    var revealed by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    var plainPassword by remember { mutableStateOf("••••••••") }
    val scope = rememberCoroutineScope()
    val bgColor = categoryColor
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = bgColor),
        border   = BorderStroke(0.5.dp, vc.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.title, color = vc.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    scope.launch {
                        if (!revealed) plainPassword = onReveal()
                        revealed = !revealed
                    }
                }, modifier = Modifier.size(28.dp)) {
                    Icon(if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            if (entry.username.isNotEmpty()) Text(entry.username, color = vc.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
            Spacer(Modifier.height(6.dp))
            Text(
                if (revealed) plainPassword else "••••••••",
                color = vc.primary.copy(.7f), fontSize = 12.sp, letterSpacing = 2.sp
            )
            // Strength bar
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(top = 6.dp)) {
                repeat(4) { i ->
                    Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(
                        if (i < entry.passwordStrength.segments) strengthColor(entry.passwordStrength) else vc.outline))
                }
            }
            Spacer(Modifier.height(8.dp))
            // Copy button
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(vc.primaryContainer)
                .clickable(onClick = {
                    onCopy()
                    copied = true
                }).padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ContentCopy, null, tint = vc.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (copied) "Copied" else "Copy", color = vc.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── List card ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasswordListCard(entry: PasswordEntry, vc: com.vaultapp.ui.theme.VaultColors, categoryColor: Color, onReveal: suspend () -> String, onCopy: () -> Unit, onClick: () -> Unit, onLongClick: () -> Unit) {
    var revealed by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    var plainPassword by remember { mutableStateOf("••••••••") }
    val scope = rememberCoroutineScope()
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
        .clip(RoundedCornerShape(14.dp)).background(categoryColor).combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.title, color = vc.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (entry.username.isNotEmpty()) Text(entry.username, color = vc.onSurfaceVariant, fontSize = 12.sp)
            Text(if (revealed) plainPassword else "••••••••", color = vc.primary.copy(.7f), fontSize = 12.sp, letterSpacing = 2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                repeat(4) { i ->
                    Box(Modifier.width(20.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(
                        if (i < entry.passwordStrength.segments) strengthColor(entry.passwordStrength) else vc.outline))
                }
                Spacer(Modifier.width(4.dp))
                Text(entry.passwordStrength.label, color = vc.onSurfaceVariant, fontSize = 10.sp)
            }
        }
        IconButton(onClick = {
            scope.launch {
                if (!revealed) plainPassword = onReveal()
                revealed = !revealed
            }
        }, modifier = Modifier.size(36.dp)) {
            Icon(if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        TextButton(onClick = { onCopy(); copied = true }) { Text(if (copied) "Copied" else "Copy", color = vc.primary, fontSize = 11.sp) }
    }
}

private fun categoryColorFor(entry: PasswordEntry, vc: com.vaultapp.ui.theme.VaultColors): Color {
    entry.cardColorHex.takeIf { it.isNotBlank() }?.let { hex ->
        runCatching { return Color(android.graphics.Color.parseColor(hex)) }
    }
    return when (entry.category) {
    PasswordCategory.SOCIAL -> NoteColor.LIGHT_PINK.toCardColor(vc)
    PasswordCategory.FINANCE -> NoteColor.LIGHT_GREEN.toCardColor(vc)
    PasswordCategory.ENTERTAINMENT -> NoteColor.LIGHT_BLUE.toCardColor(vc)
    PasswordCategory.WORK -> NoteColor.PURPLE.toCardColor(vc)
    PasswordCategory.SHOPPING -> NoteColor.LIGHT_YELLOW.toCardColor(vc)
    PasswordCategory.OTHER -> NoteColor.DEFAULT.toCardColor(vc)
}
}

private fun strengthColor(s: PasswordStrength) = when (s) {
    PasswordStrength.WEAK   -> Color(0xFFE24B4A)
    PasswordStrength.FAIR   -> Color(0xFFEF9F27)
    PasswordStrength.MEDIUM -> Color(0xFF1D9E75)
    PasswordStrength.STRONG -> Color(0xFF7C6AF5)
}
