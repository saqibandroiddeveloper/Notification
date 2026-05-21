package com.app.notifications.domain.usecase

import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationByIdUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(id: Int): ScheduledNotification? =
        repository.getScheduledNotification(id)
}
