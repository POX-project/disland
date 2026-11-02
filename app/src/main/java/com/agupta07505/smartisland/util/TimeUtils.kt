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

// Bolt optimization: Cache thread-safe DateTimeFormatter parsing logic at the file-level
// to avoid reallocation overhead during recomposition, while allowing dynamic Zone/Locale configuration.
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatNotificationTime(timeMillis: Long): String {
    // Bolt optimization: Apply timezone and locale dynamically at the usage site
    // so changes in system settings correctly update the UI.
    return timeFormatter
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(Instant.ofEpochMilli(timeMillis))
}
