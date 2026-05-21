package com.app.notifications.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.app.notifications.MainActivity
import com.app.notifications.R
import com.app.notifications.core.utils.PendingIntentUtils
import com.app.notifications.domain.model.ScheduledNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and posts system notifications with deep-link payload to [MainActivity].
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channelManager: NotificationChannelManager,
    private val foregroundTracker: AppForegroundTracker
) {
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
            ?: throw IllegalStateException("NotificationManager unavailable")
    }

    /**
     * Shows a high-priority notification. When the app is in foreground, posts a
     * low-visibility heads-up style notification (or skip if you prefer in-app only).
     * Production apps often route to in-app event bus when foreground — here we still
     * post but with DEFAULT priority to reduce interruption.
     */
    fun showNotification(notification: ScheduledNotification) {
        channelManager.ensureChannelCreated()
        if (!channelManager.areNotificationsEnabled()) return

        val contentIntent = buildContentPendingIntent(notification)
        val priority = if (foregroundTracker.isInForeground) {
            NotificationCompat.PRIORITY_DEFAULT
        } else {
            NotificationCompat.PRIORITY_HIGH
        }

        val built = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.description)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(notification.description)
            )
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notificationManager.notify(notification.id, built)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun buildContentPendingIntent(notification: ScheduledNotification): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = NotificationConstants.ACTION_NOTIFICATION_CLICK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, notification.id)
            putExtra(NotificationConstants.EXTRA_TITLE, notification.title)
            putExtra(NotificationConstants.EXTRA_DESCRIPTION, notification.description)
            putExtra(
                NotificationConstants.EXTRA_DEEP_LINK_ROUTE,
                "${NotificationConstants.DEEP_LINK_DETAIL_ROUTE}/${notification.id}"
            )
        }
        return PendingIntent.getActivity(
            context,
            notification.id,
            intent,
            PendingIntentUtils.updateCurrentImmutable()
        )
    }
}
