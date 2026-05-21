package com.app.notifications.presentation.notification.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.notifications.domain.model.ScheduledNotification
import com.app.notifications.domain.usecase.GetNotificationByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotificationByIdUseCase: GetNotificationByIdUseCase
) : ViewModel() {

    private val notificationId: Int =
        savedStateHandle.get<Int>("notificationId") ?: -1

    private val _notification = MutableStateFlow<ScheduledNotification?>(null)
    val notification: StateFlow<ScheduledNotification?> = _notification.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        if (notificationId > 0) {
            viewModelScope.launch {
                _notification.value = getNotificationByIdUseCase(notificationId)
                _isLoading.value = false
            }
        } else {
            _isLoading.value = false
        }
    }
}
