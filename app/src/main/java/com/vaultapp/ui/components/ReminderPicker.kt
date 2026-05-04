package com.vaultapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultapp.ui.theme.vaultColors
import java.text.SimpleDateFormat
import java.util.*

enum class ReminderPreset(val label: String, val icon: ImageVector) {
    IN_30_MIN("In 30 minutes", Icons.Outlined.Timer),
    IN_1_HOUR("In 1 hour", Icons.Outlined.Schedule),
    TODAY_EVENING("Today evening (7 PM)", Icons.Outlined.WbTwilight),
    TOMORROW_MORNING("Tomorrow morning (9 AM)", Icons.Outlined.WbSunny),
    NEXT_WEEK("Next week", Icons.Outlined.DateRange);
    fun millis(): Long {
        val c = Calendar.getInstance()
        return when (this) {
            IN_30_MIN        -> System.currentTimeMillis() + 30 * 60_000
            IN_1_HOUR        -> System.currentTimeMillis() + 60 * 60_000
            TODAY_EVENING    -> c.apply { set(Calendar.HOUR_OF_DAY,19); set(Calendar.MINUTE,0); set(Calendar.SECOND,0) }.timeInMillis
            TOMORROW_MORNING -> c.apply { add(Calendar.DAY_OF_YEAR,1); set(Calendar.HOUR_OF_DAY,9); set(Calendar.MINUTE,0) }.timeInMillis
            NEXT_WEEK        -> c.apply { add(Calendar.DAY_OF_YEAR,7); set(Calendar.HOUR_OF_DAY,9); set(Calendar.MINUTE,0) }.timeInMillis
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(currentReminder: Long?, onConfirm: (Long?) -> Unit, onDismiss: () -> Unit) {
    val vc  = MaterialTheme.vaultColors
    val df  = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    var sel by remember { mutableStateOf<ReminderPreset?>(null) }
    var ms  by remember { mutableStateOf(currentReminder) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val dps = rememberDatePickerState(initialSelectedDateMillis = currentReminder ?: System.currentTimeMillis())
    val tps = rememberTimePickerState(initialHour = 9, initialMinute = 0)

    AlertDialog(onDismissRequest = onDismiss, containerColor = vc.surface, shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Alarm, null, tint = vc.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Set reminder", color = vc.onBackground, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Quick options", color = vc.onSurfaceVariant, fontSize = 12.sp)
                ReminderPreset.values().forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (sel == p) vc.primaryContainer else Color.Transparent)
                        .clickable { sel = p; ms = p.millis() }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(p.icon, null, tint = if (sel == p) vc.primary else vc.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.label, color = if (sel == p) vc.primary else vc.onBackground, fontSize = 14.sp)
                            Text(df.format(Date(p.millis())), color = vc.onSurfaceVariant, fontSize = 11.sp)
                        }
                        if (sel == p) Icon(Icons.Outlined.Check, null, tint = vc.primary, modifier = Modifier.size(16.dp))
                    }
                }
                HorizontalDivider(color = vc.outline.copy(.4f), modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(vc.surfaceVariant)
                    .clickable { showDate = true }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = vc.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Custom date & time", color = vc.onBackground, fontSize = 14.sp)
                        ms?.let { Text(df.format(Date(it)), color = vc.primary, fontSize = 12.sp) }
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                if (currentReminder != null) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onConfirm(null) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.NotificationsOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Remove reminder", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(ms) }, colors = ButtonDefaults.buttonColors(containerColor = vc.primary), enabled = ms != null) { Text("Set reminder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = vc.onSurfaceVariant) } }
    )

    if (showDate) {
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = {
            TextButton(onClick = { showDate = false; showTime = true; dps.selectedDateMillis?.let { ms = it; sel = null } }) { Text("Next", color = vc.primary) }
        }, colors = DatePickerDefaults.colors(containerColor = vc.surface)) {
            DatePicker(state = dps, colors = DatePickerDefaults.colors(selectedDayContainerColor = vc.primary, todayDateBorderColor = vc.primary))
        }
    }
    if (showTime) {
        AlertDialog(onDismissRequest = { showTime = false }, containerColor = vc.surface,
            title = { Text("Pick time", color = vc.onBackground) },
            text = { TimePicker(state = tps, colors = TimePickerDefaults.colors(selectorColor = vc.primary)) },
            confirmButton = {
                TextButton(onClick = {
                    showTime = false
                    ms = Calendar.getInstance().apply {
                        timeInMillis = ms ?: System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, tps.hour); set(Calendar.MINUTE, tps.minute); set(Calendar.SECOND, 0)
                    }.timeInMillis
                }) { Text("Confirm", color = vc.primary) }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancel", color = vc.onSurfaceVariant) } }
        )
    }
}
