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

// ⚡ Bolt: Cache pattern parsing at file level to avoid overhead of allocating Formatters during recomposition
private val timeFormatterPattern = DateTimeFormatter.ofPattern("HH:mm")

fun formatNotificationTime(timeMillis: Long): String {
    return timeFormatterPattern
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(timeMillis))
}
