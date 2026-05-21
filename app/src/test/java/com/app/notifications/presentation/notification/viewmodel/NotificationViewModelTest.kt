package com.app.notifications.presentation.notification.viewmodel

import com.app.notifications.core.notification.NotificationChannelManager
import com.app.notifications.core.permission.ExactAlarmPermissionHelper
import com.app.notifications.core.permission.NotificationPermissionHelper
import com.app.notifications.domain.usecase.CancelNotificationUseCase
import com.app.notifications.domain.usecase.GetScheduledNotificationsUseCase
import com.app.notifications.domain.usecase.ScheduleNotificationUseCase
import com.app.notifications.fake.FakeNotificationRepository
import com.app.notifications.presentation.notification.intent.NotificationIntent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeNotificationRepository()

    private val notificationPermissionHelper = mockk<NotificationPermissionHelper> {
        every { hasPermission() } returns true
    }
    private val exactAlarmPermissionHelper = mockk<ExactAlarmPermissionHelper> {
        every { canScheduleExactAlarms() } returns true
    }
    private val channelManager = mockk<NotificationChannelManager>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `update title updates state`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(NotificationIntent.UpdateTitle("Hello"))
        advanceUntilIdle()
        assertEquals("Hello", viewModel.state.value.title)
    }

    private fun createViewModel(): NotificationViewModel {
        return NotificationViewModel(
            scheduleNotificationUseCase = ScheduleNotificationUseCase(repository),
            cancelNotificationUseCase = CancelNotificationUseCase(repository),
            getScheduledNotificationsUseCase = GetScheduledNotificationsUseCase(repository),
            notificationPermissionHelper = notificationPermissionHelper,
            exactAlarmPermissionHelper = exactAlarmPermissionHelper,
            channelManager = channelManager
        )
    }
}
