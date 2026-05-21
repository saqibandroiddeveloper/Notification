package com.app.notifications.domain.usecase

import com.app.notifications.domain.repository.NotificationRepository
import javax.inject.Inject

class CancelNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Int): Result<Unit> {
        if (notificationId <= 0) {
            return Result.failure(IllegalArgumentException("Invalid notification id"))
        }
        return repository.cancel(notificationId)
    }
}
