package com.vaultapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.vaultapp.data.model.Note
import com.vaultapp.data.repository.NoteRepository
import com.vaultapp.ui.components.ToastManager
import com.vaultapp.ui.theme.vaultColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(private val repo: NoteRepository) : ViewModel() {
    val deletedNotes = repo.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun restore(note: Note)       = viewModelScope.launch { repo.updateNote(note.copy(isDeleted = false)); ToastManager.restored() }
    fun permanentDelete(id: Long) = viewModelScope.launch { repo.permanentlyDelete(id); ToastManager.show("Note permanently deleted") }
    fun emptyTrash()              = viewModelScope.launch { repo.emptyTrash(); ToastManager.show("Trash emptied") }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrashScreen(
    onNoteClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val vc    = MaterialTheme.vaultColors
    val notes by viewModel.deletedNotes.collectAsStateWithLifecycle()
    var showEmptyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = vc.background,
            topBar = {
                TopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                    title = {
                        Column {
                            Text("Trash", color = vc.onBackground, fontWeight = FontWeight.SemiBold)
                            if (notes.isNotEmpty()) Text("${notes.size} deleted notes", color = vc.onSurfaceVariant, fontSize = 12.sp)
                        }
                    },
                    actions = {
                        if (notes.isNotEmpty()) TextButton(onClick = { showEmptyConfirm = true }) {
                            Text("Empty all", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(vc.background)
                    .padding(padding)
                    .padding(bottom = 100.dp)
            ) {
                if (notes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗑️", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                            Text("Trash is empty", color = vc.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            Text("Deleted notes appear here for 30 days", color = vc.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp)).background(vc.surfaceVariant).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Notes are permanently deleted after 30 days", color = vc.onSurfaceVariant, fontSize = 12.sp)
                    }
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalItemSpacing = 8.dp
                    ) {
                        items(notes, key = { it.id }) { note ->
                            TrashCard(note = note, vc = vc,
                                onRestore = { viewModel.restore(note) },
                                onDelete  = { viewModel.permanentDelete(note.id) })
                        }
                    }
                }
            }
        }

    if (showEmptyConfirm) {
        AlertDialog(onDismissRequest = { showEmptyConfirm = false }, containerColor = vc.surface,
            title = { Text("Empty trash?", color = vc.onBackground) },
            text  = { Text("All ${notes.size} notes will be permanently deleted.", color = vc.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = { viewModel.emptyTrash(); showEmptyConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Empty Trash") }
            },
            dismissButton = { TextButton(onClick = { showEmptyConfirm = false }) { Text("Cancel", color = vc.onSurfaceVariant) } }
        )
    }
}

@Composable
private fun TrashCard(note: Note, vc: com.vaultapp.ui.theme.VaultColors, onRestore: () -> Unit, onDelete: () -> Unit) {
    val bg = note.color.toCardColor(vc)
    val df = SimpleDateFormat("MMM d", Locale.getDefault())
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(bg.copy(.55f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (note.title.isNotEmpty()) Text(note.title, color = vc.onSurface.copy(.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (note.content.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(note.content, color = vc.onSurface.copy(.5f), fontSize = 11.sp, maxLines = 4, overflow = TextOverflow.Ellipsis) }
            Spacer(Modifier.height(8.dp))
            Text("Deleted ${df.format(Date(note.updatedAt))}", color = vc.onSurfaceVariant.copy(.5f), fontSize = 10.sp)
        }
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(.1f)), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onRestore, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.RestoreFromTrash, null, tint = vc.primary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Restore", color = vc.primary, fontSize = 12.sp)
            }
            TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }
}
