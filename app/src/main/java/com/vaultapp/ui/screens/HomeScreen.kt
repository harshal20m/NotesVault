package com.vaultapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultapp.data.model.Note
import com.vaultapp.data.model.NoteColor
import com.vaultapp.ui.components.ToastManager
import com.vaultapp.ui.theme.VaultColors
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNoteClick: (Long) -> Unit,
    onAddNote: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val vc            = MaterialTheme.vaultColors
    val notes         by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery   by viewModel.searchQuery.collectAsStateWithLifecycle()
    val gridColumns   by viewModel.gridColumns.collectAsStateWithLifecycle()
    val passwordCount by viewModel.passwordCount.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    val filters = remember { listOf("All", "Notes", "Pinned", "Media", "Locked") }

    val displayNotes = remember(notes, selectedFilter) {
        when (selectedFilter) {
            "Pinned" -> notes.filter { it.isPinned }
            "Media"  -> notes.filter { it.mediaUris.isNotEmpty() }
            "Locked" -> notes.filter { it.isLocked }
            else     -> notes
        }
    }

    Scaffold(
        containerColor = vc.background,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onAddNote,
                containerColor   = vc.primary,
                contentColor     = Color.White,
                modifier         = Modifier.padding(bottom = 110.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Main content ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // FIX: bottom padding = floating nav height so content isn't hidden
                    .padding(bottom = 100.dp)
            ) {
                // ── App bar — NO extra spacer, tight to status bar ─────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()          // only status bar, nothing extra
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Vault",
                        color      = vc.onBackground,
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .background(vc.primary.copy(.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("secure", color = vc.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.toggleGridColumns() }) {
                        Icon(
                            if (gridColumns == 2) Icons.Outlined.GridView else Icons.Outlined.ViewAgenda,
                            null, tint = vc.onSurfaceVariant, modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // ── Search bar ─────────────────────────────────────────────
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = viewModel::onSearchQuery,
                    placeholder   = { Text("Search notes, passwords…", color = vc.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Outlined.Search, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                    trailingIcon  = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { viewModel.onSearchQuery("") }) { Icon(Icons.Default.Close, null, tint = vc.onSurfaceVariant) } }
                    } else null,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 0.dp),
                    shape         = RoundedCornerShape(16.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = vc.surface,
                        unfocusedContainerColor = vc.surface,
                        focusedTextColor        = vc.onBackground,
                        unfocusedTextColor      = vc.onBackground,
                        focusedBorderColor      = vc.primary,
                        unfocusedBorderColor    = vc.outline,
                        cursorColor             = vc.primary
                    ),
                    singleLine    = true
                )

                Spacer(Modifier.height(8.dp))

                // ── Filter chips ───────────────────────────────────────────
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { f ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick  = { selectedFilter = f },
                            label    = { Text(f, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = vc.primaryContainer,
                                selectedLabelColor     = vc.primary,
                                containerColor         = vc.surface,
                                labelColor             = vc.onSurfaceVariant
                            ),
                            border   = FilterChipDefaults.filterChipBorder(
                                enabled             = true,
                                selected            = selectedFilter == f,
                                selectedBorderColor = Color.Transparent,
                                borderColor         = vc.outline
                            ),
                            shape    = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // ── Vault badge ────────────────────────────────────────────
                if (passwordCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(vc.primaryContainer)
                            .clickable(onClick = onNavigateToVault)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(7.dp).background(vc.primary, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("$passwordCount encrypted passwords", color = vc.primary, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, null, tint = vc.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                }

                // ── Notes grid / empty state ───────────────────────────────
                if (displayNotes.isEmpty()) {
                    EmptyState(vc, searchQuery.isNotEmpty(), onAddNote)
                } else {
                    LazyVerticalStaggeredGrid(
                        columns               = StaggeredGridCells.Fixed(gridColumns),
                        contentPadding        = PaddingValues(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing   = 8.dp,
                        modifier              = Modifier.fillMaxSize()
                    ) {
                        items(displayNotes, key = { it.id }) { note ->
                            NoteCard(
                                note        = note,
                                onClick     = { onNoteClick(note.id) },
                                onLongClick = {
                                    selectedNote = note
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    selectedNote?.let { note ->
        AlertDialog(
            onDismissRequest = { selectedNote = null },
            containerColor = vc.surface,
            title = { Text("Note options", color = vc.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = {
                        viewModel.togglePin(note.id, !note.isPinned)
                        if (note.isPinned) ToastManager.unpinned() else ToastManager.pinned()
                        selectedNote = null
                    }) { Text(if (note.isPinned) "Unpin" else "Pin", color = vc.primary) }
                    TextButton(onClick = {
                        if (note.isArchived) {
                            viewModel.unarchiveNote(note.id)
                            ToastManager.info("Note unarchived")
                        } else {
                            viewModel.archiveNote(note.id)
                            ToastManager.info("Note archived")
                        }
                        selectedNote = null
                    }) { Text(if (note.isArchived) "Unarchive" else "Archive", color = vc.primary) }
                    TextButton(onClick = {
                        viewModel.deleteNote(note.id)
                        ToastManager.deleted()
                        selectedNote = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedNote = null }) { Text("Cancel", color = vc.onSurfaceVariant) }
            }
        )
    }
}

// ── Note card ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onLongClick: () -> Unit) {
    val vc     = MaterialTheme.vaultColors
    val bgColor = note.color.toCardColor(vc)
    val df      = SimpleDateFormat("MMM d", Locale.getDefault())

    // FIX: strip JSON wrapper from content for display
    val displayContent = remember(note.content) { extractPlainText(note.content) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.dp, vc.outline.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp)
    ) {
        Column {
            // Title row
            if (note.title.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Icon(Icons.Default.PushPin, null, tint = vc.onSurface, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        note.title,
                        color      = vc.onSurface,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    if (note.isLocked) {
                        Icon(Icons.Default.Lock, null, tint = vc.onSurface, modifier = Modifier.size(13.dp))
                    }
                    if (note.reminderAt != null) {
                        Icon(Icons.Default.Alarm, null, tint = vc.onSurface, modifier = Modifier.size(13.dp))
                    }
                }
            }

            // Content
            if (note.isLocked) {
                if (note.title.isNotEmpty()) Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, null, tint = vc.onSurface.copy(.35f), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Protected content", color = vc.onSurface.copy(.35f), fontSize = 12.sp)
                }
            } else if (displayContent.isNotEmpty()) {
                if (note.title.isNotEmpty()) Spacer(Modifier.height(5.dp))
                Text(
                    displayContent,
                    color    = vc.onSurface.copy(.82f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Media indicator
            if (note.mediaUris.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Image, null, tint = vc.onSurface.copy(.45f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${note.mediaUris.size} media", color = vc.onSurface.copy(.45f), fontSize = 10.sp)
                }
            }

            // Tags
            if (note.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.take(2).forEach { tag ->
                        Text(
                            "#$tag",
                            color    = vc.primary.copy(.85f),
                            fontSize = 10.sp,
                            modifier = Modifier
                                .background(vc.primaryContainer.copy(.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(df.format(Date(note.updatedAt)), color = vc.onSurface.copy(.4f), fontSize = 10.sp)
        }
    }
}

// FIX: extract plain text from serialized JSON so note cards show readable content
fun extractPlainText(raw: String): String {
    if (raw.isBlank()) return ""
    return try {
        val map = com.google.gson.Gson().fromJson(raw, Map::class.java)
        val text = map["text"] as? String ?: ""
        // Also append checklist items as plain text preview
        @Suppress("UNCHECKED_CAST")
        val checklist = map["checklist"] as? List<Map<String, Any>>
        val checkText = checklist?.joinToString("\n") { item ->
            val done = (item["isChecked"] as? Boolean) ?: false
            val t    = item["text"] as? String ?: ""
            "${if (done) "✓" else "○"} $t"
        } ?: ""
        listOf(text, checkText).filter { it.isNotBlank() }.joinToString("\n").trim()
    } catch (_: Exception) {
        raw // not JSON, show as-is
    }
}

@Composable
private fun EmptyState(vc: VaultColors, isSearch: Boolean, onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(if (isSearch) "🔍" else "📝", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                if (isSearch) "No results found" else "No notes yet",
                color = vc.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Medium
            )
            Text(
                if (isSearch) "Try a different search term" else "Tap + to create your first note",
                color = vc.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp)
            )
            if (!isSearch) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = vc.primary)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add note")
                }
            }
        }
    }
}

fun NoteColor.toCardColor(vc: VaultColors): Color = when (this) {
    NoteColor.DEFAULT      -> vc.surface
    NoteColor.PURPLE       -> vc.noteCard1
    NoteColor.TEAL         -> vc.noteCard2
    NoteColor.PINK         -> vc.noteCard3
    NoteColor.AMBER        -> vc.noteCard4
    NoteColor.BLUE         -> vc.noteCard5
    NoteColor.GREEN        -> vc.noteCard6
    NoteColor.RED          -> vc.noteCard7
    NoteColor.INDIGO       -> vc.noteCard8
    NoteColor.LIGHT_YELLOW -> Color(0xFFFFFDE7)
    NoteColor.LIGHT_GREEN  -> Color(0xFFE8F5E9)
    NoteColor.LIGHT_BLUE   -> Color(0xFFE3F2FD)
    NoteColor.LIGHT_PINK   -> Color(0xFFFCE4EC)
}
