package com.app.notifications.data.scheduler

import com.app.notifications.domain.model.ScheduledNotification

/**
 * Abstraction over [android.app.AlarmManager] for testability.
 */
interface AlarmScheduler {
    fun scheduleExact(notification: ScheduledNotification): Result<Unit>
    fun cancel(notificationId: Int)
    fun canScheduleExactAlarms(): Boolean
}
