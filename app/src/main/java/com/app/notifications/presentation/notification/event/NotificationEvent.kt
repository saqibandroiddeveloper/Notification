package com.app.notifications.presentation.notification.event

/**
 * One-shot UI effects (snackbars, navigation, permission launches).
 */
sealed interface NotificationEvent {
    data class ShowMessage(val message: String) : NotificationEvent
    data class NavigateToDetail(val notificationId: Int) : NotificationEvent
    data object RequestNotificationPermission : NotificationEvent
    data object RequestExactAlarmPermission : NotificationEvent
}
