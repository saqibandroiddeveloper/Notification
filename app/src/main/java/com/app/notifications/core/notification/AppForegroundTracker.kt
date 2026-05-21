package com.app.notifications.core.notification

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the app process is in the foreground to avoid duplicate UX
 * when a notification fires while the user is already inside the app.
 */
@Singleton
class AppForegroundTracker @Inject constructor() : DefaultLifecycleObserver {

    @Volatile
    var isInForeground: Boolean = false
        private set

    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        isInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isInForeground = false
    }
}
