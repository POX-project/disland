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

// ⚡ Bolt: Cache DateTimeFormatter instead of creating SimpleDateFormat to prevent reallocation overhead and avoid parallel recomposition concurrency bugs.
private val notificationTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatNotificationTime(timeMillis: Long): String {
    return notificationTimeFormatter
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(timeMillis))
}
