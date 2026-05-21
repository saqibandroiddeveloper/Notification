package com.app.notifications.di

import android.content.Context
import androidx.room.Room
import com.app.notifications.data.local.NotificationDatabase
import com.app.notifications.data.local.dao.ScheduledNotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotificationDatabase =
        Room.databaseBuilder(
            context,
            NotificationDatabase::class.java,
            "notification_scheduler.db"
        ).build()

    @Provides
    fun provideScheduledNotificationDao(database: NotificationDatabase): ScheduledNotificationDao =
        database.scheduledNotificationDao()
}
