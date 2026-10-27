package com.example.plugnpark.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDurationHours(startMillis: Long, endMillis: Long): String {
        val hours = ((endMillis - startMillis) / (1000 * 3600)).toInt()
        return if (hours <= 1) "1 hr" else "$hours hrs"
    }
}
