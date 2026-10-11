/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.model

import android.app.PendingIntent
import android.graphics.Bitmap

data class IslandNotification(
    val key: String = "",
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val bigText: String? = null,
    val timeMillis: Long,
    val icon: Bitmap? = null,
    val largeIcon: Bitmap? = null,
    val actionIntents: List<IslandNotificationAction> = emptyList(),
    val category: String? = null,
    val progress: Int = 0,
    val progressMax: Int = 0,
    val mediaPositionMs: Long? = null,
    val mediaDurationMs: Long? = null,
    val mediaIsPlaying: Boolean = false,
    val mediaToken: android.media.session.MediaSession.Token? = null,
    val mode: IslandMode = IslandMode.Notification,
    val contentIntent: PendingIntent? = null,
    val picture: Bitmap? = null,
    val messages: List<IslandChatMessage> = emptyList(),
    val isConversation: Boolean = false,
    val isCallActiveExplicit: Boolean? = null
) {
    // Derived: no need to store separately
    val actions: List<String>
        get() = actionIntents.map { it.title }

    val isCallRinging: Boolean
        get() = com.agupta07505.smartisland.util.CallStateParser.isCallRinging(actionIntents, title, text, bigText)

    val isCallDialing: Boolean
        get() = mode == IslandMode.IncomingCall &&
            (isCallActiveExplicit != true) &&
            com.agupta07505.smartisland.util.CallStateParser.isCallDialing(actionIntents, title, text, bigText)

    val isCallActive: Boolean
        get() = isCallActiveExplicit ?: (mode == IslandMode.IncomingCall && !isCallRinging && !isCallDialing)

    val isMessaging: Boolean
        get() = mode != IslandMode.DownloadUpload &&
            mode != IslandMode.Music &&
            isConversation

    val isDownloadDone: Boolean
        get() {
            if (mode != IslandMode.DownloadUpload) return false
            if (progressMax > 0 && progress >= progressMax) return true
            val combined = "$title $text $bigText".lowercase()
            val keywords = listOf(
                "complete", "completed", "finished", "downloaded", "uploaded", "exported",
                "saved", "successful", "successfully", "done", "file saved", "sent successfully",
                "installed", "install finished", "tap to open", "tap to view"
            )
            return keywords.any { combined.contains(it) }
        }
}

data class IslandChatMessage(
    val sender: String? = null,
    val text: String = "",
    val timeMillis: Long = System.currentTimeMillis(),
    val isFromUser: Boolean = false,
    val picture: Bitmap? = null
)

data class IslandNotificationAction(
    val title: String,
    val pendingIntent: PendingIntent?,
    val isQuickReply: Boolean = false,
    val remoteInputKey: String? = null
)
