package com.vaultapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.vaultapp.data.local.PreferencesManager
import com.vaultapp.data.remote.GitHubService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesManager,
    private val service: GitHubService
) {
    suspend fun checkForUpdates(force: Boolean = false) {
        if (!force && !prefs.autoUpdateEnabled.first()) return

        try {
            // Check latest release from GitHub
            val release = service.getLatestRelease("harshal20m", "NotesVault")
            val latestVersion = release.tagName.removePrefix("v")
            
            // Get current version from package manager to be safe
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = pInfo.versionName ?: "0.0.0"

            if (isNewerVersion(currentVersion, latestVersion)) {
                showUpdateNotification(latestVersion, release.htmlUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val lateParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(currParts.size, lateParts.size)) {
            val curr = currParts.getOrElse(i) { 0 }
            val late = lateParts.getOrElse(i) { 0 }
            if (late > curr) return true
            if (curr > late) return false
        }
        return false
    }

    private fun showUpdateNotification(version: String, url: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "vault_updates"
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, "App Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notifications for new app versions"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val pendingIntent = PendingIntent.getActivity(
            context, 888, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Update Available: v$version")
            .setContentText("A new version of Vault is available on GitHub.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Version $version is now available. Tap to view release notes and download the APK."))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(888, notification)
    }
}
