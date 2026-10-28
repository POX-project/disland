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

private val NOTIFICATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

fun formatNotificationTime(timeMillis: Long): String {
    return NOTIFICATION_TIME_FORMATTER.format(Instant.ofEpochMilli(timeMillis))
}
