package com.ryen.bondhub.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }

    return when {
        // Today
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        }
        // Yesterday
        now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1 &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            "Yesterday"
        }
        // This week
        now.get(Calendar.WEEK_OF_YEAR) == then.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        }
        // This year
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
        // Older
        else -> {
            SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date)
        }
    }
}