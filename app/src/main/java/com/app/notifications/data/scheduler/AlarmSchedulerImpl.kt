package com.app.notifications.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.notifications.core.notification.NotificationConstants
import com.app.notifications.data.receiver.NotificationAlarmReceiver
import com.app.notifications.domain.model.ScheduledNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    override fun scheduleExact(notification: ScheduledNotification): Result<Unit> {
        if (!canScheduleExactAlarms()) {
            return Result.failure(SecurityException("Exact alarm permission not granted"))
        }
        val triggerAt = notification.triggerAtMillis
        if (triggerAt <= System.currentTimeMillis()) {
            return Result.failure(IllegalArgumentException("Trigger time must be in the future"))
        }

        val pendingIntent = buildAlarmPendingIntent(notification.id)
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (e: SecurityException) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override fun cancel(notificationId: Int) {
        alarmManager.cancel(buildAlarmPendingIntent(notificationId))
    }

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun buildAlarmPendingIntent(notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            action = NotificationConstants.ACTION_ALARM_FIRED
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            flags
        )
    }
}
