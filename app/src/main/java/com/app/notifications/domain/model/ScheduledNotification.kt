package com.app.notifications.domain.model

/**
 * Domain model for a locally scheduled notification.
 *
 * @property id Stable alarm/notification id (also Room primary key).
 * @property title Notification title shown in the status bar.
 * @property description Notification body text.
 * @property triggerAtMillis Epoch millis when the alarm should fire (RTC_WAKEUP).
 * @property repeatType How the alarm repeats after delivery.
 * @property customIntervalMillis Used when [repeatType] is [RepeatType.CUSTOM].
 * @property createdAtMillis When the schedule was created (audit / UI sorting).
 * @property isEnabled Whether this schedule is active (false = cancelled, kept for history).
 */
data class ScheduledNotification(
    val id: Int,
    val title: String,
    val description: String,
    val triggerAtMillis: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val customIntervalMillis: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true
) {
    fun nextTriggerAfterDelivery(): Long? {
        if (!isEnabled || repeatType == RepeatType.NONE) return null
        return when (repeatType) {
            RepeatType.NONE -> null
            RepeatType.DAILY -> triggerAtMillis + DAY_MILLIS
            RepeatType.WEEKLY -> triggerAtMillis + WEEK_MILLIS
            RepeatType.CUSTOM -> {
                val interval = customIntervalMillis.coerceAtLeast(MIN_CUSTOM_INTERVAL_MILLIS)
                triggerAtMillis + interval
            }
        }
    }

    companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        const val WEEK_MILLIS = 7 * DAY_MILLIS
        const val MIN_CUSTOM_INTERVAL_MILLIS = 15 * 60 * 1000L
    }
}
