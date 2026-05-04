package com.vaultapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultapp.data.model.NoteColor
import com.vaultapp.service.ReminderWorker
import com.vaultapp.ui.components.ReminderPickerDialog
import com.vaultapp.ui.components.TagsEditor
import com.vaultapp.ui.components.ToastManager
import com.vaultapp.ui.screens.editor.*
import com.vaultapp.ui.theme.VaultColors
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.NoteEditViewModel
import com.vaultapp.util.ShareHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    onBack: () -> Unit,
    onMediaOpen: (Long) -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val vc    = MaterialTheme.vaultColors
    val ctx   = LocalContext.current
    val note  by viewModel.note.collectAsStateWithLifecycle()
    val rts   = remember { RichTextState() }

    var showColorPicker    by remember { mutableStateOf(false) }
    var showMoreMenu       by remember { mutableStateOf(false) }
    var showShareSheet     by remember { mutableStateOf(false) }
    var showTagsEditor     by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) { viewModel.loadNote(noteId) }
    LaunchedEffect(note) {
        note?.let { n ->
            if (rts.textFieldValue.text.isEmpty() && n.content.isNotEmpty())
                rts.loadFromJson(n.content)
        }
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri -> viewModel.addMedia(uri.toString()) }
        if (uris.isNotEmpty()) ToastManager.show("${uris.size} image(s) added")
    }

    val cardBg = note?.color?.toCardColor(vc) ?: vc.surface

    fun save() {
        viewModel.onContent(rts.serializeToJson())
        viewModel.saveNote()
    }

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { save(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground)
                    }
                },
                title = {},
                actions = {
                    if (note?.reminderAt != null)
                        IconButton(onClick = { showReminderPicker = true }) {
                            Icon(Icons.Filled.Alarm, null, tint = vc.primary, modifier = Modifier.size(20.dp))
                        }
                    IconButton(onClick = {
                        viewModel.togglePin()
                        if (note?.isPinned == true) ToastManager.unpinned() else ToastManager.pinned()
                    }) {
                        Icon(
                            if (note?.isPinned == true) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            null,
                            tint = if (note?.isPinned == true) vc.primary else vc.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(Icons.Outlined.Share, null, tint = vc.onSurfaceVariant)
                    }
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = vc.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(vc.surface)
                    ) {
                        DropdownMenuItem(text = { Text(if (note?.isLocked == true) "Unlock note" else "Lock note", color = vc.onSurface) },
                            leadingIcon = { Icon(if (note?.isLocked == true) Icons.Outlined.LockOpen else Icons.Outlined.Lock, null, tint = vc.onSurfaceVariant) },
                            onClick = {
                                viewModel.toggleLock()
                                if (note?.isLocked == true) ToastManager.unlocked() else ToastManager.locked()
                                showMoreMenu = false
                            })
                        DropdownMenuItem(text = { Text("Add reminder", color = vc.onSurface) },
                            leadingIcon = { Icon(Icons.Outlined.Alarm, null, tint = vc.onSurfaceVariant) },
                            onClick = { showReminderPicker = true; showMoreMenu = false })
                        DropdownMenuItem(text = { Text("Tags", color = vc.onSurface) },
                            leadingIcon = { Icon(Icons.Outlined.Tag, null, tint = vc.onSurfaceVariant) },
                            onClick = { showTagsEditor = !showTagsEditor; showMoreMenu = false })
                        DropdownMenuItem(text = { Text("Add images", color = vc.onSurface) },
                            leadingIcon = { Icon(Icons.Outlined.Image, null, tint = vc.onSurfaceVariant) },
                            onClick = { imagePicker.launch("image/*"); showMoreMenu = false })
                        HorizontalDivider(color = vc.outline)
                        DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showDeleteConfirm = true; showMoreMenu = false })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(vc.surface).navigationBarsPadding().imePadding()) {
                FormattingToolbar(
                    state         = rts,
                    onColorPicker = { showColorPicker = !showColorPicker },
                    onAddMedia    = { imagePicker.launch("image/*") },
                    onCamera      = { onMediaOpen(note?.id ?: -1L) },
                    onVoice       = {},
                    onTags        = { showTagsEditor = !showTagsEditor }
                )
                AnimatedVisibility(visible = showColorPicker, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    ColorPickerStrip(selected = note?.color ?: NoteColor.DEFAULT, onSelect = { viewModel.setColor(it) }, vc = vc)
                }
                AnimatedVisibility(visible = showTagsEditor, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        TagsEditor(
                            tags          = note?.tags ?: emptyList(),
                            onTagsChanged = { viewModel.setTags(it) }
                        )
                    }
                }
                // Status bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    val wc = rts.textFieldValue.text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
                    Text(
                        "$wc words · ${note?.let { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(it.updatedAt)) } ?: ""}",
                        color = vc.onSurfaceVariant, fontSize = 11.sp
                    )
                    TextButton(onClick = { save(); ToastManager.saved(); onBack() }) {
                        Text("Save", color = vc.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    ) { padding ->
        // FIX: do NOT nest verticalScroll inside Scaffold content when bottom bar has LazyColumn
        // Use a Column with verticalScroll only; checklists use their own Column (not LazyColumn)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Locked note overlay
            if (note?.isLocked == true) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, null, tint = vc.primary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Note is locked", color = vc.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Tap ⋮ → Unlock note to edit", color = vc.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            } else {
                // Title
                BasicTextField(
                    value         = viewModel.title,
                    onValueChange = viewModel::onTitle,
                    textStyle     = TextStyle(color = vc.onBackground, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 30.sp),
                    cursorBrush   = SolidColor(vc.primary),
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    decorationBox = { inner ->
                        if (viewModel.title.isEmpty()) Text("Title", color = vc.onSurfaceVariant.copy(.5f), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        inner()
                    }
                )
                // Mode badge
                if (rts.editorMode == EditorMode.CHECKLIST) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(vc.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("✓ Checklist mode", color = vc.primary, fontSize = 11.sp)
                    }
                }
                // Rich text editor - FIX: uses Column internally, NOT LazyColumn, so no infinite height crash
                RichTextEditor(
                    state           = rts,
                    modifier        = Modifier.fillMaxWidth(),
                    onContentChange = viewModel::onContent
                )
                // Tags row compact
                if (note?.tags?.isNotEmpty() == true && !showTagsEditor) {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier              = Modifier.padding(vertical = 6.dp)
                    ) {
                        items(note?.tags ?: emptyList()) { tag ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(vc.primaryContainer.copy(.6f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text("#$tag", color = vc.primary, fontSize = 11.sp)
                            }
                        }
                    }
                }
                // Media grid
                if (note?.mediaUris?.isNotEmpty() == true) {
                    NoteMediaGrid(
                        mediaUris    = note?.mediaUris ?: emptyList(),
                        onRemove     = { viewModel.removeMedia(it); ToastManager.show("Image removed") },
                        onFullScreen = {}
                    )
                }
            }
            Spacer(Modifier.height(120.dp))
        }
    }

    // ── Share bottom sheet ─────────────────────────────────────────────────────
    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            containerColor   = vc.surface,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = vc.onSurfaceVariant.copy(.3f)) }
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).navigationBarsPadding()) {
                Text("Share note", color = vc.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                ShareRow("💬", "WhatsApp message",  "Send note text to a contact") {
                    ShareHelper.shareAsWhatsAppText(ctx, viewModel.title, rts.textFieldValue.text)
                    showShareSheet = false
                }
                ShareRow("🖼️", "WhatsApp image",   "Share as a styled image card") {
                    ShareHelper.shareAsImage(ctx, viewModel.title, rts.textFieldValue.text)
                    showShareSheet = false
                }
                ShareRow("📋", "Copy to clipboard","Copy plain text") {
                    ShareHelper.copyToClipboard(ctx, viewModel.title, rts.textFieldValue.text)
                    ToastManager.copied(); showShareSheet = false
                }
                ShareRow("📤", "Share via…",       "Open system share sheet") {
                    ShareHelper.shareAsText(ctx, viewModel.title, rts.textFieldValue.text)
                    showShareSheet = false
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Reminder picker ────────────────────────────────────────────────────────
    if (showReminderPicker) {
        ReminderPickerDialog(
            currentReminder = note?.reminderAt,
            onConfirm = { ms ->
                viewModel.setReminder(ms)
                ms?.let { ReminderWorker.schedule(ctx, note?.id ?: -1L, viewModel.title, rts.textFieldValue.text.take(100), it) }
                    ?: note?.id?.let { ReminderWorker.cancel(ctx, it) }
                if (ms != null) ToastManager.info("Reminder set") else ToastManager.info("Reminder removed")
                showReminderPicker = false
            },
            onDismiss = { showReminderPicker = false }
        )
    }

    // ── Delete confirm ─────────────────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = vc.surface,
            title            = { Text("Delete note?", color = vc.onBackground) },
            text             = { Text("This note will be moved to trash.", color = vc.onSurfaceVariant) },
            confirmButton    = {
                Button(
                    onClick = { viewModel.deleteNote(); ToastManager.deleted(); showDeleteConfirm = false; onBack() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton    = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = vc.onSurfaceVariant) } }
        )
    }
}

@Composable
private fun ShareRow(emoji: String, label: String, subtitle: String, onClick: () -> Unit) {
    val vc = MaterialTheme.vaultColors
    Column {
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp, modifier = Modifier.size(40.dp).wrapContentSize())
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, color = vc.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = vc.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = vc.outline.copy(.2f), modifier = Modifier.padding(horizontal = 12.dp))
    }
}

@Composable
private fun ColorPickerStrip(selected: NoteColor, onSelect: (NoteColor) -> Unit, vc: VaultColors) {
    val colors = listOf(
        NoteColor.DEFAULT to vc.surface, NoteColor.PURPLE to vc.noteCard1, NoteColor.TEAL to vc.noteCard2,
        NoteColor.PINK to vc.noteCard3, NoteColor.AMBER to vc.noteCard4, NoteColor.BLUE to vc.noteCard5,
        NoteColor.GREEN to vc.noteCard6, NoteColor.RED to vc.noteCard7, NoteColor.INDIGO to vc.noteCard8,
        NoteColor.LIGHT_YELLOW to Color(0xFFFFFDE7), NoteColor.LIGHT_GREEN to Color(0xFFE8F5E9),
        NoteColor.LIGHT_BLUE to Color(0xFFE3F2FD), NoteColor.LIGHT_PINK to Color(0xFFFCE4EC)
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(colors) { (nc, dc) ->
            Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(dc)
                .border(if (selected == nc) 2.5.dp else 0.5.dp, if (selected == nc) vc.primary else vc.outline, CircleShape)
                .clickable { onSelect(nc) }, contentAlignment = Alignment.Center) {
                if (selected == nc) Icon(Icons.Default.Check, null, tint = Color.White.copy(.8f), modifier = Modifier.size(14.dp))
            }
        }
    }
}
