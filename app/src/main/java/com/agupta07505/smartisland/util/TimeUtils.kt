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

// ⚡ Bolt: Cache thread-safe DateTimeFormatter to avoid reallocation overhead in compose loops
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

fun formatNotificationTime(timeMillis: Long): String {
    return timeFormatter.format(Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()))
}
