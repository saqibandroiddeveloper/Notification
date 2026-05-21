package com.app.notifications.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.notifications.domain.usecase.RescheduleAllNotificationsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Restores all persisted alarms after device reboot.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        val pendingResult = goAsync()
        scope.launch {
            try {
                rescheduleAllNotificationsUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
