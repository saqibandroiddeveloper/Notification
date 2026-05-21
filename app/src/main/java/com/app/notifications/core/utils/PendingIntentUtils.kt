package com.app.notifications.core.utils

import android.app.PendingIntent

/**
 * Central place for immutable PendingIntent flags (API 23+ requirement).
 */
object PendingIntentUtils {
    fun updateCurrentImmutable(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun cancelCurrentImmutable(): Int =
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
}
