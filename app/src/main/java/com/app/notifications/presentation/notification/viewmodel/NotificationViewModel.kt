package com.app.notifications.presentation.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.notifications.core.notification.NotificationChannelManager
import com.app.notifications.core.permission.ExactAlarmPermissionHelper
import com.app.notifications.core.permission.NotificationPermissionHelper
import com.app.notifications.domain.model.RepeatType
import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.usecase.CancelNotificationUseCase
import com.app.notifications.domain.usecase.GetScheduledNotificationsUseCase
import com.app.notifications.domain.usecase.ScheduleNotificationUseCase
import com.app.notifications.presentation.notification.event.NotificationEvent
import com.app.notifications.presentation.notification.intent.NotificationIntent
import com.app.notifications.presentation.notification.state.NotificationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
    private val cancelNotificationUseCase: CancelNotificationUseCase,
    private val getScheduledNotificationsUseCase: GetScheduledNotificationsUseCase,
    private val notificationPermissionHelper: NotificationPermissionHelper,
    private val exactAlarmPermissionHelper: ExactAlarmPermissionHelper,
    private val channelManager: NotificationChannelManager
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    private val _events = Channel<NotificationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        channelManager.ensureChannelCreated()
        initDefaultDateTime()
        refreshPermissions()
        observeScheduled()
    }

    fun onIntent(intent: NotificationIntent) {
        when (intent) {
            is NotificationIntent.UpdateTitle ->
                _state.update { it.copy(title = intent.value, errorMessage = null) }
            is NotificationIntent.UpdateDescription ->
                _state.update { it.copy(description = intent.value) }
            is NotificationIntent.UpdateScheduledDate ->
                _state.update {
                    it.copy(
                        scheduledYear = intent.year,
                        scheduledMonth = intent.month,
                        scheduledDay = intent.day
                    )
                }
            is NotificationIntent.UpdateScheduledTime ->
                _state.update {
                    it.copy(scheduledHour = intent.hour, scheduledMinute = intent.minute)
                }
            is NotificationIntent.UpdateRepeatType ->
                _state.update { it.copy(repeatType = intent.repeatType) }
            is NotificationIntent.UpdateCustomIntervalMinutes ->
                _state.update { it.copy(customIntervalMinutes = intent.minutes) }
            NotificationIntent.ScheduleNotification -> schedule()
            is NotificationIntent.CancelNotification -> cancel(intent.id)
            NotificationIntent.RefreshPermissions -> refreshPermissions()
            NotificationIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
            NotificationIntent.ShowDatePicker -> _state.update { it.copy(showDatePicker = true) }
            NotificationIntent.HideDatePicker -> _state.update { it.copy(showDatePicker = false) }
            NotificationIntent.ShowTimePicker -> _state.update { it.copy(showTimePicker = true) }
            NotificationIntent.HideTimePicker -> _state.update { it.copy(showTimePicker = false) }
        }
    }

    private fun initDefaultDateTime() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _state.update {
            it.copy(
                scheduledYear = cal.get(Calendar.YEAR),
                scheduledMonth = cal.get(Calendar.MONTH),
                scheduledDay = cal.get(Calendar.DAY_OF_MONTH),
                scheduledHour = cal.get(Calendar.HOUR_OF_DAY),
                scheduledMinute = cal.get(Calendar.MINUTE)
            )
        }
    }

    private fun observeScheduled() {
        viewModelScope.launch {
            getScheduledNotificationsUseCase().collect { list ->
                _state.update { it.copy(scheduledNotifications = list) }
            }
        }
    }

    private fun refreshPermissions() {
        _state.update {
            it.copy(
                hasNotificationPermission = notificationPermissionHelper.hasPermission(),
                canScheduleExactAlarms = exactAlarmPermissionHelper.canScheduleExactAlarms()
            )
        }
    }

    private fun schedule() {
        val current = _state.value
        if (!current.hasNotificationPermission) {
            viewModelScope.launch {
                _events.send(NotificationEvent.RequestNotificationPermission)
            }
            return
        }
        if (!current.canScheduleExactAlarms) {
            viewModelScope.launch {
                _events.send(NotificationEvent.RequestExactAlarmPermission)
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val customMillis = if (current.repeatType == RepeatType.CUSTOM) {
                current.customIntervalMinutes * 60 * 1000
            } else {
                0L
            }
            val result = scheduleNotificationUseCase(
                ScheduleNotificationUseCase.Params(
                    title = current.title,
                    description = current.description,
                    triggerAtMillis = current.triggerAtMillis(),
                    repeatType = current.repeatType,
                    customIntervalMillis = customMillis
                )
            )
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { id ->
                    _events.send(NotificationEvent.ShowMessage("Scheduled (id: $id)"))
                    _state.update {
                        it.copy(title = "", description = "", errorMessage = null)
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(errorMessage = e.message ?: "Schedule failed") }
                }
            )
        }
    }

    private fun cancel(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = cancelNotificationUseCase(id)
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = {
                    _events.send(NotificationEvent.ShowMessage("Cancelled notification $id"))
                },
                onFailure = { e ->
                    _state.update { it.copy(errorMessage = e.message) }
                }
            )
        }
    }
}
