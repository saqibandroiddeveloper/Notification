package com.app.notifications

import android.app.Application
import com.app.notifications.core.notification.AppForegroundTracker
import com.app.notifications.core.notification.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotificationApplication : Application() {

    @Inject
    lateinit var channelManager: NotificationChannelManager

    @Inject
    lateinit var foregroundTracker: AppForegroundTracker

    override fun onCreate() {
        super.onCreate()
        channelManager.ensureChannelCreated()
        foregroundTracker.register()
    }
}
