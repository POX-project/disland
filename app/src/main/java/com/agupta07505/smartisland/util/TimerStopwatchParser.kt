/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import java.util.regex.Pattern

object TimerStopwatchParser {

    private val CLOCK_PACKAGES = setOf(
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.android.deskclock",
        "com.miui.deskclock",
        "com.oneplus.deskclock",
        "com.coloros.alarmclock",
        "com.oplus.alarmclock",
        "com.android.bbkclock",
        "com.vivo.clock",
        "com.huawei.deskclock",
        "com.hihonor.deskclock",
        "com.motorola.blur.alarmclock",
        "com.motorola.timeweatherwidget",
        "com.sec.android.deskclock",
        "com.asus.deskclock",
        "com.lenovo.deskclock",
        "com.sonyericsson.organizer",
        "com.htc.android.worldclock",
        "com.timer.stopwatch",
        "com.hybrid.stopwatch",
        "com.sportstracklive.stopwatch",
        "com.jee.timer",
        "com.clover.timer"
    )

    private val TIMER_KEYWORDS = listOf(
        "timer", "countdown", "time's up", "times up", "time is up",
        "timer paused", "timer running", "timer expired", "timer finished",
        "remaining", "sec remaining", "min remaining"
    )

    private val STOPWATCH_KEYWORDS = listOf(
        "stopwatch", "lap", "laps", "split", "elapsed",
        "stopwatch running", "stopwatch paused"
    )

    private val TIME_PATTERN = Pattern.compile("\\b(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,2}))?\\b")

    fun extractFullText(notification: Notification): String {
        val extras = notification.extras ?: return ""
        val title = runCatching { extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() }.getOrNull().orEmpty()
        val text = runCatching { extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() }.getOrNull().orEmpty()
        val subText = runCatching { extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() }.getOrNull().orEmpty()
        val bigText = runCatching { extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() }.getOrNull().orEmpty()
        val infoText = runCatching { extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString() }.getOrNull().orEmpty()
        val summaryText = runCatching { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() }.getOrNull().orEmpty()
        val ticker = runCatching { notification.tickerText?.toString() }.getOrNull().orEmpty()
        val textLines = runCatching {
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString(" ") { it.toString() }
        }.getOrNull().orEmpty()
        return "$title $text $subText $bigText $infoText $summaryText $ticker $textLines".lowercase()
    }

    fun extractActionLabels(notification: Notification): List<String> {
        val actions = runCatching { notification.actions }.getOrNull() ?: return emptyList()
        return actions.mapNotNull {
            runCatching { it?.title?.toString()?.lowercase() }.getOrNull()
        }
    }

    fun isClockPackage(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        if (NotificationFilter.isMessagingPackage(pkg)) return false
        if (pkg.contains("instagram") || pkg.contains("facebook") || pkg.contains("whatsapp") ||
            pkg.contains("telegram") || pkg.contains("tiktok") || pkg.contains("youtube") ||
            pkg.contains("twitter") || pkg.contains("reddit") || pkg.contains("google.android.gm") ||
            pkg.contains("android.chrome") || pkg.contains("netflix") || pkg.contains("spotify")
        ) return false

        return CLOCK_PACKAGES.contains(pkg) ||
            pkg.endsWith(".deskclock") ||
            pkg.endsWith(".alarmclock") ||
            pkg.endsWith(".clock") ||
            pkg.contains(".deskclock.") ||
            pkg.contains(".alarmclock.") ||
            pkg.contains(".clockpackage") ||
            pkg.contains(".timer.") ||
            pkg.contains(".stopwatch.")
    }

    /**
     * Checks if a notification is a live Timer notification.
     */
    fun isTimer(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification ?: return false
        val packageName = sbn.packageName.lowercase()
        return isTimer(notification, packageName)
    }

    /**
     * Checks if a Notification is a live Timer notification.
     */
    fun isTimer(notification: Notification, packageName: String = ""): Boolean {
        val pkg = packageName.lowercase()

        // 1. Messaging, email, or social apps are NEVER timers
        if (NotificationFilter.isMessagingPackage(pkg) ||
            NotificationFilter.isMessagingNotification(notification, pkg)
        ) {
            return false
        }

        // 2. Exclude if clearly a stopwatch
        if (isStopwatch(notification, pkg)) {
            return false
        }

        val fullText = extractFullText(notification)
        val actionLabels = extractActionLabels(notification)

        // 3. Exclude non-timer system events (Hotspot, Screen Recording, Calls, Navigation)
        if (OemDeviceRules.isNonNavigationContent(pkg, fullText) &&
            !CLOCK_PACKAGES.contains(pkg) &&
            !actionLabels.any { it.contains("+1") || it.contains("timer") || it.contains("add 1") }
        ) {
            if (OemDeviceRules.isScreenRecording(pkg, fullText, OemDeviceType.GENERIC) ||
                OemDeviceRules.isHotspot(pkg, fullText)
            ) {
                return false
            }
        }

        val extras = notification.extras
        val isChronometerCountDown = extras?.let {
            runCatching {
                it.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false) ||
                it.getBoolean("android.chronometerCountDown", false)
            }.getOrDefault(false)
        } ?: false

        // Standard Android countdown chronometer (official framework API for countdown timers)
        if (isChronometerCountDown) return true

        val isClockApp = isClockPackage(pkg)
        val category = runCatching { notification.category }.getOrNull().orEmpty().lowercase()
        val isAlarmCategory = category == "alarm" || category == "category_alarm"

        val hasTimerAction = actionLabels.any {
            it.contains("+1") || it.contains("timer") || it.contains("add 1") || it.contains("reset timer") || it.contains("stop timer")
        }
        val hasTimerKeyword = TIMER_KEYWORDS.any { fullText.contains(it) }
        val hasTimePattern = TIME_PATTERN.matcher(fullText).find()

        // If it's a genuine clock app or ALARM category:
        if (isClockApp || isAlarmCategory) {
            if (hasTimerAction) return true
            if (hasTimerKeyword) return true
            if (hasTimePattern && !actionLabels.any { it.equals("lap", true) || it.startsWith("lap ") }) return true
            val notifWhen = runCatching { notification.`when` }.getOrDefault(0L)
            if (notifWhen > System.currentTimeMillis() && (hasTimerKeyword || hasTimerAction)) return true
        } else {
            // For third-party apps not recognized as clock packages:
            // MUST have explicit timer action AND (timer keyword OR time pattern)
            if (hasTimerAction && (hasTimerKeyword || hasTimePattern)) return true
        }

        return false
    }

    /**
     * Checks if a notification is a live Stopwatch notification.
     */
    fun isStopwatch(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification ?: return false
        val packageName = sbn.packageName.lowercase()
        return isStopwatch(notification, packageName)
    }

    /**
     * Checks if a Notification is a live Stopwatch notification.
     */
    fun isStopwatch(notification: Notification, packageName: String = ""): Boolean {
        val pkg = packageName.lowercase()

        // 1. Messaging, email, or social apps are NEVER stopwatches
        if (NotificationFilter.isMessagingPackage(pkg) ||
            NotificationFilter.isMessagingNotification(notification, pkg)
        ) {
            return false
        }

        val fullText = extractFullText(notification)
        val actionLabels = extractActionLabels(notification)

        val category = runCatching { notification.category }.getOrNull().orEmpty().lowercase()
        val isStopwatchCategory = category == "stopwatch" || category == "category_stopwatch"

        val isClockApp = isClockPackage(pkg)

        // Strict lap action check — avoid matching arbitrary words
        val hasLapAction = actionLabels.any {
            val label = it.trim()
            label.equals("lap", ignoreCase = true) ||
            label.startsWith("lap ", ignoreCase = true) ||
            label.equals("split", ignoreCase = true) ||
            label.startsWith("split ", ignoreCase = true)
        }

        // Strict stopwatch keyword check — avoid matching substring "lap" against "laptop", "collapse", etc.
        val hasStopwatchKeyword = fullText.contains("stopwatch")

        val extras = notification.extras
        val isChronometer = extras?.let {
            runCatching {
                it.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false) ||
                it.getBoolean("android.showChronometer", false) ||
                it.getLong("android.chronometerBase", 0L) > 0L
            }.getOrDefault(false)
        } ?: false

        val isChronometerCountDown = extras?.let {
            runCatching {
                it.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false) ||
                it.getBoolean("android.chronometerCountDown", false)
            }.getOrDefault(false)
        } ?: false

        val hasTimerKeyword = TIMER_KEYWORDS.any { fullText.contains(it) } ||
            actionLabels.any { it.contains("+1") || it.contains("timer") || it.contains("add 1") }

        if (isStopwatchCategory && !hasTimerKeyword) return true
        if (hasLapAction && (isClockApp || isStopwatchCategory || hasStopwatchKeyword)) return true

        if (isClockApp) {
            if (hasStopwatchKeyword) return true
            if (isChronometer && !isChronometerCountDown && !hasTimerKeyword) return true
            if (actionLabels.any { it.equals("pause", true) || it.equals("resume", true) || it.equals("reset", true) } &&
                !hasTimerKeyword && !isChronometerCountDown && notification.`when` <= System.currentTimeMillis()
            ) {
                return true
            }
        }

        return false
    }

    /**
     * Checks if a Timer notification has completed / expired ("Time's up").
     */
    fun isTimerFinished(notification: Notification, packageName: String = ""): Boolean {
        val pkg = packageName.lowercase()
        if (NotificationFilter.isMessagingPackage(pkg) ||
            NotificationFilter.isMessagingNotification(notification, pkg)
        ) {
            return false
        }

        // Paused timer is never finished
        if (isTimerPaused(notification)) return false

        val fullText = extractFullText(notification)

        val finishKeywords = listOf(
            "time's up", "times up", "time is up", "timer finished",
            "timer expired", "timer ended", "timer done"
        )
        if (finishKeywords.any { fullText.contains(it) }) {
            val isClockApp = isClockPackage(pkg)
            val category = runCatching { notification.category }.getOrNull().orEmpty().lowercase()
            if (isClockApp || category == "alarm" || fullText.contains("timer")) {
                return true
            }
        }

        val isZeroTime = fullText.contains("00:00") || fullText.contains("0:00") || fullText.contains("00:00:00")
        val flags = runCatching { notification.flags }.getOrDefault(0)
        val isOngoing = (flags and Notification.FLAG_ONGOING_EVENT) != 0
        if (isZeroTime && !isOngoing && (isClockPackage(pkg) || fullText.contains("timer"))) return true

        return false
    }

    /**
     * Checks if a Timer is currently paused.
     */
    fun isTimerPaused(notification: Notification): Boolean {
        val actionLabels = extractActionLabels(notification)
        val fullText = extractFullText(notification)

        val hasResumeAction = actionLabels.any {
            it.contains("resume") || it.contains("start") || it.contains("play") || it.contains("continue") || it.contains("unpause")
        }
        val hasPauseAction = actionLabels.any { it.contains("pause") }
        val hasPausedKeyword = fullText.contains("paused") || fullText.contains("pause")

        if (hasResumeAction) return true
        if (hasPausedKeyword && !hasPauseAction) return true
        return false
    }

    /**
     * Checks if a Stopwatch is currently paused.
     */
    fun isStopwatchPaused(notification: Notification): Boolean {
        val actionLabels = extractActionLabels(notification)
        val fullText = extractFullText(notification)

        val hasResumeAction = actionLabels.any {
            it.contains("resume") || it.contains("start") || it.contains("play") || it.contains("continue") || it.contains("unpause")
        }
        val hasPauseAction = actionLabels.any { it.contains("pause") }
        val hasPausedKeyword = fullText.contains("paused") || fullText.contains("pause")

        if (hasResumeAction) return true
        if (hasPausedKeyword && !hasPauseAction) return true
        return false
    }

    /**
     * Extracts the remaining time in seconds from a timer notification.
     */
    fun parseTimerRemainingSeconds(notification: Notification): Long? {
        val isPaused = isTimerPaused(notification)
        val extras = notification.extras

        // 1. If running and has chronometer countdown base, this is the most accurate
        if (extras != null && !isPaused) {
            val isCountDown = runCatching {
                extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false) ||
                extras.getBoolean("android.chronometerCountDown", false)
            }.getOrDefault(false)
            val chronometerBase = runCatching {
                extras.getLong("android.chronometerBase", 0L)
            }.getOrDefault(0L)

            if (isCountDown && chronometerBase > 0L) {
                val diffMs = chronometerBase - SystemClock.elapsedRealtime()
                if (diffMs > 0) return (diffMs + 500L) / 1000L
            }
        }

        // 2. If running and notification.when is in future
        if (!isPaused) {
            val notifWhen = runCatching { notification.`when` }.getOrDefault(0L)
            if (notifWhen > System.currentTimeMillis()) {
                val diffSec = (notifWhen - System.currentTimeMillis() + 500L) / 1000L
                if (diffSec > 0) return diffSec
            }
        }

        // 3. Inspect individual text fields for timer countdown
        val title = runCatching { extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() }.getOrNull().orEmpty()
        val text = runCatching { extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() }.getOrNull().orEmpty()
        val subText = runCatching { extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() }.getOrNull().orEmpty()
        val bigText = runCatching { extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() }.getOrNull().orEmpty()

        for (field in listOf(title, text, subText, bigText)) {
            if (field.isBlank()) continue
            val parsed = parseTimeStringToSeconds(field)
            if (parsed != null && parsed >= 0) return parsed
        }

        return null
    }

    /**
     * Parses a string containing time (e.g. "04:55", "00:04:55", "5m 30s") into total seconds.
     */
    fun parseTimeStringToSeconds(input: String): Long? {
        val matcher = TIME_PATTERN.matcher(input)
        if (matcher.find()) {
            val hours = matcher.group(1)?.toLongOrNull() ?: 0L
            val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
            val seconds = matcher.group(3)?.toLongOrNull() ?: 0L
            val totalSeconds = hours * 3600L + minutes * 60L + seconds
            if (totalSeconds >= 0) return totalSeconds
        }

        val hrMatcher = Pattern.compile("(\\d+)\\s*(?:h|hr|hrs|hours?)\\b", Pattern.CASE_INSENSITIVE).matcher(input)
        val minMatcher = Pattern.compile("(\\d+)\\s*(?:m|min|mins|minutes?)\\b", Pattern.CASE_INSENSITIVE).matcher(input)
        val secMatcher = Pattern.compile("(\\d+)\\s*(?:s|sec|secs|seconds?)\\b", Pattern.CASE_INSENSITIVE).matcher(input)
        val hrs = if (hrMatcher.find()) hrMatcher.group(1)?.toLongOrNull() ?: 0L else 0L
        val mins = if (minMatcher.find()) minMatcher.group(1)?.toLongOrNull() ?: 0L else 0L
        val secs = if (secMatcher.find()) secMatcher.group(1)?.toLongOrNull() ?: 0L else 0L
        if (hrs > 0 || mins > 0 || secs > 0) {
            return hrs * 3600L + mins * 60L + secs
        }

        return null
    }

    /**
     * Extracts the elapsed time in seconds from a stopwatch notification.
     */
    fun parseStopwatchElapsedSeconds(notification: Notification): Long? {
        val isPaused = isStopwatchPaused(notification)
        val extras = notification.extras

        if (extras != null && !isPaused) {
            val isCountDown = runCatching {
                extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN, false) ||
                extras.getBoolean("android.chronometerCountDown", false)
            }.getOrDefault(false)
            val chronometerBase = runCatching {
                extras.getLong("android.chronometerBase", 0L)
            }.getOrDefault(0L)

            if (!isCountDown && chronometerBase > 0L) {
                val diffMs = SystemClock.elapsedRealtime() - chronometerBase
                if (diffMs >= 0) return diffMs / 1000L
            }
        }

        val title = runCatching { extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() }.getOrNull().orEmpty()
        val text = runCatching { extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() }.getOrNull().orEmpty()
        val subText = runCatching { extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() }.getOrNull().orEmpty()
        val bigText = runCatching { extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() }.getOrNull().orEmpty()

        for (field in listOf(title, text, subText, bigText)) {
            if (field.isBlank()) continue
            val parsed = parseTimeStringToSeconds(field)
            if (parsed != null && parsed >= 0) return parsed
        }

        if (!isPaused) {
            val notifWhen = runCatching { notification.`when` }.getOrDefault(0L)
            if (notifWhen in 1..System.currentTimeMillis()) {
                val diffSec = (System.currentTimeMillis() - notifWhen) / 1000L
                if (diffSec >= 0) return diffSec
            }
        }

        return null
    }

    /**
     * Formats seconds into MM:SS or HH:MM:SS string.
     */
    fun formatTime(totalSeconds: Long): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0L)
        val hours = safeSeconds / 3600L
        val minutes = (safeSeconds % 3600L) / 60L
        val seconds = safeSeconds % 60L

        return if (hours > 0) {
            String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
