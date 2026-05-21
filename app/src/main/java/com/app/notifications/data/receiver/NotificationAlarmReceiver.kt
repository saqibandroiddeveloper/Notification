package com.app.notifications.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.notifications.core.notification.NotificationConstants
import com.app.notifications.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives AlarmManager RTC_WAKEUP broadcasts and triggers notification display.
 * Registered in manifest with exported=false for security.
 */
@AndroidEntryPoint
class NotificationAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: NotificationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationConstants.ACTION_ALARM_FIRED) return
        val notificationId = intent.getIntExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, -1)
        if (notificationId <= 0) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                repository.onNotificationDelivered(notificationId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
