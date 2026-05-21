package com.app.notifications.presentation.notification.state

import com.app.notifications.domain.model.RepeatType
import com.app.notifications.domain.model.ScheduledNotification

/**
 * Immutable UI state for the schedule screen.
 */
data class NotificationState(
    val title: String = "",
    val description: String = "",
    val scheduledYear: Int = 0,
    val scheduledMonth: Int = 0,
    val scheduledDay: Int = 0,
    val scheduledHour: Int = 12,
    val scheduledMinute: Int = 0,
    val repeatType: RepeatType = RepeatType.NONE,
    val customIntervalMinutes: Long = 60,
    val scheduledNotifications: List<ScheduledNotification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasNotificationPermission: Boolean = true,
    val canScheduleExactAlarms: Boolean = true,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
) {
    val isFormValid: Boolean
        get() = title.isNotBlank() &&
            scheduledYear > 0 &&
            scheduledMonth >= 0 &&
            scheduledDay > 0

    fun triggerAtMillis(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(scheduledYear, scheduledMonth, scheduledDay, scheduledHour, scheduledMinute, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
