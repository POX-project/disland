/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.app.Person
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.agupta07505.smartisland.model.IslandChatMessage
import com.agupta07505.smartisland.model.IslandNotificationAction

data class ExtractedNotificationContent(
    val title: String,
    val text: String,
    val bigText: String,
    val picture: Bitmap? = null,
    val actions: List<IslandNotificationAction> = emptyList(),
    val messages: List<IslandChatMessage> = emptyList(),
    val isConversation: Boolean = false
)

object NotificationContentExtractor {

    private const val TAG = "NotifContentExtractor"
    private const val MAX_DIMENSION = 512

    private val REPLY_KEYWORDS = setOf(
        "reply", "responder", "répondre", "repondre", "antworten", "rispondi",
        "odpowiedz", "ответить", "відповісти", "رد", "回复", "回覆", "返信",
        "balas", "jawab", "답장"
    )

    fun isReplyTitle(title: String): Boolean {
        val lower = title.lowercase().trim()
        return REPLY_KEYWORDS.any { lower.contains(it) }
    }

    /**
     * Extracts full notification details across all standard Android notification styles:
     * - MessagingStyle (WhatsApp, Telegram, Instagram, Google Messages, Signal, etc.)
     * - InboxStyle (Gmail, Outlook, Twitter digests)
     * - BigTextStyle
     * - BigPictureStyle (Instagram photos, screenshot alerts, camera alerts)
     * - Custom and fallback layouts
     */
    fun extract(
        sbn: StatusBarNotification,
        context: Context,
        appName: String,
        channelIsConversation: Boolean = false
    ): ExtractedNotificationContent {
        val notification = sbn.notification
        val extras = notification.extras

        val pkgName = runCatching { sbn.packageName }.getOrNull() ?: ""
        val postTime = runCatching { sbn.postTime }.getOrNull() ?: System.currentTimeMillis()
        val shortcutId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { notification.shortcutId }.getOrNull()
        } else null

        val isConversation = NotificationFilter.isAuthenticConversation(
            notification = notification,
            packageName = pkgName,
            shortcutId = shortcutId,
            channelIsConversation = channelIsConversation
        )

        // 1. Extract Actions (Quick reply capability strictly reserved for authentic conversations)
        val rawActions = extractActions(notification, isConversation).toMutableList()

        // If it is an authentic conversation and has NO reply action, synthesize one!
        if (isConversation && rawActions.none { it.isQuickReply }) {
            val replyPendingIntent = notification.contentIntent
            if (replyPendingIntent != null) {
                rawActions.add(
                    0,
                    IslandNotificationAction(
                        title = "Reply",
                        pendingIntent = replyPendingIntent,
                        isQuickReply = true,
                        remoteInputKey = "key_text_reply"
                    )
                )
            }
        }

        // 2. Extract Title
        val title = extractTitle(extras, appName, notification)

        // 3. Extract Text, BigText & Structured Chat Messages (ONLY for authentic conversations)
        val (text, bigText) = extractText(extras, notification)
        val messages = if (isConversation) {
            extractChatMessages(extras, notification, title, text, postTime, isAuthenticChat = true)
        } else {
            emptyList()
        }

        // 4. Extract Picture (for BigPictureStyle)
        val picture = extractPicture(extras, context)

        // 5. Final fallback sanitation
        val safeTitle = if (title.isNotBlank()) title.take(200) else appName.take(200)
        val safeText = when {
            text.isNotBlank() -> text.take(1000)
            picture != null -> "[Photo]"
            else -> ""
        }
        val safeBigText = when {
            bigText.isNotBlank() -> bigText.take(4000)
            safeText.isNotBlank() -> safeText
            else -> safeTitle
        }

        return ExtractedNotificationContent(
            title = safeTitle,
            text = safeText,
            bigText = safeBigText,
            picture = picture,
            actions = rawActions,
            messages = messages,
            isConversation = isConversation
        )
    }

    /**
     * Extracts the best title from notification extras or fallbacks.
     */
    fun extractTitle(extras: Bundle?, appName: String, notification: Notification? = null): String {
        if (extras != null) {
            // Group conversation title (e.g. WhatsApp / Telegram group name)
            extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()?.takeIf { it.isNotBlank() }
                ?.let { return it.trim() }

            // Standard title
            extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.takeIf { it.isNotBlank() }
                ?.let { return it.trim() }

            // Big style title
            extras.getCharSequence("android.title.big")?.toString()?.takeIf { it.isNotBlank() }
                ?.let { return it.trim() }

            // MessagingStyle person / sender fallback
            extractMessagingSender(extras)?.takeIf { it.isNotBlank() }
                ?.let { return it.trim() }

            // SubText fallback
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIf { it.isNotBlank() }
                ?.let { return it.trim() }
        }

        // Custom RemoteViews fallback if title is still blank
        if (notification != null) {
            val rvTexts = extractTextFromRemoteViews(
                notification.bigContentView ?: notification.contentView ?: notification.headsUpContentView
            )
            if (rvTexts.isNotEmpty()) {
                return rvTexts.first()
            }
        }

        return appName
    }

    /**
     * Extracts message text and big text from all supported styles.
     */
    fun extractText(extras: Bundle?, notification: Notification): Pair<String, String> {
        var text = ""
        var bigText = ""

        if (extras != null) {
            // 1. MessagingStyle messages (e.g. WhatsApp, Telegram, Instagram DMs, audio notes)
            val messagingExtracted = extractMessagingStyleText(extras)
            if (messagingExtracted != null) {
                text = messagingExtracted.first
                bigText = messagingExtracted.second
            }

            // 2. InboxStyle text lines (e.g. Gmail, Outlook, multi-line digests)
            if (text.isBlank()) {
                val inboxExtracted = extractInboxStyleText(extras)
                if (inboxExtracted != null) {
                    text = inboxExtracted.first
                    bigText = inboxExtracted.second
                }
            }

            // 3. BigTextStyle
            if (text.isBlank() || bigText.isBlank()) {
                val directBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                if (!directBigText.isNullOrBlank()) {
                    bigText = directBigText.trim()
                    if (text.isBlank()) {
                        text = directBigText.trim()
                    }
                }
            }

            // 4. Standard text
            if (text.isBlank()) {
                extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }
                    ?.let { text = it.trim() }
            }

            // 5. Secondary text extras
            if (text.isBlank()) {
                extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIf { it.isNotBlank() }
                    ?.let { text = it.trim() }
            }
            if (text.isBlank()) {
                extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()?.takeIf { it.isNotBlank() }
                    ?.let { text = it.trim() }
            }
            if (text.isBlank()) {
                extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()?.takeIf { it.isNotBlank() }
                    ?.let { text = it.trim() }
            }
        }

        // 6. TickerText fallback
        if (text.isBlank()) {
            notification.tickerText?.toString()?.takeIf { it.isNotBlank() }
                ?.let { text = it.trim() }
        }

        // 7. Custom RemoteViews fallback (for apps using custom notification views)
        if (text.isBlank()) {
            val rvTexts = extractTextFromRemoteViews(
                notification.bigContentView ?: notification.contentView ?: notification.headsUpContentView
            )
            if (rvTexts.isNotEmpty()) {
                text = if (rvTexts.size > 1) {
                    rvTexts.subList(1, rvTexts.size).joinToString(" • ")
                } else {
                    rvTexts.first()
                }
                if (bigText.isBlank()) {
                    bigText = rvTexts.joinToString("\n")
                }
            }
        }

        if (bigText.isBlank()) {
            bigText = text
        }

        return Pair(text, bigText)
    }

    /**
     * Safely extracts text strings from custom RemoteViews using reflection.
     */
    fun extractTextFromRemoteViews(remoteViews: android.widget.RemoteViews?): List<String> {
        if (remoteViews == null) return emptyList()
        val strings = mutableListOf<String>()
        runCatchingLogged(TAG, "Failed extracting text from RemoteViews") {
            val field = runCatching { remoteViews.javaClass.getDeclaredField("mActions") }
                .getOrNull() ?: return emptyList()
            field.isAccessible = true
            val actions = field.get(remoteViews) as? List<*> ?: return emptyList()
            for (action in actions) {
                if (action == null) continue
                val actionClass = action.javaClass
                val methodName = runCatching {
                    val mField = actionClass.getDeclaredField("methodName").apply { isAccessible = true }
                    mField.get(action) as? String
                }.getOrNull() ?: runCatching {
                    val mField = actionClass.getDeclaredField("mMethodName").apply { isAccessible = true }
                    mField.get(action) as? String
                }.getOrNull()

                if (methodName == "setText") {
                    val value = runCatching {
                        val vField = actionClass.getDeclaredField("value").apply { isAccessible = true }
                        vField.get(action) as? CharSequence
                    }.getOrNull() ?: runCatching {
                        val vField = actionClass.getDeclaredField("mValue").apply { isAccessible = true }
                        vField.get(action) as? CharSequence
                    }.getOrNull()

                    val textStr = value?.toString()?.trim()
                    if (!textStr.isNullOrBlank() && !strings.contains(textStr)) {
                        strings.add(textStr)
                    }
                }
            }
        }
        return strings
    }

    /**
     * Extracts sender from MessagingStyle messages.
     */
    private fun extractMessagingSender(extras: Bundle): String? {
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return null
        if (messages.isEmpty()) return null

        val lastBundle = messages.lastOrNull() as? Bundle ?: return null
        // API 28+ sender_person
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val person = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lastBundle.getParcelable("sender_person", Person::class.java)
            } else {
                @Suppress("DEPRECATION")
                lastBundle.getParcelable("sender_person") as? Person
            }
            if (person != null && !person.name.isNullOrBlank()) {
                return person.name.toString()
            }
        }

        return lastBundle.getCharSequence("sender")?.toString()
    }

    /**
     * Parses MessagingStyle extras into latest message text and joined conversation history.
     * Accurately supports voice messages, audio notes, photos, videos, and media attachments.
     */
    private fun extractMessagingStyleText(extras: Bundle): Pair<String, String>? {
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (messages.isNullOrEmpty()) return null

        val messageLines = mutableListOf<String>()
        var latestText = ""

        for (item in messages) {
            val bundle = item as? Bundle ?: continue
            val rawText = bundle.getCharSequence("text")?.toString()?.takeIf { it.isNotBlank() }
            val mimeType = bundle.getString("type")
            val msgText = when {
                rawText != null -> rawText
                mimeType?.startsWith("audio/") == true -> "Voice message"
                mimeType?.startsWith("video/") == true -> "Video"
                mimeType?.startsWith("image/") == true -> "Photo"
                mimeType != null -> "Attachment"
                bundle.containsKey("uri") -> "Media message"
                else -> null
            } ?: continue

            var senderName: String? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val person = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable("sender_person", Person::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelable("sender_person") as? Person
                }
                senderName = person?.name?.toString()
            }
            if (senderName.isNullOrBlank()) {
                senderName = bundle.getCharSequence("sender")?.toString()
            }

            val line = if (!senderName.isNullOrBlank()) "$senderName: $msgText" else msgText
            messageLines.add(line)
            latestText = msgText
        }

        if (latestText.isBlank() && messageLines.isEmpty()) return null

        val fullHistory = messageLines.takeLast(8).joinToString("\n")
        return Pair(latestText, fullHistory.ifBlank { latestText })
    }

    /**
     * Parses InboxStyle lines.
     */
    private fun extractInboxStyleText(extras: Bundle): Pair<String, String>? {
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lines.isNullOrEmpty()) return null

        val nonBlankLines = lines.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
        if (nonBlankLines.isEmpty()) return null

        val latest = nonBlankLines.last()
        val all = nonBlankLines.joinToString("\n")
        return Pair(latest, all)
    }

    /**
     * Extracts pictures attached to BigPictureStyle notifications.
     */
    fun extractPicture(extras: Bundle?, context: Context): Bitmap? {
        if (extras == null) return null

        // 1. Direct Bitmap in EXTRA_PICTURE
        val pictureObj = extras.get(Notification.EXTRA_PICTURE)
        if (pictureObj is Bitmap) {
            return pictureObj.downsample(MAX_DIMENSION)
        }

        // 2. EXTRA_PICTURE_ICON (Android 12+ or backward-compatible Icon/Bitmap)
        val pictureIcon = extras.get("android.pictureIcon")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pictureIcon is Icon) {
            runCatchingLogged(TAG, "Failed to load pictureIcon as Icon") {
                pictureIcon.loadDrawable(context)?.toBitmap()?.downsample(MAX_DIMENSION)
            }?.let { return it }
        } else if (pictureIcon is Bitmap) {
            return pictureIcon.downsample(MAX_DIMENSION)
        }

        // 3. Fallback: Check if bigPicture bitmap is present
        val bigPicture = extras.get("android.bigPicture")
        if (bigPicture is Bitmap) {
            return bigPicture.downsample(MAX_DIMENSION)
        }

        return null
    }

    /**
     * Parses structured chat messages from MessagingStyle extras or InboxStyle.
     */
    fun extractChatMessages(
        extras: Bundle?,
        notification: Notification,
        fallbackTitle: String,
        fallbackText: String,
        postTime: Long,
        isAuthenticChat: Boolean = false
    ): List<IslandChatMessage> {
        if (!isAuthenticChat) return emptyList()

        val chatMessages = mutableListOf<IslandChatMessage>()
        if (extras == null) {
            if (fallbackText.isNotBlank()) {
                chatMessages.add(
                    IslandChatMessage(
                        sender = fallbackTitle,
                        text = fallbackText,
                        timeMillis = postTime,
                        isFromUser = false
                    )
                )
            }
            return chatMessages
        }

        // 1. Check MessagingStyle messages (both EXTRA_MESSAGES and EXTRA_HISTORIC_MESSAGES)
        val currentMsgs = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        val historicMsgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            extras.getParcelableArray("android.historicMessages")
        } else null

        val allMsgBundles = mutableListOf<Bundle>()
        historicMsgs?.forEach { if (it is Bundle) allMsgBundles.add(it) }
        currentMsgs?.forEach { if (it is Bundle) allMsgBundles.add(it) }

        // User person from MessagingStyle if available
        val userPerson = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            extras.getParcelable("android.messagingStyleUser") as? Person
        } else null
        val userPersonName = userPerson?.name?.toString()
        val userPersonKey = userPerson?.key

        for (bundle in allMsgBundles) {
            val rawText = bundle.getCharSequence("text")?.toString()?.takeIf { it.isNotBlank() }
            val mimeType = bundle.getString("type")
            val msgText = when {
                rawText != null -> rawText
                mimeType?.startsWith("audio/") == true -> "Voice message"
                mimeType?.startsWith("video/") == true -> "Video"
                mimeType?.startsWith("image/") == true -> "Photo"
                mimeType != null -> "Attachment"
                bundle.containsKey("uri") -> "Media message"
                else -> null
            } ?: continue

            var senderName: String? = null
            var isUser = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val person = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable("sender_person", Person::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelable("sender_person") as? Person
                }
                senderName = person?.name?.toString()
                if (person == null) {
                    isUser = true
                } else if ((userPersonKey != null && person.key == userPersonKey) ||
                    (userPersonName != null && person.name?.toString() == userPersonName) ||
                    person.name?.toString()?.equals("You", ignoreCase = true) == true
                ) {
                    isUser = true
                }
            }
            if (senderName.isNullOrBlank()) {
                senderName = bundle.getCharSequence("sender")?.toString()
            }
            if (senderName?.equals("You", ignoreCase = true) == true) {
                isUser = true
            }
            if (senderName.isNullOrBlank() && !isUser) {
                senderName = fallbackTitle
            }

            val timestamp = bundle.getLong("time", postTime)
            chatMessages.add(
                IslandChatMessage(
                    sender = if (isUser) "You" else senderName,
                    text = msgText,
                    timeMillis = if (timestamp > 0) timestamp else postTime,
                    isFromUser = isUser
                )
            )
        }

        // 2. Extract RemoteInput history (user replies sent from notifications)
        val remoteInputHistory = extras.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY)
        if (!remoteInputHistory.isNullOrEmpty()) {
            for (reply in remoteInputHistory) {
                val rText = reply?.toString()?.trim().orEmpty()
                if (rText.isNotBlank()) {
                    chatMessages.add(
                        IslandChatMessage(
                            sender = "You",
                            text = rText,
                            timeMillis = postTime,
                            isFromUser = true
                        )
                    )
                }
            }
        }

        // 3. If MessagingStyle empty, check InboxStyle lines ONLY for authentic chat notifications
        if (chatMessages.isEmpty() && isAuthenticChat) {
            val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            if (!lines.isNullOrEmpty()) {
                val nonBlank = lines.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
                for (line in nonBlank) {
                    val colonIdx = line.indexOf(':')
                    val (sender, text) = if (colonIdx > 0 && colonIdx < 30) {
                        Pair(line.substring(0, colonIdx).trim(), line.substring(colonIdx + 1).trim())
                    } else {
                        Pair(fallbackTitle, line)
                    }
                    val isFromUser = sender.equals("You", ignoreCase = true)
                    chatMessages.add(
                        IslandChatMessage(
                            sender = if (isFromUser) "You" else sender,
                            text = text,
                            timeMillis = postTime,
                            isFromUser = isFromUser
                        )
                    )
                }
            }
        }

        // 4. Fallback: single message ONLY for authentic chat conversations
        if (chatMessages.isEmpty() && isAuthenticChat && fallbackText.isNotBlank()) {
            chatMessages.add(
                IslandChatMessage(
                    sender = fallbackTitle,
                    text = fallbackText,
                    timeMillis = postTime,
                    isFromUser = false
                )
            )
        }

        return chatMessages.takeLast(30)
    }

    /**
     * Extracts actions and identifies quick reply actions with remote inputs across
     * framework actions, AndroidX compat actions, invisible actions, car extender,
     * and wearable extensions.
     */
    fun extractActions(notification: Notification, isConversation: Boolean = true): List<IslandNotificationAction> {
        val actionsList = mutableListOf<IslandNotificationAction>()
        val seenTitles = mutableSetOf<String>()

        // 1. Check NotificationCompat actions (covers framework actions + compat extras)
        runCatching {
            val compatCount = NotificationCompat.getActionCount(notification)
            for (i in 0 until compatCount) {
                val compatAction = NotificationCompat.getAction(notification, i) ?: continue
                val title = compatAction.title?.toString()?.takeIf { it.isNotBlank() } ?: continue
                if (!seenTitles.add(title.lowercase())) continue

                val remoteInputs = compatAction.remoteInputs
                val remoteInput = remoteInputs?.firstOrNull()
                val isSemanticReply = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    compatAction.semanticAction == NotificationCompat.Action.SEMANTIC_ACTION_REPLY
                } else false
                val isReply = isConversation && (remoteInput != null || isSemanticReply || isReplyTitle(title))

                actionsList.add(
                    IslandNotificationAction(
                        title = title,
                        pendingIntent = compatAction.actionIntent,
                        isQuickReply = isReply,
                        remoteInputKey = remoteInput?.resultKey ?: "key_text_reply"
                    )
                )
            }
        }

        // 2. Check InvisibleActions (vital for Google Messages / direct reply services)
        runCatching {
            val invisibleActions = NotificationCompat.getInvisibleActions(notification)
            for (invAction in invisibleActions) {
                val title = invAction.title?.toString()?.takeIf { it.isNotBlank() } ?: "Reply"
                if (!seenTitles.add(title.lowercase())) continue

                val remoteInputs = invAction.remoteInputs
                val remoteInput = remoteInputs?.firstOrNull()
                val isSemanticReply = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    invAction.semanticAction == NotificationCompat.Action.SEMANTIC_ACTION_REPLY
                } else false
                val isReply = isConversation && (remoteInput != null || isSemanticReply || isReplyTitle(title))

                actionsList.add(
                    IslandNotificationAction(
                        title = title,
                        pendingIntent = invAction.actionIntent,
                        isQuickReply = isReply,
                        remoteInputKey = remoteInput?.resultKey ?: "key_text_reply"
                    )
                )
            }
        }

        // 3. Check WearableExtender actions (companion extensions frequently hold reply RemoteInputs)
        runCatching {
            val wearableActions = NotificationCompat.WearableExtender(notification).actions
            for (wAction in wearableActions) {
                val title = wAction.title?.toString()?.takeIf { it.isNotBlank() } ?: continue
                if (!seenTitles.add(title.lowercase())) continue

                val remoteInput = wAction.remoteInputs?.firstOrNull()
                val isReply = isConversation && (remoteInput != null || isReplyTitle(title))

                actionsList.add(
                    IslandNotificationAction(
                        title = title,
                        pendingIntent = wAction.actionIntent,
                        isQuickReply = isReply,
                        remoteInputKey = remoteInput?.resultKey ?: "key_text_reply"
                    )
                )
            }
        }

        // 4. Check CarExtender UnreadConversation (Android Auto / Assistant reply intent)
        runCatching {
            val carExtender = NotificationCompat.CarExtender(notification)
            val unreadConv = carExtender.unreadConversation
            if (unreadConv != null && unreadConv.replyPendingIntent != null && isConversation) {
                val title = "Reply"
                if (seenTitles.add(title.lowercase())) {
                    actionsList.add(
                        IslandNotificationAction(
                            title = title,
                            pendingIntent = unreadConv.replyPendingIntent,
                            isQuickReply = true,
                            remoteInputKey = unreadConv.remoteInput?.resultKey ?: "key_text_reply"
                        )
                    )
                }
            }
        }

        // 5. Check raw framework notification.actions (unconditionally, deduplicating)
        runCatching {
            val rawActions = notification.actions.orEmpty()
            for (action in rawActions) {
                if (action == null) continue
                val title = action.title?.toString()?.takeIf { it.isNotBlank() } ?: continue
                if (!seenTitles.add(title.lowercase())) continue

                val remoteInput = action.remoteInputs?.firstOrNull()
                val isSemanticReply = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    action.semanticAction == Notification.Action.SEMANTIC_ACTION_REPLY
                } else false
                val isReply = isConversation && (remoteInput != null || isSemanticReply || isReplyTitle(title))

                actionsList.add(
                    IslandNotificationAction(
                        title = title,
                        pendingIntent = action.actionIntent,
                        isQuickReply = isReply,
                        remoteInputKey = remoteInput?.resultKey ?: "key_text_reply"
                    )
                )
            }
        }

        return actionsList
    }

    /**
     * Helper to downsample a bitmap if it exceeds max dimension.
     */
    fun Bitmap.downsample(maxDimension: Int = MAX_DIMENSION): Bitmap {
        if (width <= maxDimension && height <= maxDimension) return this
        val scale = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return runCatchingLogged(TAG, "downsample failed") {
            Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
        } ?: this
    }
}

