package com.vaultapp.ui.screens.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultapp.ui.theme.vaultColors

// ── Main editor composable ─────────────────────────────────────────────────────
@Composable
fun RichTextEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
    onContentChange: (String) -> Unit = {}
) {
    val vc = MaterialTheme.vaultColors
    Column(modifier = modifier) {
        TextEditor(state, vc, onContentChange)
        if (state.checklistItems.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            ChecklistEditor(state, vc)
        }
    }
}

// ── Plain / rich text editor with live span rendering ─────────────────────────
@Composable
private fun TextEditor(
    state: RichTextState,
    vc: com.vaultapp.ui.theme.VaultColors,
    onContentChange: (String) -> Unit
) {
    val fr = remember { FocusRequester() }

    BasicTextField(
        value          = state.textFieldValue,
        onValueChange  = { newVal ->
            state.onTextChanged(newVal)
            onContentChange(state.serializeToJson())
        },
        textStyle      = TextStyle(
            color      = vc.onSurface,
            fontSize   = 16.sp,
            lineHeight  = 26.sp
        ),
        cursorBrush    = SolidColor(vc.primary),
        visualTransformation = { text ->
            androidx.compose.ui.text.input.TransformedText(
                state.buildAnnotatedString(),
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        },
        modifier       = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 300.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .focusRequester(fr),
        decorationBox  = { inner ->
            if (state.textFieldValue.text.isEmpty()) {
                Text("Start writing…", color = vc.onSurfaceVariant.copy(.4f), fontSize = 16.sp)
            }
            inner()
        }
    )
}

// ── Checklist editor ──────────────────────────────────────────────────────────
@Composable
fun ChecklistEditor(state: RichTextState, vc: com.vaultapp.ui.theme.VaultColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        state.checklistItems.forEachIndexed { idx, item ->
            ChecklistRow(
                item      = item,
                vc        = vc,
                onText    = { state.updateChecklistItem(item.id, text = it) },
                onChecked = { state.updateChecklistItem(item.id, checked = it) },
                onDelete  = { state.removeChecklistItem(item.id) },
                onAddNext = { state.checklistItems.add(idx + 1, ChecklistItem()) }
            )
        }
        // Add item button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { state.addChecklistItem() }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AddCircleOutline, null, tint = vc.primary.copy(.6f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Add item", color = vc.onSurfaceVariant.copy(.6f), fontSize = 15.sp)
        }
        // Completed count
        val doneCount = state.checklistItems.count { it.isChecked }
        if (doneCount > 0) {
            HorizontalDivider(color = vc.outline.copy(.4f)); Spacer(Modifier.height(4.dp))
            Text("$doneCount completed", color = vc.onSurfaceVariant, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    vc: com.vaultapp.ui.theme.VaultColors,
    onText: (String) -> Unit,
    onChecked: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onAddNext: () -> Unit
) {
    val fr = remember { FocusRequester() }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, if (item.isChecked) vc.primary else vc.onSurfaceVariant.copy(.4f), CircleShape)
                .background(if (item.isChecked) vc.primary else Color.Transparent)
                .clickable { onChecked(!item.isChecked) },
            contentAlignment = Alignment.Center
        ) {
            if (item.isChecked) {
                Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value          = item.text,
            onValueChange  = onText,
            textStyle      = TextStyle(
                color          = if (item.isChecked) vc.onSurfaceVariant.copy(.5f) else vc.onSurface,
                fontSize       = 15.sp,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
            ),
            cursorBrush    = SolidColor(vc.primary),
            modifier       = Modifier.weight(1f).focusRequester(fr),
            decorationBox  = { inner ->
                if (item.text.isEmpty()) Text("List item", color = vc.onSurfaceVariant.copy(.3f), fontSize = 15.sp)
                inner()
            }
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Close, null, tint = vc.onSurfaceVariant.copy(.4f), modifier = Modifier.size(16.dp))
        }
    }
}

// ── Formatting toolbar ─────────────────────────────────────────────────────────
@Composable
fun FormattingToolbar(
    state: RichTextState,
    onColorPicker: () -> Unit,
    onAddMedia: () -> Unit,
    onCamera: () -> Unit,
    onVoice: () -> Unit,
    onTags: () -> Unit
) {
    val vc = MaterialTheme.vaultColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo/Redo buttons
            FmtBtn(Icons.Outlined.Undo, "Undo", false, vc, enabled = state.canUndo()) { state.undo() }
            FmtBtn(Icons.Outlined.Redo, "Redo", false, vc, enabled = state.canRedo()) { state.redo() }
            
            // Divider
            Box(modifier = Modifier.width(1.dp).height(20.dp).background(vc.outline.copy(0.3f)))
            
            FmtBtn(Icons.Outlined.FormatBold,        "Bold",        state.isFormatActive(SpanType.BOLD),          vc) { state.toggleFormat(SpanType.BOLD) }
            FmtBtn(Icons.Outlined.FormatItalic,      "Italic",      state.isFormatActive(SpanType.ITALIC),        vc) { state.toggleFormat(SpanType.ITALIC) }
            FmtBtn(Icons.Outlined.FormatUnderlined,  "Underline",   state.isFormatActive(SpanType.UNDERLINE),     vc) { state.toggleFormat(SpanType.UNDERLINE) }
            FmtBtn(Icons.Outlined.FormatStrikethrough,"Strike",     state.isFormatActive(SpanType.STRIKETHROUGH), vc) { state.toggleFormat(SpanType.STRIKETHROUGH) }
            FmtBtn(Icons.Outlined.Code,              "Code",        state.isFormatActive(SpanType.CODE),          vc) { state.toggleFormat(SpanType.CODE) }
            FmtBtn(Icons.AutoMirrored.Outlined.FormatListBulleted, "List", false, vc) {
                val cur = state.textFieldValue.selection.start
                val t   = state.textFieldValue.text
                state.onTextChanged(state.textFieldValue.copy(text = t.substring(0, cur) + "\n• " + t.substring(cur)))
            }
            FmtBtn(Icons.Outlined.CheckBox, "Checklist", false, vc) { state.addChecklistItem() }
        }

        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TBtn(Icons.Outlined.Image,    "Gallery",  vc, onAddMedia)
            TBtn(Icons.Outlined.CameraAlt,"Camera",   vc, onCamera)
            TBtn(Icons.Outlined.Mic,      "Voice",    vc, onVoice)
            TBtn(Icons.Outlined.Palette,  "Color",    vc, onColorPicker)
            TBtn(Icons.Outlined.Tag,      "Tags",     vc, onTags)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
@Composable
fun ModeBtn(label: String = "", icon: ImageVector? = null, active: Boolean, vc: com.vaultapp.ui.theme.VaultColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) vc.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) Icon(icon, null, tint = if (active) vc.primary else vc.onSurfaceVariant, modifier = Modifier.size(18.dp))
        else Text(label, color = if (active) vc.primary else vc.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FmtBtn(icon: ImageVector, desc: String, active: Boolean, vc: com.vaultapp.ui.theme.VaultColors, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) vc.primaryContainer else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            desc,
            tint = if (!enabled) vc.onSurfaceVariant.copy(0.3f) else if (active) vc.primary else vc.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
fun TBtn(icon: ImageVector, desc: String, vc: com.vaultapp.ui.theme.VaultColors, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, desc, tint = vc.onSurfaceVariant, modifier = Modifier.size(19.dp))
    }
}
