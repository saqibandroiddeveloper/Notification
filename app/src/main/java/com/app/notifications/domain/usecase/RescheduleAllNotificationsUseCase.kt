package com.app.notifications.domain.usecase

import com.app.notifications.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Restores all enabled alarms from local storage — invoked on BOOT_COMPLETED.
 */
class RescheduleAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.rescheduleAll()
}
