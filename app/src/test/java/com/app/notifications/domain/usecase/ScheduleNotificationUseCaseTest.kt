package com.app.notifications.domain.usecase

import com.app.notifications.domain.model.RepeatType
import com.app.notifications.fake.FakeNotificationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScheduleNotificationUseCaseTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var useCase: ScheduleNotificationUseCase

    @Before
    fun setup() {
        repository = FakeNotificationRepository()
        useCase = ScheduleNotificationUseCase(repository)
    }

    @Test
    fun `schedule succeeds with valid input`() = runTest {
        val trigger = System.currentTimeMillis() + 60_000
        val result = useCase(
            ScheduleNotificationUseCase.Params(
                title = "Test",
                description = "Body",
                triggerAtMillis = trigger,
                repeatType = RepeatType.DAILY
            )
        )
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
    }

    @Test
    fun `schedule fails when title blank`() = runTest {
        val result = useCase(
            ScheduleNotificationUseCase.Params(
                title = "  ",
                description = "Body",
                triggerAtMillis = System.currentTimeMillis() + 60_000
            )
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `schedule fails when time in past`() = runTest {
        val result = useCase(
            ScheduleNotificationUseCase.Params(
                title = "Test",
                description = "Body",
                triggerAtMillis = System.currentTimeMillis() - 1000
            )
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `custom repeat requires minimum interval`() = runTest {
        val result = useCase(
            ScheduleNotificationUseCase.Params(
                title = "Test",
                description = "Body",
                triggerAtMillis = System.currentTimeMillis() + 60_000,
                repeatType = RepeatType.CUSTOM,
                customIntervalMillis = 60_000
            )
        )
        assertTrue(result.isFailure)
    }
}
