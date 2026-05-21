package com.app.notifications.domain.usecase

import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.fake.FakeNotificationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancelNotificationUseCaseTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var cancelUseCase: CancelNotificationUseCase
    private lateinit var scheduleUseCase: ScheduleNotificationUseCase

    @Before
    fun setup() {
        repository = FakeNotificationRepository()
        cancelUseCase = CancelNotificationUseCase(repository)
        scheduleUseCase = ScheduleNotificationUseCase(repository)
    }

    @Test
    fun `cancel removes scheduled notification`() = runTest {
        val id = scheduleUseCase(
            ScheduleNotificationUseCase.Params(
                title = "A",
                description = "B",
                triggerAtMillis = System.currentTimeMillis() + 120_000
            )
        ).getOrThrow()

        val cancelResult = cancelUseCase(id)
        assertTrue(cancelResult.isSuccess)
        val stored = repository.getScheduledNotification(id)
        assertFalse(stored?.isEnabled == true)
    }

    @Test
    fun `cancel fails for invalid id`() = runTest {
        val result = cancelUseCase(0)
        assertTrue(result.isFailure)
    }
}
