package com.vaultapp.ui.screens.editor

import androidx.compose.runtime.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

enum class SpanType { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, CODE, HEADING1, HEADING2 }
data class TextSpan(val start: Int, val end: Int, val type: SpanType)
data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)
enum class EditorMode { TEXT, CHECKLIST }

class RichTextState {
    var textFieldValue by mutableStateOf(TextFieldValue())
    val spans          = mutableStateListOf<TextSpan>()
    val checklistItems = mutableStateListOf<ChecklistItem>()
    var editorMode     by mutableStateOf(EditorMode.TEXT)
    // FIX: use mutableStateListOf not mutableStateSetOf (doesn't exist)
    val activeFormats  = mutableStateListOf<SpanType>()
    
    // Callback to notify when content changes (including formatting)
    var onContentChanged: ((String) -> Unit)? = null
    
    // Undo/Redo stacks
    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()
    private var isUndoRedoOperation = false
    
    data class EditorSnapshot(
        val text: String,
        val spans: List<TextSpan>,
        val checklistItems: List<ChecklistItem>,
        val selection: androidx.compose.ui.text.TextRange
    )
    
    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()

    private fun saveSnapshot() {
        if (isUndoRedoOperation) return
        
        val snapshot = EditorSnapshot(
            text = textFieldValue.text,
            spans = spans.toList(),
            checklistItems = checklistItems.toList(),
            selection = textFieldValue.selection
        )
        undoStack.add(snapshot)
        
        // Limit undo stack to 50 items
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        
        // Clear redo stack when new change is made
        redoStack.clear()
    }
    
    fun undo() {
        if (undoStack.isEmpty()) return
        
        isUndoRedoOperation = true
        
        // Save current state to redo stack
        val currentSnapshot = EditorSnapshot(
            text = textFieldValue.text,
            spans = spans.toList(),
            checklistItems = checklistItems.toList(),
            selection = textFieldValue.selection
        )
        redoStack.add(currentSnapshot)
        
        // Restore previous state
        val snapshot = undoStack.removeAt(undoStack.lastIndex)
        textFieldValue = TextFieldValue(snapshot.text, snapshot.selection)
        spans.clear()
        spans.addAll(snapshot.spans)
        checklistItems.clear()
        checklistItems.addAll(snapshot.checklistItems)
        
        isUndoRedoOperation = false
        onContentChanged?.invoke(serializeToJson())
    }
    
    fun redo() {
        if (redoStack.isEmpty()) return
        
        isUndoRedoOperation = true
        
        // Save current state to undo stack
        val currentSnapshot = EditorSnapshot(
            text = textFieldValue.text,
            spans = spans.toList(),
            checklistItems = checklistItems.toList(),
            selection = textFieldValue.selection
        )
        undoStack.add(currentSnapshot)
        
        // Restore next state
        val snapshot = redoStack.removeAt(redoStack.lastIndex)
        textFieldValue = TextFieldValue(snapshot.text, snapshot.selection)
        spans.clear()
        spans.addAll(snapshot.spans)
        checklistItems.clear()
        checklistItems.addAll(snapshot.checklistItems)
        
        isUndoRedoOperation = false
        onContentChanged?.invoke(serializeToJson())
    }
    
    fun buildAnnotatedString(): AnnotatedString = buildAnnotatedString {
        append(textFieldValue.text)
        spans.forEach { span ->
            if (span.start < span.end && span.end <= textFieldValue.text.length) {
                when (span.type) {
                    SpanType.BOLD          -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), span.start, span.end)
                    SpanType.ITALIC        -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), span.start, span.end)
                    SpanType.UNDERLINE     -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), span.start, span.end)
                    SpanType.STRIKETHROUGH -> addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), span.start, span.end)
                    SpanType.CODE          -> addStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp), span.start, span.end)
                    SpanType.HEADING1      -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp), span.start, span.end)
                    SpanType.HEADING2      -> addStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp), span.start, span.end)
                }
            }
        }
    }

    fun toggleFormat(type: SpanType) {
        saveSnapshot()
        
        val sel = textFieldValue.selection
        if (sel.collapsed) {
            // toggle active format for typing
            if (activeFormats.contains(type)) activeFormats.remove(type) else activeFormats.add(type)
            return
        }
        val existing = spans.firstOrNull { it.start == sel.start && it.end == sel.end && it.type == type }
        if (existing != null) spans.remove(existing) else spans.add(TextSpan(sel.start, sel.end, type))
        
        // Notify content changed
        onContentChanged?.invoke(serializeToJson())
    }

    fun isFormatActive(type: SpanType) = activeFormats.contains(type)

    fun addChecklistItem(text: String = "") {
        saveSnapshot()
        checklistItems.add(ChecklistItem(text = text))
        onContentChanged?.invoke(serializeToJson())
    }

    fun updateChecklistItem(id: String, text: String? = null, checked: Boolean? = null) {
        val idx = checklistItems.indexOfFirst { it.id == id }
        if (idx >= 0) {
            saveSnapshot()
            checklistItems[idx] = checklistItems[idx].copy(
                text    = text    ?: checklistItems[idx].text,
                isChecked = checked ?: checklistItems[idx].isChecked
            )
            // Notify content changed
            onContentChanged?.invoke(serializeToJson())
        }
    }

    fun removeChecklistItem(id: String) {
        saveSnapshot()
        checklistItems.removeAll { it.id == id }
        // Notify content changed
        onContentChanged?.invoke(serializeToJson())
    }
    
    fun onTextChanged(newValue: TextFieldValue) {
        if (!isUndoRedoOperation && newValue.text != textFieldValue.text) {
            saveSnapshot()
        }
        textFieldValue = newValue
    }

    fun serializeToJson(): String {
        val gson = com.google.gson.Gson()
        return gson.toJson(mapOf(
            "text"      to textFieldValue.text,
            "mode"      to editorMode.name,
            "checklist" to checklistItems.toList(),
            "spans"     to spans.toList()
        ))
    }

    fun loadFromJson(json: String) {
        runCatching {
            val map = com.google.gson.Gson().fromJson(json, Map::class.java)
            val text = map["text"] as? String ?: json
            textFieldValue = TextFieldValue(text)
            
            (map["mode"] as? String)?.let { m ->
                runCatching { editorMode = EditorMode.valueOf(m) }
            }
            // restore spans
            @Suppress("UNCHECKED_CAST")
            (map["spans"] as? List<Map<String,Any>>)?.let { list ->
                spans.clear()
                list.forEach { item ->
                    val type = runCatching { SpanType.valueOf(item["type"] as String) }.getOrNull()
                    if (type != null) {
                        spans.add(TextSpan(
                            start = (item["start"] as Double).toInt(),
                            end   = (item["end"] as Double).toInt(),
                            type  = type
                        ))
                    }
                }
            }
            // restore checklist items
            @Suppress("UNCHECKED_CAST")
            (map["checklist"] as? List<Map<String,Any>>)?.let { list ->
                checklistItems.clear()
                list.forEach { item ->
                    checklistItems.add(ChecklistItem(
                        text      = item["text"] as? String ?: "",
                        isChecked = (item["isChecked"] as? Boolean) ?: false
                    ))
                }
            }
        }.onFailure { textFieldValue = TextFieldValue(json) }
    }
}
