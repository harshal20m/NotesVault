package com.vaultapp.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.*
import com.vaultapp.ui.theme.VaultColors
import com.vaultapp.ui.theme.vaultColors

fun createImageUri(context: Context): Uri {
    val cv = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "vault_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        ?: error("Cannot create image URI")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MediaCaptureBar(onImageCaptured: (Uri) -> Unit, onImagePicked: (Uri) -> Unit, vc: VaultColors) {
    val ctx = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val camPerm = rememberPermissionState(Manifest.permission.CAMERA)
    val stoPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    else rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) tempUri?.let { onImageCaptured(it) }
    }
    val galLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { onImagePicked(it) }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Triple(Icons.Outlined.CameraAlt, "Camera", vc.primary),
            Triple(Icons.Outlined.Image, "Gallery", Color(0xFF1D9E75)),
            Triple(Icons.Outlined.Mic, "Audio", Color(0xFFD4537E)),
            Triple(Icons.Outlined.AttachFile, "File", Color(0xFFBA7517))
        ).forEach { (icon, label, color) ->
            Column(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(.12f))
                .clickable {
                    when (label) {
                        "Camera" -> if (camPerm.status.isGranted) { tempUri = createImageUri(ctx); camLauncher.launch(tempUri!!) } else camPerm.launchPermissionRequest()
                        "Gallery" -> if (stoPerm.status.isGranted) galLauncher.launch("image/*") else stoPerm.launchPermissionRequest()
                        else -> galLauncher.launch("*/*")
                    }
                }.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(4.dp))
                Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun NoteMediaGrid(mediaUris: List<String>, onRemove: (String) -> Unit, onFullScreen: (String) -> Unit) {
    if (mediaUris.isEmpty()) return
    val vc = MaterialTheme.vaultColors
    val ctx = LocalContext.current
    val validUris = remember(mediaUris) {
        mediaUris.filter { uriString ->
            runCatching {
                ctx.contentResolver.openInputStream(Uri.parse(uriString))?.use { true } ?: false
            }.getOrDefault(false)
        }
    }
    if (validUris.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        validUris.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { uriString ->
                    val uri = Uri.parse(uriString)
                    val isPdf = ctx.contentResolver.getType(uri)?.contains("pdf") == true || uriString.endsWith(".pdf", true)
                    
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(vc.surfaceVariant).clickable { onFullScreen(uriString) }) {
                        if (isPdf) {
                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Outlined.PictureAsPdf, null, tint = Color(0xFFE24B4A), modifier = Modifier.size(32.dp))
                                Text("PDF", color = vc.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(uriString).crossfade(true).build(),
                                contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).clip(CircleShape)
                            .background(Color.Black.copy(.6f)).clickable { onRemove(uriString) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(noteId: Long, onBack: () -> Unit) {
    val vc = MaterialTheme.vaultColors
    val mediaItems = remember { mutableStateListOf<String>() }
    var showCapture by remember { mutableStateOf(false) }
    var fullUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground) } },
                title = { Column { Text("Media", color = vc.onBackground, fontWeight = FontWeight.SemiBold); if (mediaItems.isNotEmpty()) Text("${mediaItems.size} items", color = vc.onSurfaceVariant, fontSize = 12.sp) } },
                actions = { IconButton(onClick = { showCapture = !showCapture }) { Icon(Icons.Outlined.AddPhotoAlternate, null, tint = vc.primary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(vc.background).padding(padding)) {
            AnimatedVisibility(visible = showCapture, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.background(vc.surface).padding(vertical = 8.dp)) {
                    MediaCaptureBar(onImageCaptured = { mediaItems.add(it.toString()); showCapture = false },
                        onImagePicked = { mediaItems.add(it.toString()); showCapture = false }, vc = vc)
                }
            }
            if (mediaItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼️", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                        Text("No media yet", color = vc.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Add photos, videos or audio", color = vc.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { showCapture = true }, colors = ButtonDefaults.buttonColors(containerColor = vc.primary)) {
                            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Add media")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp), verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxSize()) {
                    items(mediaItems) { uri ->
                        Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(vc.surfaceVariant).clickable { fullUri = uri }) {
                            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
                                contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(18.dp).clip(CircleShape)
                                .background(Color.Black.copy(.5f)).clickable { mediaItems.remove(uri) }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                    item {
                        Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(vc.surfaceVariant)
                            .border(1.dp, vc.outline, RoundedCornerShape(8.dp)).clickable { showCapture = true }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Add, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
    fullUri?.let { uri ->
        Dialog(onDismissRequest = { fullUri = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(uri).build(), contentDescription = null,
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                IconButton(onClick = { fullUri = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
