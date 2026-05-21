package com.app.notifications.domain.usecase

import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScheduledNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<ScheduledNotification>> =
        repository.observeScheduledNotifications()
}
