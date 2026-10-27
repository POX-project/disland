/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Do not cache the formatter as static property so that it updates when the user changes locale
fun formatNotificationTime(timeMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return formatter.format(Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()))
}
