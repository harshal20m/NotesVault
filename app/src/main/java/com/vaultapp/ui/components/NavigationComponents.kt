package com.vaultapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultapp.ui.theme.VaultColors

@Composable
fun FloatingBottomNav(
    selectedIndex: Int,
    onHome: () -> Unit,
    onVault: () -> Unit,
    onAnalytics: () -> Unit,
    onSettings: () -> Unit,
    vc: VaultColors,
    modifier: Modifier = Modifier
) {
    val navItems = remember {
        listOf(
            Triple(Icons.Filled.GridView,      Icons.Outlined.GridView,      "Home"),
            Triple(Icons.Filled.Lock,          Icons.Outlined.Lock,          "Vault"),
            Triple(Icons.Filled.BarChart,      Icons.Outlined.BarChart,      "Insights"),
            Triple(Icons.Filled.Person,        Icons.Outlined.Person,        "Settings")
        )
    }
    val actions = remember(onHome, onVault, onAnalytics, onSettings) {
        listOf(onHome, onVault, onAnalytics, onSettings)
    }

    Row(
        modifier              = modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = 20.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .background(vc.surface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        navItems.forEachIndexed { i, (filledIcon, outlineIcon, label) ->
            val selected = selectedIndex == i
            Column(
                modifier              = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = actions[i])
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) vc.primaryContainer else Color.Transparent)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector       = if (selected) filledIcon else outlineIcon,
                        contentDescription = label,
                        tint              = if (selected) vc.primary else vc.onSurfaceVariant,
                        modifier          = Modifier.size(22.dp)
                    )
                }
                Text(
                    label,
                    color    = if (selected) vc.primary else vc.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
