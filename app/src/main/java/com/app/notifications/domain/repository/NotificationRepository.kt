package com.app.notifications.domain.repository

import com.app.notifications.domain.model.ScheduledNotification
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over local persistence and AlarmManager scheduling.
 */
interface NotificationRepository {

    fun observeScheduledNotifications(): Flow<List<ScheduledNotification>>

    suspend fun getScheduledNotification(id: Int): ScheduledNotification?

    suspend fun getAllEnabledNotifications(): List<ScheduledNotification>

    /**
     * Persists and schedules an exact alarm. Returns the assigned id.
     */
    suspend fun schedule(notification: ScheduledNotification): Result<Int>

    /**
     * Cancels alarm and marks notification disabled (or removes per implementation).
     */
    suspend fun cancel(id: Int): Result<Unit>

    /**
     * Re-schedules all enabled notifications from persistence (boot recovery).
     */
    suspend fun rescheduleAll(): Result<Unit>

    /**
     * Called when alarm fires: optionally reschedules next occurrence for repeating types.
     */
    suspend fun onNotificationDelivered(id: Int): Result<Unit>
}
