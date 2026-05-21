package com.app.notifications.presentation.notification.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.notifications.core.utils.DateTimeFormatter
import com.app.notifications.domain.model.RepeatType
import com.app.notifications.presentation.notification.event.NotificationEvent
import com.app.notifications.presentation.notification.intent.NotificationIntent
import com.app.notifications.presentation.notification.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScheduleScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onIntent(NotificationIntent.RefreshPermissions)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
                is NotificationEvent.NavigateToDetail ->
                    onNavigateToDetail(event.notificationId)
                NotificationEvent.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
                NotificationEvent.RequestExactAlarmPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Schedule Notifications") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.hasNotificationPermission) {
                PermissionBanner(
                    text = "Notification permission required (Android 13+)",
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                android.Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                )
            }
            if (!state.canScheduleExactAlarms) {
                PermissionBanner(
                    text = "Exact alarm permission required for precise scheduling",
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                            )
                        }
                    }
                )
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(NotificationIntent.UpdateTitle(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onIntent(NotificationIntent.UpdateDescription(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.onIntent(NotificationIntent.ShowDatePicker) }) {
                    Text(
                        if (state.scheduledYear > 0) {
                            "${state.scheduledDay}/${state.scheduledMonth + 1}/${state.scheduledYear}"
                        } else {
                            "Pick date"
                        }
                    )
                }
                OutlinedButton(onClick = { viewModel.onIntent(NotificationIntent.ShowTimePicker) }) {
                    Text(
                        String.format("%02d:%02d", state.scheduledHour, state.scheduledMinute)
                    )
                }
            }

            RepeatTypeSelector(
                selected = state.repeatType,
                customMinutes = state.customIntervalMinutes,
                onRepeatSelected = { viewModel.onIntent(NotificationIntent.UpdateRepeatType(it)) },
                onCustomMinutes = { viewModel.onIntent(NotificationIntent.UpdateCustomIntervalMinutes(it)) }
            )

            state.errorMessage?.let { msg ->
                Text(text = msg, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.onIntent(NotificationIntent.ScheduleNotification) },
                enabled = state.isFormValid && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                } else {
                    Text("Schedule")
                }
            }

            Text("Scheduled", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.scheduledNotifications, key = { it.id }) { item ->
                    ScheduledItemCard(
                        title = item.title,
                        subtitle = DateTimeFormatter.formatEpochMillis(item.triggerAtMillis),
                        repeat = item.repeatType.name,
                        onOpen = { onNavigateToDetail(item.id) },
                        onCancel = {
                            viewModel.onIntent(NotificationIntent.CancelNotification(item.id))
                        }
                    )
                }
            }
        }
    }

    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.onIntent(NotificationIntent.HideDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            viewModel.onIntent(
                                NotificationIntent.UpdateScheduledDate(
                                    year = cal.get(Calendar.YEAR),
                                    month = cal.get(Calendar.MONTH),
                                    day = cal.get(Calendar.DAY_OF_MONTH)
                                )
                            )
                        }
                        viewModel.onIntent(NotificationIntent.HideDatePicker)
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(NotificationIntent.HideDatePicker) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.scheduledHour,
            initialMinute = state.scheduledMinute,
            is24Hour = true
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onIntent(NotificationIntent.HideTimePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onIntent(
                            NotificationIntent.UpdateScheduledTime(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            )
                        )
                        viewModel.onIntent(NotificationIntent.HideTimePicker)
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(NotificationIntent.HideTimePicker) }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun PermissionBanner(text: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, modifier = Modifier.weight(1f))
            TextButton(onClick = onAction) { Text("Grant") }
        }
    }
}

@Composable
private fun RepeatTypeSelector(
    selected: RepeatType,
    customMinutes: Long,
    onRepeatSelected: (RepeatType) -> Unit,
    onCustomMinutes: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Repeat", style = MaterialTheme.typography.labelLarge)
        RepeatType.entries.forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onRepeatSelected(type) }) {
                    Text(
                        text = type.name,
                        color = if (selected == type) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
        if (selected == RepeatType.CUSTOM) {
            OutlinedTextField(
                value = customMinutes.toString(),
                onValueChange = { v -> v.toLongOrNull()?.let(onCustomMinutes) },
                label = { Text("Interval (minutes, min 15)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScheduledItemCard(
    title: String,
    subtitle: String,
    repeat: String,
    onOpen: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                Text("Repeat: $repeat", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Delete, contentDescription = "Cancel")
            }
        }
    }
}
