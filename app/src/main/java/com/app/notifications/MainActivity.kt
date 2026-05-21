package com.app.notifications

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.notifications.core.navigation.NotificationNavGraph
import com.app.notifications.core.notification.NotificationConstants
import com.app.notifications.ui.theme.NotificationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deepLinkId = extractDeepLinkNotificationId(intent)
        setContent {
            NotificationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationNavGraph(deepLinkNotificationId = deepLinkId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLinkId = extractDeepLinkNotificationId(intent)
        if (deepLinkId != null) {
            setContent {
                NotificationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NotificationNavGraph(deepLinkNotificationId = deepLinkId)
                    }
                }
            }
        }
    }

    private fun extractDeepLinkNotificationId(intent: Intent?): Int? {
        if (intent?.action != NotificationConstants.ACTION_NOTIFICATION_CLICK) return null
        val id = intent.getIntExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, -1)
        return id.takeIf { it > 0 }
    }
}
