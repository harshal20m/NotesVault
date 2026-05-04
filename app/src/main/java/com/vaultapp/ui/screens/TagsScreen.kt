package com.vaultapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.vaultapp.data.model.Note
import com.vaultapp.data.repository.NoteRepository
import com.vaultapp.ui.theme.vaultColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(private val repo: NoteRepository) : ViewModel() {
    val allNotes = repo.getAllNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val tagMap: StateFlow<Map<String,Int>> = allNotes.map { notes ->
        buildMap<String,Int> { notes.forEach { n -> n.tags.forEach { t -> put(t, (get(t) ?: 0) + 1) } } }
            .entries.sortedByDescending { it.value }.associate { it.key to it.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun renameTag(old: String, new: String) = viewModelScope.launch {
        val c = new.trim().lowercase().replace(" ","_"); if (c.isEmpty() || c == old) return@launch
        allNotes.value.filter { it.tags.contains(old) }.forEach { repo.updateNote(it.copy(tags = it.tags.map { t -> if (t == old) c else t })) }
    }
    fun deleteTag(tag: String) = viewModelScope.launch {
        allNotes.value.filter { it.tags.contains(tag) }.forEach { repo.updateNote(it.copy(tags = it.tags - tag)) }
    }
    fun notesFor(tag: String) = allNotes.value.filter { it.tags.contains(tag) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(onNoteClick: (Long) -> Unit, onBack: () -> Unit, viewModel: TagsViewModel = hiltViewModel()) {
    val vc      = MaterialTheme.vaultColors
    val tagMap  by viewModel.tagMap.collectAsStateWithLifecycle()
    var expanded    by remember { mutableStateOf<String?>(null) }
    var editing     by remember { mutableStateOf<String?>(null) }
    var editTxt     by remember { mutableStateOf("") }
    var delConfirm  by remember { mutableStateOf<String?>(null) }
    var search      by remember { mutableStateOf("") }

    val visible = if (search.isEmpty()) tagMap else tagMap.filter { (t,_) -> t.contains(search, true) }

    Scaffold(containerColor = vc.background,
        topBar = {
            TopAppBar(navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                title = { Column { Text("Tags", color = vc.onBackground, fontWeight = FontWeight.SemiBold); Text("${tagMap.size} tags · ${tagMap.values.sum()} uses", color = vc.onSurfaceVariant, fontSize = 11.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface))
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(vc.background)
                .padding(padding)
                .padding(bottom = 100.dp)
        ) {
            OutlinedTextField(value = search, onValueChange = { search = it },
                placeholder = { Text("Search tags…", color = vc.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = vc.onSurfaceVariant) },
                trailingIcon = if (search.isNotEmpty()) { { IconButton(onClick = { search = "" }) { Icon(Icons.Default.Close, null, tint = vc.onSurfaceVariant) } } } else null,
                modifier = Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(14.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = vc.surface, unfocusedContainerColor = vc.surface,
                    focusedTextColor = vc.onBackground, unfocusedTextColor = vc.onBackground, focusedBorderColor = vc.primary, unfocusedBorderColor = vc.outline))
            if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏷️", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("No tags yet", color = vc.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Add tags while editing notes", color = vc.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(visible.entries.toList(), key = { it.key }) { (tag, count) ->
                        TagRow(tag, count, expanded == tag, if (expanded == tag) viewModel.notesFor(tag) else emptyList(), vc,
                            onExpand = { expanded = if (expanded == tag) null else tag },
                            onEdit   = { editing = tag; editTxt = tag },
                            onDelete = { delConfirm = tag },
                            onNote   = onNoteClick)
                    }
                }
            }
        }
    }
    editing?.let { tag ->
        AlertDialog(onDismissRequest = { editing = null }, containerColor = vc.surface,
            title = { Text("Rename tag", color = vc.onBackground) },
            text = { OutlinedTextField(value = editTxt, onValueChange = { editTxt = it }, label = { Text("Tag name") }, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = vc.surface, unfocusedContainerColor = vc.surface,
                    focusedTextColor = vc.onBackground, unfocusedTextColor = vc.onBackground, focusedBorderColor = vc.primary, unfocusedBorderColor = vc.outline, focusedLabelColor = vc.primary, unfocusedLabelColor = vc.onSurfaceVariant)) },
            confirmButton = { Button(onClick = { viewModel.renameTag(tag, editTxt); editing = null }, colors = ButtonDefaults.buttonColors(containerColor = vc.primary)) { Text("Rename") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel", color = vc.onSurfaceVariant) } })
    }
    delConfirm?.let { tag ->
        AlertDialog(onDismissRequest = { delConfirm = null }, containerColor = vc.surface,
            title = { Text("Delete \"#$tag\"?", color = vc.onBackground) },
            text  = { Text("Removes the tag from all ${tagMap[tag]} notes. Notes won't be deleted.", color = vc.onSurfaceVariant) },
            confirmButton = { Button(onClick = { viewModel.deleteTag(tag); delConfirm = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Remove tag") } },
            dismissButton = { TextButton(onClick = { delConfirm = null }) { Text("Cancel", color = vc.onSurfaceVariant) } })
    }
}

@Composable
private fun TagRow(tag: String, count: Int, expanded: Boolean, notes: List<Note>, vc: com.vaultapp.ui.theme.VaultColors,
    onExpand: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onNote: (Long) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onExpand).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(vc.primaryContainer), contentAlignment = Alignment.Center) {
                Text("#", color = vc.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("#$tag", color = vc.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("$count note${if (count != 1) "s" else ""}", color = vc.onSurfaceVariant, fontSize = 12.sp)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.Edit, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.DeleteOutline, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(modifier = Modifier.fillMaxWidth().background(vc.surfaceVariant.copy(.4f)).padding(start = 66.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)) {
                notes.forEach { note ->
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onNote(note.id) }.padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(vc.primary.copy(.5f), CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title.ifEmpty { note.content.take(40).ifEmpty { "Untitled" } }, color = vc.onSurface, fontSize = 13.sp, maxLines = 1)
                            if (note.content.isNotEmpty() && note.title.isNotEmpty()) Text(note.content.take(50), color = vc.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = vc.onSurfaceVariant.copy(.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        HorizontalDivider(color = vc.outline.copy(.3f), modifier = Modifier.padding(horizontal = 16.dp))
    }
}

// Note: TagsScreen uses VaultToastHost from the navigation host above.
// ToastManager is global and works across all screens.
