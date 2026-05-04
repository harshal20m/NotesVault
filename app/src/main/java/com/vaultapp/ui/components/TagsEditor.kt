package com.vaultapp.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultapp.ui.theme.vaultColors

private val SUGGESTED = listOf("work","personal","ideas","todo","important","finance","health","travel","shopping","journal","code","meeting","reading","goals","family","hobby")

@Composable
fun TagsEditor(tags: List<String>, onTagsChanged: (List<String>) -> Unit, modifier: Modifier = Modifier) {
    val vc = MaterialTheme.vaultColors
    var input by remember { mutableStateOf("") }
    val filtered = if (input.length >= 1) SUGGESTED.filter { it.startsWith(input, true) && !tags.contains(it) }
                   else SUGGESTED.filter { !tags.contains(it) }.take(8)

    fun add(tag: String) {
        val c = tag.trim().lowercase().replace(" ","_")
        if (c.isNotEmpty() && !tags.contains(c)) onTagsChanged(tags + c)
        input = ""
    }

    Column(modifier = modifier) {
        if (tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                items(tags) { tag ->
                    Row(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(vc.primaryContainer)
                        .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("#$tag", color = vc.primary, fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(8.dp)).background(vc.primary.copy(.2f))
                            .clickable { onTagsChanged(tags - tag) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Close, null, tint = vc.primary, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(vc.surfaceVariant).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Tag, null, tint = vc.primary.copy(.7f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            BasicTextField(value = input, onValueChange = { input = it }, singleLine = true,
                textStyle = TextStyle(color = vc.onSurface, fontSize = 14.sp), cursorBrush = SolidColor(vc.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { add(input) }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner -> if (input.isEmpty()) Text("Add tag…", color = vc.onSurfaceVariant.copy(.5f), fontSize = 14.sp); inner() })
            if (input.isNotEmpty()) TextButton(onClick = { add(input) }, modifier = Modifier.height(28.dp)) { Text("Add", color = vc.primary, fontSize = 12.sp) }
        }
        AnimatedVisibility(visible = filtered.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(filtered.take(10)) { s ->
                    SuggestionChip(onClick = { add(s) }, label = { Text("#$s", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = vc.surfaceVariant, labelColor = vc.onSurfaceVariant),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = vc.outline))
                }
            }
        }
    }
}
