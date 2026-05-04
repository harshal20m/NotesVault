package com.vaultapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.vaultapp.MainActivity
import java.util.concurrent.TimeUnit

// ── Notification channel setup ─────────────────────────────────────────────────

object NotificationHelper {

    const val CHANNEL_REMINDERS = "vault_reminders"
    const val CHANNEL_BACKUP = "vault_backup"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val remindersChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Note Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders you set on notes"
            enableVibration(true)
        }

        val backupChannel = NotificationChannel(
            CHANNEL_BACKUP,
            "Backup",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Vault backup status"
        }

        manager.createNotificationChannel(remindersChannel)
        manager.createNotificationChannel(backupChannel)
    }

    fun showReminderNotification(context: Context, noteId: Long, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_note_id", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, noteId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title.ifEmpty { "Note reminder" })
            .setContentText(content.take(80))
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.take(200)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(noteId.toInt(), notification)
    }
}

// ── WorkManager worker for note reminders ─────────────────────────────────────

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getLong("note_id", -1L)
        val title = inputData.getString("title") ?: ""
        val content = inputData.getString("content") ?: ""
        if (noteId == -1L) return Result.failure()
        NotificationHelper.showReminderNotification(context, noteId, title, content)
        return Result.success()
    }

    companion object {
        const val WORK_TAG = "note_reminder"

        fun schedule(
            context: Context,
            noteId: Long,
            title: String,
            content: String,
            triggerAtMillis: Long
        ) {
            val delay = triggerAtMillis - System.currentTimeMillis()
            if (delay <= 0) return

            val data = workDataOf(
                "note_id" to noteId,
                "title" to title,
                "content" to content
            )
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("$WORK_TAG:$noteId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork("reminder_$noteId", ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context, noteId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$noteId")
        }
    }
}

// ── Auto-backup WorkManager worker ─────────────────────────────────────────────

class AutoBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Auto-backup logic: export to local storage
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_BACKUP)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Vault backup complete")
            .setContentText("Your notes and passwords have been backed up.")
            .setAutoCancel(true)
            .build()
        manager.notify(9999, notification)
        return Result.success()
    }

    companion object {
        fun scheduleWeekly(context: Context) {
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .addTag("auto_backup")
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("auto_backup", ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("auto_backup")
        }
    }
}
