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
        val sel = textFieldValue.selection
        if (sel.collapsed) {
            // toggle active format for typing
            if (activeFormats.contains(type)) activeFormats.remove(type) else activeFormats.add(type)
            return
        }
        val existing = spans.firstOrNull { it.start == sel.start && it.end == sel.end && it.type == type }
        if (existing != null) spans.remove(existing) else spans.add(TextSpan(sel.start, sel.end, type))
    }

    fun isFormatActive(type: SpanType) = activeFormats.contains(type)

    fun addChecklistItem(text: String = "") { checklistItems.add(ChecklistItem(text = text)) }

    fun updateChecklistItem(id: String, text: String? = null, checked: Boolean? = null) {
        val idx = checklistItems.indexOfFirst { it.id == id }
        if (idx >= 0) checklistItems[idx] = checklistItems[idx].copy(
            text    = text    ?: checklistItems[idx].text,
            isChecked = checked ?: checklistItems[idx].isChecked
        )
    }

    fun removeChecklistItem(id: String) { checklistItems.removeAll { it.id == id } }

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
