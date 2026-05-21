package com.app.notifications.fake

import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeNotificationRepository : NotificationRepository {

    private val store = mutableMapOf<Int, ScheduledNotification>()
    private val flow = MutableStateFlow<List<ScheduledNotification>>(emptyList())

    override fun observeScheduledNotifications(): Flow<List<ScheduledNotification>> = flow

    override suspend fun getScheduledNotification(id: Int): ScheduledNotification? = store[id]

    override suspend fun getAllEnabledNotifications(): List<ScheduledNotification> =
        store.values.filter { it.isEnabled }

    override suspend fun schedule(notification: ScheduledNotification): Result<Int> {
        val id = if (notification.id == 0) (store.keys.maxOrNull() ?: 0) + 1 else notification.id
        val saved = notification.copy(id = id, isEnabled = true)
        store[id] = saved
        flow.update { store.values.filter { it.isEnabled }.sortedBy { it.triggerAtMillis } }
        return Result.success(id)
    }

    override suspend fun cancel(id: Int): Result<Unit> {
        store[id]?.let { store[id] = it.copy(isEnabled = false) }
        flow.update { store.values.filter { it.isEnabled }.sortedBy { it.triggerAtMillis } }
        return Result.success(Unit)
    }

    override suspend fun rescheduleAll(): Result<Unit> = Result.success(Unit)

    override suspend fun onNotificationDelivered(id: Int): Result<Unit> = Result.success(Unit)
}
