package com.app.notifications.presentation.notification.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.notifications.core.utils.DateTimeFormatter
import com.app.notifications.presentation.notification.viewmodel.NotificationDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: Int,
    onBack: () -> Unit,
    viewModel: NotificationDetailViewModel = hiltViewModel()
) {
    val notification by viewModel.notification.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification #$notificationId") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                val item = notification
                if (item == null) {
                    Text("Notification not found (id: $notificationId)")
                } else {
                    Text(item.title, style = MaterialTheme.typography.headlineSmall)
                    Text(item.description, style = MaterialTheme.typography.bodyLarge)
                    Text("Scheduled: ${DateTimeFormatter.formatEpochMillis(item.triggerAtMillis)}")
                    Text("Repeat: ${item.repeatType}")
                    if (item.repeatType == com.app.notifications.domain.model.RepeatType.CUSTOM) {
                        Text("Interval: ${item.customIntervalMillis / 60_000} min")
                    }
                }
            }
        }
    }
}
