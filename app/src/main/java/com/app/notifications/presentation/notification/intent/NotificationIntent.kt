package com.app.notifications.presentation.notification.intent

import com.app.notifications.domain.model.RepeatType

/**
 * User actions from the Compose UI (MVI Intent).
 */
sealed interface NotificationIntent {
    data class UpdateTitle(val value: String) : NotificationIntent
    data class UpdateDescription(val value: String) : NotificationIntent
    data class UpdateScheduledDate(val year: Int, val month: Int, val day: Int) : NotificationIntent
    data class UpdateScheduledTime(val hour: Int, val minute: Int) : NotificationIntent
    data class UpdateRepeatType(val repeatType: RepeatType) : NotificationIntent
    data class UpdateCustomIntervalMinutes(val minutes: Long) : NotificationIntent
    data object ScheduleNotification : NotificationIntent
    data class CancelNotification(val id: Int) : NotificationIntent
    data object RefreshPermissions : NotificationIntent
    data object DismissError : NotificationIntent
    data object ShowDatePicker : NotificationIntent
    data object HideDatePicker : NotificationIntent
    data object ShowTimePicker : NotificationIntent
    data object HideTimePicker : NotificationIntent
}
