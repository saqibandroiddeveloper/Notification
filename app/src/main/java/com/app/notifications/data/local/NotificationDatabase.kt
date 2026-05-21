package com.app.notifications.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.notifications.data.local.dao.ScheduledNotificationDao
import com.app.notifications.data.local.entity.ScheduledNotificationEntity

@Database(
    entities = [ScheduledNotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun scheduledNotificationDao(): ScheduledNotificationDao
}
