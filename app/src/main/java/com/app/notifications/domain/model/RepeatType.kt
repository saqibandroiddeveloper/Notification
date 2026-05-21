package com.app.notifications.domain.model

/**
 * Defines how a scheduled notification repeats after the first trigger.
 */
enum class RepeatType {
    /** Fires once; alarm is removed after delivery. */
    NONE,

    /** Reschedules every 24 hours from the previous trigger time. */
    DAILY,

    /** Reschedules every 7 days from the previous trigger time. */
    WEEKLY,

    /**
     * Reschedules after [ScheduledNotification.customIntervalMillis].
     * Minimum interval enforced at scheduling time (15 minutes).
     */
    CUSTOM
}
