package com.vaultapp.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.vaultapp.MainActivity

private val BG        = Color(0xFF1E1E2E)
private val PRIMARY   = Color(0xFF7C6AF5)
private val SURFACE   = Color(0xFF2A1A50)
private val SUBTITLE  = Color(0xFF9080FF)
private val WHITE     = Color.White

class QuickAddWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }

    @Composable
    private fun WidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                // FIX #5: use background(Color) overload supported by Glance
                .background(BG)
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                // Header
                Text(
                    text = "🔐  Vault",
                    style = TextStyle(
                        color      = ColorProvider(WHITE),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(10.dp))

                // Quick note button
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(PRIMARY)
                        .cornerRadius(12.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable(
                            actionStartActivity<QuickAddWidgetActivity>(
                                actionParametersOf(ActionParameters.Key<String>("type") to "note")
                            )
                        )
                ) {
                    Text("✏️  Quick note",
                        style = TextStyle(color = ColorProvider(WHITE), fontSize = 13.sp))
                }
                Spacer(GlanceModifier.height(6.dp))

                // Password button
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(SURFACE)
                        .cornerRadius(12.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable(
                            actionStartActivity<QuickAddWidgetActivity>(
                                actionParametersOf(ActionParameters.Key<String>("type") to "password")
                            )
                        )
                ) {
                    Text("🔑  Add password",
                        style = TextStyle(color = ColorProvider(SUBTITLE), fontSize = 13.sp))
                }
                Spacer(GlanceModifier.height(6.dp))

                // Open app button
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(Color(0xFF151520))
                        .cornerRadius(12.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    Text("Open Vault →",
                        style = TextStyle(color = ColorProvider(PRIMARY), fontSize = 12.sp))
                }
            }
        }
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

class QuickAddWidgetActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent?.getStringExtra("type") ?: "note"
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra("quick_add_type", type)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        )
        finish()
    }
}
