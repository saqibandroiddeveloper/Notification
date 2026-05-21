package com.app.notifications.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.notifications.domain.model.RepeatType
import com.app.notifications.domain.model.ScheduledNotification

@Entity(tableName = "scheduled_notifications")
data class ScheduledNotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val triggerAtMillis: Long,
    val repeatType: String,
    val customIntervalMillis: Long,
    val createdAtMillis: Long,
    val isEnabled: Boolean
)

fun ScheduledNotificationEntity.toDomain(): ScheduledNotification = ScheduledNotification(
    id = id,
    title = title,
    description = description,
    triggerAtMillis = triggerAtMillis,
    repeatType = RepeatType.valueOf(repeatType),
    customIntervalMillis = customIntervalMillis,
    createdAtMillis = createdAtMillis,
    isEnabled = isEnabled
)

fun ScheduledNotification.toEntity(): ScheduledNotificationEntity = ScheduledNotificationEntity(
    id = id,
    title = title,
    description = description,
    triggerAtMillis = triggerAtMillis,
    repeatType = repeatType.name,
    customIntervalMillis = customIntervalMillis,
    createdAtMillis = createdAtMillis,
    isEnabled = isEnabled
)
