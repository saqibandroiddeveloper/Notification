package com.app.notifications.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatter {
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    fun formatEpochMillis(epochMillis: Long): String =
        displayFormat.format(Date(epochMillis))
}
