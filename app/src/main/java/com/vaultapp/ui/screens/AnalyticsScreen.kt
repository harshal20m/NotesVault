package com.vaultapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vaultapp.ui.theme.vaultColors
import com.vaultapp.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val vc = MaterialTheme.vaultColors
    val pwStats by viewModel.passwordStats.collectAsStateWithLifecycle()
    val noteStats by viewModel.noteStats.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = vc.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = vc.onBackground)
                    }
                },
                title = { Text("Insights", color = vc.onBackground, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = vc.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 100.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Security Overview", color = vc.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            StatCard(
                title = "Passwords",
                icon = Icons.Outlined.Lock,
                color = vc.primary,
                vc = vc
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Total Logins", pwStats.total.toString(), vc)
                    StatRow("Most Used Category", pwStats.mostActiveCategory.lowercase().replaceFirstChar { it.uppercase() }, vc)
                    SecurityBar(pwStats, vc)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStat("Strong", pwStats.strong.toString(), Color(0xFF1D9E75), Modifier.weight(1f))
                        MiniStat("Weak", pwStats.weak.toString(), Color(0xFFE24B4A), Modifier.weight(1f))
                    }
                }
            }

            Text("Content Overview", color = vc.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            StatCard(
                title = "Notes & Media",
                icon = Icons.Outlined.StickyNote2,
                color = Color(0xFFEF9F27),
                vc = vc
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GridStat("Total Notes", noteStats.total.toString(), Icons.Outlined.Description, vc, Modifier.weight(1f))
                        GridStat("Pinned", noteStats.pinned.toString(), Icons.Outlined.PushPin, vc, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GridStat("Encrypted", noteStats.locked.toString(), Icons.Outlined.Https, vc, Modifier.weight(1f))
                        GridStat("With Media", noteStats.withMedia.toString(), Icons.Outlined.Image, vc, Modifier.weight(1f))
                    }
                    GridStat("Unique Tags", noteStats.tagCount.toString(), Icons.Outlined.Tag, vc, Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    icon: ImageVector,
    color: Color,
    vc: com.vaultapp.ui.theme.VaultColors,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = vc.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, vc.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, color = vc.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, vc: com.vaultapp.ui.theme.VaultColors) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = vc.onSurfaceVariant, fontSize = 14.sp)
        Text(value, color = vc.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(0.1f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = color.copy(0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun GridStat(label: String, value: String, icon: ImageVector, vc: com.vaultapp.ui.theme.VaultColors, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(vc.surfaceVariant.copy(0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = vc.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, color = vc.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = vc.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SecurityBar(stats: com.vaultapp.ui.viewmodel.PasswordStats, vc: com.vaultapp.ui.theme.VaultColors) {
    val total = if (stats.total == 0) 1 else stats.total
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
            if (stats.total == 0) {
                Box(Modifier.fillMaxSize().background(vc.outline))
            } else {
                if (stats.strong > 0) Box(Modifier.weight(stats.strong.toFloat()).fillMaxHeight().background(Color(0xFF1D9E75)))
                if (stats.medium > 0) Box(Modifier.weight(stats.medium.toFloat()).fillMaxHeight().background(vc.primary))
                if (stats.fair > 0) Box(Modifier.weight(stats.fair.toFloat()).fillMaxHeight().background(Color(0xFFEF9F27)))
                if (stats.weak > 0) Box(Modifier.weight(stats.weak.toFloat()).fillMaxHeight().background(Color(0xFFE24B4A)))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Strength Distribution", color = vc.onSurfaceVariant, fontSize = 11.sp)
            Text("${((stats.strong + stats.medium).toFloat() / total * 100).toInt()}% Secure", color = Color(0xFF1D9E75), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
