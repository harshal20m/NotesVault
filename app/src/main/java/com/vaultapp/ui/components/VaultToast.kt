package com.vaultapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.vaultapp.ui.theme.vaultColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ── Toast model ────────────────────────────────────────────────────────────────
enum class ToastType { SUCCESS, ERROR, INFO, WARNING }

data class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.SUCCESS,
    val icon: ImageVector? = null
)

// ── Global toast event bus ─────────────────────────────────────────────────────
object ToastManager {
    private val _events = MutableSharedFlow<ToastMessage>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    fun show(msg: String, type: ToastType = ToastType.SUCCESS, icon: ImageVector? = null) {
        _events.tryEmit(ToastMessage(msg, type, icon))
    }
    fun success(msg: String) = show(msg, ToastType.SUCCESS, Icons.Outlined.CheckCircle)
    fun error(msg: String)   = show(msg, ToastType.ERROR,   Icons.Outlined.ErrorOutline)
    fun info(msg: String)    = show(msg, ToastType.INFO,    Icons.Outlined.Info)
    fun warning(msg: String) = show(msg, ToastType.WARNING, Icons.Outlined.WarningAmber)
    fun copied()             = show("Copied to clipboard", ToastType.SUCCESS, Icons.Outlined.ContentCopy)
    fun saved()              = show("Note saved", ToastType.SUCCESS, Icons.Outlined.CheckCircle)
    fun deleted()            = show("Moved to trash", ToastType.INFO, Icons.Outlined.Delete)
    fun restored()           = show("Note restored", ToastType.SUCCESS, Icons.Outlined.RestoreFromTrash)
    fun locked()             = show("Note locked", ToastType.INFO, Icons.Outlined.Lock)
    fun unlocked()           = show("Note unlocked", ToastType.INFO, Icons.Outlined.LockOpen)
    fun pinned()             = show("Note pinned", ToastType.SUCCESS, Icons.Outlined.PushPin)
    fun unpinned()           = show("Note unpinned", ToastType.INFO, Icons.Outlined.PushPin)
    fun tagAdded(tag: String)   = show("Tag \"#$tag\" added", ToastType.SUCCESS, Icons.Outlined.Tag)
    fun tagRemoved(tag: String) = show("Tag \"#$tag\" removed", ToastType.INFO, Icons.Outlined.Tag)
    fun backupDone()         = show("Backup saved", ToastType.SUCCESS, Icons.Outlined.CloudDone)
    fun importDone(n: Int, p: Int) = show("Imported $n notes · $p passwords", ToastType.SUCCESS, Icons.Outlined.CloudDownload)
}

// ── Toast host composable — place at root of screen ───────────────────────────
@Composable
fun VaultToastHost(content: @Composable () -> Unit) {
    var current by remember { mutableStateOf<ToastMessage?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ToastManager.events.collect { msg ->
            // dismiss current then show new
            visible = false
            delay(120)
            current = msg
            visible = true
            delay(2_600)
            visible = false
            delay(400)
            current = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        // FIX: toast slides from left, sits top-centre, exits right
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .zIndex(100f),
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec  = spring(dampingRatio = .7f, stiffness = 400f)
            ) + fadeIn(),
            exit  = slideOutHorizontally(
                targetOffsetX  = { it },
                animationSpec  = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            current?.let { toast ->
                ToastPill(toast)
            }
        }
    }
}

@Composable
private fun ToastPill(toast: ToastMessage) {
    val vc = MaterialTheme.vaultColors
    val (bg, fg) = when (toast.type) {
        ToastType.SUCCESS -> Pair(Color(0xFF1E2A24), Color(0xFFCDEBDD))
        ToastType.ERROR   -> Pair(Color(0xFF2E1E1E), Color(0xFFF0C7C7))
        ToastType.WARNING -> Pair(Color(0xFF2F2A20), Color(0xFFF2DFB3))
        ToastType.INFO    -> Pair(vc.surfaceVariant.copy(alpha = 0.94f),  vc.onSurface)
    }
    Row(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        toast.icon?.let { icon ->
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        }
        Text(toast.message, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
