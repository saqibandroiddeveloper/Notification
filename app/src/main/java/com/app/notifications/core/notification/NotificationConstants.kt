package com.app.notifications.core.notification

object NotificationConstants {
    const val CHANNEL_ID = "scheduled_notifications_channel"
    const val CHANNEL_NAME = "Scheduled Notifications"
    const val CHANNEL_DESCRIPTION = "Notifications scheduled by the app"

    const val ACTION_ALARM_FIRED = "com.app.notifications.ACTION_ALARM_FIRED"
    const val ACTION_NOTIFICATION_CLICK = "com.app.notifications.ACTION_NOTIFICATION_CLICK"

    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESCRIPTION = "extra_description"
    const val EXTRA_DEEP_LINK_ROUTE = "extra_deep_link_route"

    const val DEEP_LINK_DETAIL_ROUTE = "notification_detail"
}
