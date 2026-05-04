package com.vaultapp.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.vaultapp.data.local.PreferencesManager
import com.vaultapp.data.model.LockTimeout
import com.vaultapp.ui.components.ToastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockManager @Inject constructor(
    private val prefs: PreferencesManager
) : Application.ActivityLifecycleCallbacks {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var backgroundedAt: Long = 0L
    var isLocked = true
        private set

    fun unlock() { isLocked = false }
    fun lock() { isLocked = true }

    override fun onActivityPaused(activity: Activity) {
        backgroundedAt = System.currentTimeMillis()
        scope.launch { prefs.updateLastActiveAt() }
    }

    override fun onActivityResumed(activity: Activity) {
        if (backgroundedAt == 0L) return
        scope.launch {
            val timeout = prefs.lockTimeout.first()
            val elapsed = System.currentTimeMillis() - backgroundedAt
            if (timeout == LockTimeout.IMMEDIATELY || elapsed >= timeout.millis) {
                for (i in 5 downTo 1) {
                    ToastManager.info("$i")
                    delay(1000)
                }
                ToastManager.info("Locked")
                lock()
                // Navigate to lock screen — handled by observing isLocked in MainActivity
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
