package com.app.notifications.data.repository

import com.app.notifications.core.notification.NotificationHelper
import com.app.notifications.data.local.dao.ScheduledNotificationDao
import com.app.notifications.data.local.entity.toDomain
import com.app.notifications.data.local.entity.toEntity
import com.app.notifications.data.scheduler.AlarmScheduler
import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: ScheduledNotificationDao,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : NotificationRepository {

    override fun observeScheduledNotifications(): Flow<List<ScheduledNotification>> =
        dao.observeEnabled().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getScheduledNotification(id: Int): ScheduledNotification? =
        dao.getById(id)?.toDomain()

    override suspend fun getAllEnabledNotifications(): List<ScheduledNotification> =
        dao.getAllEnabled().map { it.toDomain() }

    override suspend fun schedule(notification: ScheduledNotification): Result<Int> {
        val assignedId = if (notification.id == 0) {
            dao.getMaxId() + 1
        } else {
            notification.id
        }
        val toSchedule = notification.copy(id = assignedId, isEnabled = true)
        val alarmResult = alarmScheduler.scheduleExact(toSchedule)
        if (alarmResult.isFailure) return Result.failure(
            alarmResult.exceptionOrNull() ?: IllegalStateException("Alarm schedule failed")
        )
        dao.insert(toSchedule.toEntity())
        return Result.success(assignedId)
    }

    override suspend fun cancel(id: Int): Result<Unit> {
        alarmScheduler.cancel(id)
        notificationHelper.cancelNotification(id)
        dao.disable(id)
        return Result.success(Unit)
    }

    override suspend fun rescheduleAll(): Result<Unit> {
        val enabled = dao.getAllEnabled()
        var lastError: Throwable? = null
        enabled.forEach { entity ->
            val domain = entity.toDomain()
            val trigger = domain.triggerAtMillis
            val toSchedule = if (trigger <= System.currentTimeMillis()) {
                domain.nextTriggerAfterDelivery()?.let { next ->
                    domain.copy(triggerAtMillis = next)
                } ?: run {
                    dao.disable(domain.id)
                    return@forEach
                }
            } else {
                domain
            }
            if (toSchedule != domain) {
                dao.update(toSchedule.toEntity())
            }
            val result = alarmScheduler.scheduleExact(toSchedule)
            if (result.isFailure) {
                lastError = result.exceptionOrNull()
            }
        }
        return if (lastError != null) Result.failure(lastError!!) else Result.success(Unit)
    }

    override suspend fun onNotificationDelivered(id: Int): Result<Unit> {
        val current = dao.getById(id)?.toDomain()
            ?: return Result.failure(NoSuchElementException("Notification $id not found"))

        notificationHelper.showNotification(current)

        val nextTrigger = current.nextTriggerAfterDelivery()
        if (nextTrigger == null) {
            dao.disable(id)
            alarmScheduler.cancel(id)
            return Result.success(Unit)
        }

        val updated = current.copy(triggerAtMillis = nextTrigger)
        dao.update(updated.toEntity())
        return alarmScheduler.scheduleExact(updated)
    }
}
