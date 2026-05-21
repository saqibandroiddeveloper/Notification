package com.app.notifications.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.notifications.data.local.entity.ScheduledNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledNotificationDao {

    @Query("SELECT * FROM scheduled_notifications WHERE isEnabled = 1 ORDER BY triggerAtMillis ASC")
    fun observeEnabled(): Flow<List<ScheduledNotificationEntity>>

    @Query("SELECT * FROM scheduled_notifications ORDER BY triggerAtMillis ASC")
    fun observeAll(): Flow<List<ScheduledNotificationEntity>>

    @Query("SELECT * FROM scheduled_notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ScheduledNotificationEntity?

    @Query("SELECT * FROM scheduled_notifications WHERE isEnabled = 1")
    suspend fun getAllEnabled(): List<ScheduledNotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduledNotificationEntity)

    @Update
    suspend fun update(entity: ScheduledNotificationEntity)

    @Query("UPDATE scheduled_notifications SET isEnabled = 0 WHERE id = :id")
    suspend fun disable(id: Int)

    @Query("DELETE FROM scheduled_notifications WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COALESCE(MAX(id), 0) FROM scheduled_notifications")
    suspend fun getMaxId(): Int
}
