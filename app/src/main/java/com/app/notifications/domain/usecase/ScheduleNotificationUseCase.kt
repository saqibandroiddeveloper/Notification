package com.app.notifications.domain.usecase

import com.app.notifications.domain.model.RepeatType
import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Validates input and schedules a local notification via the repository.
 */
class ScheduleNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(params: Params): Result<Int> {
        val title = params.title.trim()
        val description = params.description.trim()
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        if (params.triggerAtMillis <= System.currentTimeMillis()) {
            return Result.failure(IllegalArgumentException("Scheduled time must be in the future"))
        }
        if (params.repeatType == RepeatType.CUSTOM &&
            params.customIntervalMillis < ScheduledNotification.MIN_CUSTOM_INTERVAL_MILLIS
        ) {
            return Result.failure(
                IllegalArgumentException("Custom interval must be at least 15 minutes")
            )
        }

        val notification = ScheduledNotification(
            id = params.id ?: 0,
            title = title,
            description = description,
            triggerAtMillis = params.triggerAtMillis,
            repeatType = params.repeatType,
            customIntervalMillis = params.customIntervalMillis
        )
        return repository.schedule(notification)
    }

    data class Params(
        val title: String,
        val description: String,
        val triggerAtMillis: Long,
        val repeatType: RepeatType = RepeatType.NONE,
        val customIntervalMillis: Long = 0L,
        val id: Int? = null
    )
}
