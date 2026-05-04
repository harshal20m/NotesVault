package com.vaultapp.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareAsWhatsAppText(context: Context, title: String, content: String) {
        val text = buildShareText(title, content)
        runCatching {
            context.startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text); setPackage("com.whatsapp")
            })
        }.onFailure { shareAsText(context, title, content) }
    }

    fun shareAsText(context: Context, title: String, content: String) {
        context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, buildShareText(title, content)) },
            "Share note"
        ))
    }

    fun copyToClipboard(context: Context, title: String, content: String) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("vault_note", buildShareText(title, content)))
    }

    fun shareAsImage(context: Context, title: String, content: String, bgHex: String = "#1E1E2E") {
        val uri = renderToUri(context, title, content, bgHex)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); setPackage("com.whatsapp")
        }
        runCatching { context.startActivity(intent) }.onFailure {
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, "Share note image"))
        }
    }

    fun shareAsImageGeneric(context: Context, title: String, content: String, bgHex: String = "#1E1E2E") {
        val uri = renderToUri(context, title, content, bgHex)
        context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share note image"
        ))
    }

    private fun renderToUri(context: Context, title: String, content: String, bgHex: String): Uri {
        val bmp  = renderNoteCard(title, content, bgHex)
        val file = File(context.cacheDir, "vault_note_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun renderNoteCard(title: String, content: String, bgHex: String): Bitmap {
        val W = 1080; val PAD = 72f; val maxW = (W - PAD * 2).toInt()
        val bgP     = Paint().apply { color = Color.parseColor(bgHex); isAntiAlias = true }
        val accentP = Paint().apply { color = Color.parseColor("#7C6AF5") }
        val brandP  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9080FF"); textSize = 30f }
        val titleP  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 52f; typeface = Typeface.DEFAULT_BOLD }
        val bodyP   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C8C8E0"); textSize = 38f }
        val timeP   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6B6B8A"); textSize = 28f }
        val titleLines = if (title.isNotEmpty()) wrap(title, titleP, maxW) else emptyList()
        val bodyLines  = wrap(content.take(600), bodyP, maxW)
        val H = (PAD * 2 + 70f + titleLines.size * 64f + bodyLines.size * 50f + 60f).toInt().coerceAtLeast(380)
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c   = Canvas(bmp)
        c.drawRoundRect(0f, 0f, W.toFloat(), H.toFloat(), 48f, 48f, bgP)
        c.drawRoundRect(0f, 0f, W.toFloat(), 8f, 0f, 0f, accentP)
        c.drawText("🔐  Vault · Secure Notes", PAD, PAD + 34f, brandP)
        var y = PAD + 80f
        titleLines.forEach { l -> c.drawText(l, PAD, y, titleP); y += 64f }
        if (titleLines.isNotEmpty()) y += 14f
        bodyLines.forEach  { l -> c.drawText(l, PAD, y, bodyP);  y += 50f }
        val ts = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        c.drawText(ts, PAD, H - PAD + 20f, timeP)
        return bmp
    }

    private fun wrap(text: String, p: Paint, max: Int): List<String> {
        val lines = mutableListOf<String>(); var cur = StringBuilder()
        text.split(" ").forEach { w ->
            val t = if (cur.isEmpty()) w else "$cur $w"
            if (p.measureText(t) <= max) cur = StringBuilder(t)
            else { if (cur.isNotEmpty()) lines.add(cur.toString()); cur = StringBuilder(w) }
        }
        if (cur.isNotEmpty()) lines.add(cur.toString())
        return lines.take(14)
    }

    private fun buildShareText(title: String, content: String) = buildString {
        if (title.isNotEmpty()) { appendLine(title); appendLine() }; append(content)
    }
}
