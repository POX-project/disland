/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import com.agupta07505.smartisland.model.IslandNotificationAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SmartIslandNotificationRepository : INotificationRepository {
    private val _notifications = MutableStateFlow<List<IslandNotification>>(emptyList())
    override val notifications: StateFlow<List<IslandNotification>> = _notifications.asStateFlow()

    private val _autoExpandEvent = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val autoExpandEvent: SharedFlow<String> = _autoExpandEvent.asSharedFlow()

    private val _resetTimerEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    override val resetTimerEvent: SharedFlow<Unit> = _resetTimerEvent.asSharedFlow()

    private val _commands = MutableSharedFlow<SmartIslandCommand>(extraBufferCapacity = 16)
    override val commands: SharedFlow<SmartIslandCommand> = _commands.asSharedFlow()

    private fun isProgressOrTimeRemainingUpdate(text: String): Boolean {
        val lower = text.lowercase()
        return listOf(
            "remaining", "left", "eta", "time remaining", "time left",
            "mb/s", "kb/s", "gb/s", "kbps", "mbps", "b/s", "k/s", "m/s",
            "bytes", "mb /", "gb /", "kb /", "mb of", "gb of", "%",
            "downloading", "uploading", "installing", "updating"
        ).any { lower.contains(it) }
    }

    override fun postNotification(notification: IslandNotification, autoExpand: Boolean) {
        val isDownloadOrProgress = notification.mode == IslandMode.DownloadUpload ||
            notification.mode == IslandMode.Music ||
            notification.mode == IslandMode.ScreenRecording ||
            notification.mode == IslandMode.Timer ||
            notification.mode == IslandMode.Stopwatch ||
            notification.progressMax > 0

        var shouldTriggerAutoExpand = false
        _notifications.update { existingNotifications ->
            val updated = existingNotifications.toMutableList()
            val index = updated.indexOfFirst { it.key == notification.key }
            if (index >= 0) {
                val existing = updated[index]
                val isExistingDownloadOrProgress = isDownloadOrProgress ||
                    existing.mode == IslandMode.DownloadUpload ||
                    existing.progressMax > 0 ||
                    isProgressOrTimeRemainingUpdate(notification.text) ||
                    isProgressOrTimeRemainingUpdate(existing.text)

                // For existing notifications, NEVER auto-expand on progress or time-remaining updates!
                // Only allow auto-expand if a genuine new chat message from the contact arrived in the thread.
                val hasNewIncomingChatMessage = notification.isMessaging &&
                    notification.messages.size > existing.messages.size &&
                    notification.messages.lastOrNull()?.isFromUser == false

                if (!isExistingDownloadOrProgress && (hasNewIncomingChatMessage || (existing.title != notification.title && autoExpand))) {
                    shouldTriggerAutoExpand = true
                }

                val mergedMessages = mergeChatMessages(existing.messages, notification.messages)
                val finalMessages = if (mergedMessages.isNotEmpty()) mergedMessages else notification.messages
                updated[index] = notification.copy(
                    icon = notification.icon ?: existing.icon,
                    largeIcon = notification.largeIcon ?: existing.largeIcon,
                    picture = notification.picture ?: existing.picture,
                    messages = finalMessages
                )
            } else {
                if (autoExpand && !isDownloadOrProgress && !isProgressOrTimeRemainingUpdate(notification.text)) {
                    shouldTriggerAutoExpand = true
                }
                updated.add(notification)
            }
            updated.takeLast(MAX_STORED_NOTIFICATIONS)
        }
        if (shouldTriggerAutoExpand) {
            _autoExpandEvent.tryEmit(notification.key)
        }
    }

    override fun appendUserReply(key: String, replyText: String) {
        if (replyText.isBlank()) return
        val now = System.currentTimeMillis()
        val userMsg = com.agupta07505.smartisland.model.IslandChatMessage(
            sender = "You",
            text = replyText.trim(),
            timeMillis = now,
            isFromUser = true
        )
        _notifications.update { list ->
            val updated = list.toMutableList()
            val index = updated.indexOfFirst { it.key == key }
            if (index >= 0) {
                val existing = updated[index]
                val newMessages = (existing.messages + userMsg).takeLast(30)
                updated[index] = existing.copy(
                    text = replyText.trim(),
                    timeMillis = now,
                    messages = newMessages
                )
            }
            updated
        }
    }

    private fun mergeChatMessages(
        existing: List<com.agupta07505.smartisland.model.IslandChatMessage>,
        incoming: List<com.agupta07505.smartisland.model.IslandChatMessage>
    ): List<com.agupta07505.smartisland.model.IslandChatMessage> {
        if (existing.isEmpty()) return incoming
        if (incoming.isEmpty()) return existing

        val result = ArrayList<com.agupta07505.smartisland.model.IslandChatMessage>(existing)
        for (newMsg in incoming) {
            val newText = newMsg.text.trim()
            val existingIndex = result.indexOfLast { existingMsg ->
                val existingText = existingMsg.text.trim()
                if (existingText != newText) return@indexOfLast false
                // Case 1: Same sender flag within 30 seconds
                if (existingMsg.isFromUser == newMsg.isFromUser &&
                    kotlin.math.abs(existingMsg.timeMillis - newMsg.timeMillis) < 30_000L
                ) {
                    return@indexOfLast true
                }
                // Case 2: User reply echo! User sent this exact text recently (< 60s), and the incoming notification
                // contains the same text, possibly tagged without isFromUser flag.
                if (existingMsg.isFromUser && kotlin.math.abs(existingMsg.timeMillis - newMsg.timeMillis) < 60_000L) {
                    return@indexOfLast true
                }
                false
            }

            if (existingIndex >= 0) {
                // If message already existed as user reply, preserve isFromUser = true and update timestamp
                val existingMsg = result[existingIndex]
                if (existingMsg.isFromUser && !newMsg.isFromUser) {
                    result[existingIndex] = existingMsg.copy(
                        timeMillis = kotlin.math.max(existingMsg.timeMillis, newMsg.timeMillis)
                    )
                }
            } else {
                result.add(newMsg)
            }
        }
        result.sortBy { it.timeMillis }
        return result.takeLast(30)
    }

    override fun removeNotification(key: String) {
        _notifications.update { notifications ->
            notifications.filterNot { it.key == key }
        }
    }

    override fun removeNotificationsForPackage(packageName: String) {
        val matching = _notifications.value.filter { it.packageName == packageName && it.mode != IslandMode.Music }
        if (matching.isEmpty()) return
        _notifications.update { list ->
            list.filterNot { it.packageName == packageName && it.mode != IslandMode.Music }
        }
        for (notif in matching) {
            _commands.tryEmit(SmartIslandCommand.CancelNotification(notif.key))
        }
    }

    override fun removeAllNotifications() {
        _notifications.update { emptyList() }
    }

    override fun resetTimer() {
        _resetTimerEvent.tryEmit(Unit)
    }

    override fun sendCommand(command: SmartIslandCommand) {
        _commands.tryEmit(command)
    }

    override fun showDemo(mode: IslandMode) {
        val demoNotification = when (mode) {
            IslandMode.Notification -> IslandNotification(
                key = "demo_notif",
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                title = "Alice Smith",
                text = "Hey! Are we still meeting for lunch today?",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.Notification,
                actionIntents = listOf(
                    IslandNotificationAction("Reply", null, isQuickReply = true, remoteInputKey = "key_text_reply"),
                    IslandNotificationAction("Mark Read", null)
                ),
                messages = listOf(
                    com.agupta07505.smartisland.model.IslandChatMessage(
                        sender = "Alice Smith",
                        text = "Hey! Are we still meeting for lunch today?",
                        timeMillis = System.currentTimeMillis() - 45000L,
                        isFromUser = false
                    )
                )
            )
            IslandMode.IncomingCall -> IslandNotification(
                key = "demo_call",
                packageName = "com.google.android.dialer",
                appName = "Phone",
                title = "John Doe",
                text = "Incoming Call",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.IncomingCall,
                actionIntents = listOf(
                    IslandNotificationAction("Decline", null),
                    IslandNotificationAction("Answer", null)
                )
            )
            IslandMode.Music -> IslandNotification(
                key = "demo_music",
                packageName = "com.spotify.music",
                appName = "Spotify",
                title = "Starlight",
                text = "Muse - Black Holes and Revelations",
                timeMillis = System.currentTimeMillis(),
                largeIcon = createDemoArtworkBitmap(),
                mode = IslandMode.Music,
                mediaIsPlaying = true,
                mediaDurationMs = 240000L,
                mediaPositionMs = 45000L,
                actionIntents = listOf(
                    IslandNotificationAction("Previous", null),
                    IslandNotificationAction("Play", null),
                    IslandNotificationAction("Next", null)
                )
            )
            IslandMode.Battery -> {
                val current = notifications.value.find { it.key == "demo_battery" }
                when (current?.title) {
                    "Charging" -> IslandNotification(
                        key = "demo_battery",
                        packageName = "com.android.systemui",
                        appName = "System",
                        title = "Low Battery",
                        text = "15%",
                        category = "battery_low",
                        timeMillis = System.currentTimeMillis(),
                        mode = IslandMode.Battery
                    )
                    "Low Battery" -> IslandNotification(
                        key = "demo_battery",
                        packageName = "com.android.systemui",
                        appName = "System",
                        title = "Battery Saver ON",
                        text = "18%",
                        category = "battery_saver",
                        timeMillis = System.currentTimeMillis(),
                        mode = IslandMode.Battery
                    )
                    else -> IslandNotification(
                        key = "demo_battery",
                        packageName = "com.android.systemui",
                        appName = "System",
                        title = "Charging",
                        text = "85%",
                        category = "battery_charging",
                        timeMillis = System.currentTimeMillis(),
                        mode = IslandMode.Battery
                    )
                }
            }
            IslandMode.LiveActivity -> IslandNotification(
                key = "demo_live_activity",
                packageName = "com.ubercab",
                appName = "Uber",
                title = "Driver is on the way",
                text = "Arriving in 8 mins • 2.5 km away",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.LiveActivity,
                progress = 40,
                progressMax = 100,
                actionIntents = listOf(
                    IslandNotificationAction("Call Driver", null),
                    IslandNotificationAction("Share Trip", null)
                )
            )
            IslandMode.Navigation -> IslandNotification(
                key = "demo_navigation",
                packageName = "com.google.android.apps.maps",
                appName = "Google Maps",
                title = "Turn left onto MG Road",
                text = "In 300 m • 24 min remaining (8.5 km)",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.Navigation,
                actionIntents = listOf(
                    IslandNotificationAction("Mute Voice", null),
                    IslandNotificationAction("Exit", null)
                )
            )
            IslandMode.DownloadUpload -> {
                val hasDownload = _notifications.value.any { it.key == "demo_download" }
                val hasUpload = _notifications.value.any { it.key == "demo_upload" }
                val hasComplete = _notifications.value.any { it.key == "demo_download_complete" }
                when {
                    !hasDownload -> {
                        IslandNotification(
                            key = "demo_download",
                            packageName = "com.android.chrome",
                            appName = "Chrome",
                            title = "video_2026_download.mp4",
                            text = "Downloading... 145 MB of 280 MB • 52%",
                            timeMillis = System.currentTimeMillis(),
                            mode = IslandMode.DownloadUpload,
                            progress = 52,
                            progressMax = 100,
                            actionIntents = listOf(
                                IslandNotificationAction("Pause", null),
                                IslandNotificationAction("Cancel", null)
                            )
                        )
                    }
                    !hasUpload -> {
                        IslandNotification(
                            key = "demo_upload",
                            packageName = "com.google.android.apps.docs",
                            appName = "Google Drive",
                            title = "presentation_2026.pdf",
                            text = "Uploading... 85 MB of 120 MB • 70%",
                            timeMillis = System.currentTimeMillis(),
                            mode = IslandMode.DownloadUpload,
                            progress = 70,
                            progressMax = 100,
                            actionIntents = listOf(
                                IslandNotificationAction("Cancel", null)
                            )
                        )
                    }
                    else -> {
                        IslandNotification(
                            key = "demo_download_complete",
                            packageName = "com.android.chrome",
                            appName = "Chrome",
                            title = "archive_update.zip",
                            text = "Download complete • 2.8 GB",
                            timeMillis = System.currentTimeMillis(),
                            mode = IslandMode.DownloadUpload,
                            progress = 100,
                            progressMax = 100,
                            actionIntents = listOf(
                                IslandNotificationAction("Open", null)
                            )
                        )
                    }
                }
            }
            IslandMode.Hotspot -> IslandNotification(
                key = "demo_hotspot",
                packageName = "com.android.settings",
                appName = "Mobile Hotspot",
                title = "Mobile Hotspot",
                text = "Active • 2 devices connected",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.Hotspot,
                actionIntents = listOf(
                    IslandNotificationAction("Hotspot Settings", null)
                )
            )
            IslandMode.Bluetooth -> IslandNotification(
                key = "demo_bluetooth",
                packageName = "com.android.bluetooth",
                appName = "Bluetooth",
                title = "AirPods Pro",
                text = "Connected • 100%",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.Bluetooth
            )
            IslandMode.Flashlight -> IslandNotification(
                key = "demo_flashlight",
                packageName = "com.android.systemui",
                appName = "Flashlight",
                title = "Flashlight ON",
                text = "Tap to turn off",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.Flashlight,
                actionIntents = listOf(
                    IslandNotificationAction("Turn Off", null)
                )
            )
            IslandMode.ScreenRecording -> IslandNotification(
                key = "demo_screen_recording",
                packageName = "com.android.systemui",
                appName = "Screen Recorder",
                title = "Screen Recording",
                text = "Recording screen...",
                timeMillis = System.currentTimeMillis(),
                mode = IslandMode.ScreenRecording,
                actionIntents = listOf(
                    IslandNotificationAction("Stop", null)
                )
            )
            IslandMode.Timer -> IslandNotification(
                key = "demo_timer",
                packageName = "com.google.android.deskclock",
                appName = "Clock",
                title = "Tea Timer",
                text = "04:59",
                timeMillis = System.currentTimeMillis() + 299000L,
                mode = IslandMode.Timer,
                actionIntents = listOf(
                    IslandNotificationAction("Pause", null),
                    IslandNotificationAction("Reset", null)
                )
            )
            IslandMode.Stopwatch -> IslandNotification(
                key = "demo_stopwatch",
                packageName = "com.google.android.deskclock",
                appName = "Clock",
                title = "Stopwatch",
                text = "00:14.28",
                timeMillis = System.currentTimeMillis() - 14280L,
                mode = IslandMode.Stopwatch,
                actionIntents = listOf(
                    IslandNotificationAction("Lap", null),
                    IslandNotificationAction("Pause", null),
                    IslandNotificationAction("Reset", null)
                )
            )
            IslandMode.Empty -> null
        }
        if (demoNotification != null) {
            postNotification(demoNotification, autoExpand = true)
        }
    }

    override fun clearTestNotifications() {
        _notifications.update { list ->
            list.filterNot { it.key.startsWith("demo_") }
        }
    }

    private fun createDemoArtworkBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f, 0f, 256f, 256f,
            intArrayOf(0xFF7C3AED.toInt(), 0xFFEC4899.toInt(), 0xFFF59E0B.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, 256f, 256f, paint)
        return bitmap
    }

    private companion object {
        const val MAX_STORED_NOTIFICATIONS = 50
    }
}

sealed interface SmartIslandCommand {
    data class CancelNotification(val key: String) : SmartIslandCommand
    data class SeekTo(val packageName: String, val positionMs: Long) : SmartIslandCommand
}
