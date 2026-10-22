/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import com.agupta07505.smartisland.model.IslandMode


private val TORCH_KEYWORDS = listOf("flashlight", "torch", "flash light")
private val OTP_KEYWORDS = listOf(
    "otp", "verification code", "one-time", "one time password",
    "security code", "auth code", "login code", "do not share",
    "do not reply", "txn", "debited", "credited", "bank alert",
    "transaction alert", "order confirmed", "package delivered"
)
private val CALL_KEYWORDS = listOf("calling", "dialing", "incoming call", "ongoing call", "call in progress", "in call", "ringing")
private val DOWNLOAD_PROGRESS_KEYWORDS = listOf("download", "upload", "saving", "transfer", "remaining", "left", "eta", "mb/s", "kb/s", "mb /", "gb /")
private val TIME_REMAINING_KEYWORDS = listOf("remaining", "left", "eta", "time remaining", "time left")
private val TRANSFER_SPEED_KEYWORDS = listOf("kb/s", "mb/s", "gb/s", "kbps", "mbps", "b/s", "k/s", "m/s", "bytes/s")
private val FILE_SIZE_KEYWORDS = listOf("mb /", "gb /", "kb /", "bytes /", "mb of", "gb of", "kb of", "m of", "g of")
private val ACTIVE_TRANSFER_KEYWORDS = listOf("downloading", "uploading", "exporting", "transferring", "saving file", "downloading file", "uploading file", "installing", "updating", "sending file")
private val GENERIC_TRANSFER_KEYWORDS = listOf("download", "upload", "export", "apk", "pdf", "mp4", "zip", "iso", "mkv", "tar", "gz", "rar", "7z")
private val COMPLETION_KEYWORDS = listOf(
    "complete", "completed", "finished", "downloaded", "uploaded", "exported",
    "saved", "successful", "successfully", "done", "tap to open", "tap to view",
    "file saved", "sent successfully", "installed", "install finished"
)
private val CALL_ENDED_KEYWORDS = listOf(
    "call ended", "call finished", "call declined", "call rejected",
    "missed call", "missed video call", "call disconnected", "hung up"
)
private val SYNC_PHRASES = listOf(
    "syncing messages", "syncing message", "checking for messages", "checking for new messages",
    "syncing chats", "syncing chat", "updating messages", "updating chat", "updating chats",
    "waiting for messages", "waiting for message", "looking for messages", "looking for new messages",
    "connecting to chat", "connecting to messages", "refreshing messages", "refreshing chats",
    "sync in progress", "message sync", "chat sync", "syncing...", "checking messages",
    "checking message", "syncing snaps", "checking snaps", "syncing snapchat", "synchronizing messages",
    "synchronizing chats", "synchronizing...", "backup in progress", "syncing backup",
    "checking for updates", "checking for chats", "connecting..."
)

object NotificationFilter {
    private val SYSTEM_LEVEL_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.agupta07505.smartisland"
    )

    fun shouldSuppressFromIsland(
        sbn: StatusBarNotification,
        packageManager: PackageManager,
        liveActivitiesEnabled: Boolean = true,
        navigationEnabled: Boolean = true,
        disabledNotificationPackages: Set<String> = emptySet(),
        deviceType: String? = null
    ): Boolean {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val mode = notification.toIslandMode(sbn, liveActivitiesEnabled, navigationEnabled, deviceType)

        // Hotspot, Screen Recording, Timer, Stopwatch notifications are allowed even if posted by system/OEM frameworks
        if (mode == IslandMode.Hotspot || mode == IslandMode.ScreenRecording || mode == IslandMode.IncomingCall || mode == IslandMode.Timer || mode == IslandMode.Stopwatch) {
            if (packageName == "com.agupta07505.smartisland") return true
            if (packageName in disabledNotificationPackages) return true
        } else {
            if (packageName in SYSTEM_LEVEL_PACKAGES) return true
            if (packageName in disabledNotificationPackages) return true
            if (isSystemLevelCategory(notification, packageName, packageManager)) return true
            if (isSystemLevelPackage(packageName, packageManager)) return true
        }

        // Suppress group summary notifications only if they are pure empty group summaries without independent message content
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        if (isGroupSummary) {
            val extras = notification.extras
            val hasTextLines = !extras?.getCharSequenceArray(Notification.EXTRA_TEXT_LINES).isNullOrEmpty()
            val hasMessages = !extras?.getParcelableArray(Notification.EXTRA_MESSAGES).isNullOrEmpty()
            val hasDirectContent = !extras?.getCharSequence(Notification.EXTRA_TEXT).isNullOrBlank() ||
                !extras?.getCharSequence(Notification.EXTRA_BIG_TEXT).isNullOrBlank() ||
                !extras?.getCharSequence(Notification.EXTRA_TITLE).isNullOrBlank()
            if (!hasTextLines && !hasMessages && !hasDirectContent) {
                return true
            }
        }

        // Check extracted title and text across all Android notification styles
        val extras = notification.extras
        val extractedTitle = NotificationContentExtractor.extractTitle(extras, "", notification)
        val (extractedText, _) = NotificationContentExtractor.extractText(extras, notification)
        val hasPictureExtra = extras?.containsKey(Notification.EXTRA_PICTURE) == true ||
            extras?.containsKey("android.pictureIcon") == true
        val hasCustomViews = notification.contentView != null ||
            notification.bigContentView != null ||
            notification.headsUpContentView != null

        if (extractedTitle.isBlank() && extractedText.isBlank() && !hasPictureExtra && !hasCustomViews &&
            mode != IslandMode.Stopwatch && mode != IslandMode.Timer && mode != IslandMode.ScreenRecording
        ) {
            return true
        }

        // Suppress external torch / flashlight notifications from entering Smart Island,
        // because Smart Island natively manages physical torch state via CameraManager.TorchCallback.
        val titleText = "$extractedTitle $extractedText".lowercase()
        val isTorchNotification = TORCH_KEYWORDS.any { titleText.contains(it) }
        if (isTorchNotification && packageName != "com.agupta07505.smartisland") {
            return true
        }

        val isOngoing = (notification.flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE)) != 0

        // Suppress background message syncing / polling notifications (e.g. Snapchat, WhatsApp, Telegram "Syncing messages", "Checking for messages")
        val isMessageSync = isMessageSyncNotification(titleText)
        if (isOngoing && isMessageSync) {
            return true
        }

        // Suppress ongoing notifications that are internal system frameworks or pure progress syncs,
        // but preserve user-installed apps' ongoing notifications (weather, voice recording, radio,
        // workouts, fitness, live alerts).
        if (isOngoing) {
            if (isSystemLevelPackage(packageName, packageManager)) {
                val isProgressNotification = !isMessageSync && (
                    notification.category == Notification.CATEGORY_PROGRESS ||
                    (notification.extras?.getInt(Notification.EXTRA_PROGRESS_MAX, 0) ?: 0) > 0
                )
                val isUserVisibleOngoingCategory =
                    notification.category == Notification.CATEGORY_ALARM ||
                    notification.category == Notification.CATEGORY_REMINDER ||
                    notification.category == Notification.CATEGORY_EVENT ||
                    notification.category == Notification.CATEGORY_RECOMMENDATION ||
                    notification.category == Notification.CATEGORY_WORKOUT
                if (!isProgressNotification &&
                    !isUserVisibleOngoingCategory &&
                    mode != IslandMode.IncomingCall &&
                    mode != IslandMode.Music &&
                    mode != IslandMode.LiveActivity &&
                    mode != IslandMode.Navigation &&
                    mode != IslandMode.DownloadUpload &&
                    mode != IslandMode.Hotspot &&
                    mode != IslandMode.ScreenRecording &&
                    mode != IslandMode.Timer &&
                    mode != IslandMode.Stopwatch
                ) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Returns true if the package belongs to a third-party (user-installed) app, meaning it is
     * not a system-level package. Used to decide whether a group summary should be cancelled
     * from the system shade.
     */
    fun isThirdPartyApp(packageName: String, packageManager: PackageManager): Boolean {
        return !isSystemLevelPackage(packageName, packageManager)
    }

    /**
     * Returns true if the app package is eligible to be configured and shown in Smart Island.
     * System-level packages and sensitive system settings (like com.android.settings) return false.
     */
    fun isAppEligibleForIsland(packageName: String, packageManager: PackageManager): Boolean {
        if (packageName in SYSTEM_LEVEL_PACKAGES) return false
        return !isSystemLevelPackage(packageName, packageManager)
    }

    /**
     * Returns true if the package belongs to a recognized messaging or chat app.
     */
    fun isMessagingPackage(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        return pkg == "com.instagram.android" ||
            pkg == "com.whatsapp" ||
            pkg == "com.whatsapp.w4b" ||
            pkg == "org.telegram.messenger" ||
            pkg == "org.thunderdog.challegram" ||
            pkg == "com.viber.voip" ||
            pkg == "kik.android" ||
            pkg == "com.snapchat.android" ||
            pkg == "jp.naver.line.android" ||
            pkg == "com.discord" ||
            pkg == "com.skype.raider" ||
            pkg == "com.microsoft.teams" ||
            pkg == "com.facebook.orca" ||
            pkg == "com.facebook.mlite" ||
            pkg == "com.facebook.katana" ||
            pkg == "com.facebook.lite" ||
            pkg == "com.twitter.android" ||
            pkg == "com.zhiliaoapp.musically" ||
            pkg == "com.ss.android.ugc.trill" ||
            pkg == "com.reddit.frontpage" ||
            pkg == "com.slack" ||
            pkg == "com.google.android.apps.messaging" ||
            pkg == "com.android.mms" ||
            pkg == "com.google.android.talk" ||
            pkg == "com.signal.android" ||
            pkg == "org.thoughtcrime.securesms" ||
            pkg == "com.tencent.mm" ||
            pkg == "com.kakao.talk" ||
            pkg == "com.imo.android.imoim" ||
            pkg.startsWith("org.telegram")
    }

    /**
     * Returns true if this notification is from a messaging/chat app.
     *
     * Messaging notifications must NEVER be island-only — they should peek in the island
     * AND remain in the system shade so the user can fully access them (read long texts,
     * play voice/audio messages, use reply actions, etc.).
     *
     * Detection uses three independent signals (any one is sufficient):
     *  1. Standard Android notification template (MessagingStyle / InboxStyle)
     *  2. Standard Android notification category (CATEGORY_MESSAGE / CATEGORY_SOCIAL / CATEGORY_EMAIL)
     *  3. Known messaging app package name (for apps that skip standard categories)
     */
    fun isMessagingNotification(notification: Notification, packageName: String): Boolean {
        val template = notification.extras?.getString(Notification.EXTRA_TEMPLATE)
        if (template == "android.app.Notification\$MessagingStyle" ||
            template == "android.app.Notification\$InboxStyle") return true
        if (notification.category == Notification.CATEGORY_MESSAGE ||
            notification.category == Notification.CATEGORY_SOCIAL ||
            notification.category == Notification.CATEGORY_EMAIL) return true
        return isMessagingPackage(packageName)
    }

    /**
     * Returns true ONLY if this notification is an authentic conversation / direct chat message.
     * Used specifically to determine whether a Reply button and in-island conversation bubbles
     * should be shown.
     *
     * Non-conversations (emails, YouTube/Reddit comments, social feed alerts, OTPs, promotional alerts)
     * are strictly rejected so that the Reply button and chat view are NEVER shown for them.
     */
    fun isAuthenticConversation(
        notification: Notification,
        packageName: String,
        shortcutId: String? = null,
        channelIsConversation: Boolean? = null
    ): Boolean {
        // Downloads, progress bars, and transfers are never conversations
        val progressMax = notification.extras?.getInt(Notification.EXTRA_PROGRESS_MAX, 0) ?: 0
        if (progressMax > 0) return false

        // System packages are never conversations
        if (packageName == "android" || packageName.startsWith("com.android.systemui")) return false

        val pkg = packageName.lowercase()

        // 1. Check Android official MessagingStyle
        val template = notification.extras?.getString(Notification.EXTRA_TEMPLATE)
        val hasMessagingTemplate = template == "android.app.Notification\$MessagingStyle" ||
            template == "androidx.core.app.NotificationCompat\$MessagingStyle"
        val hasMessagingExtras = notification.extras?.containsKey(Notification.EXTRA_MESSAGES) == true ||
            notification.extras?.containsKey("android.messagingStyleUser") == true ||
            runCatching { notification.extras?.getParcelableArray(Notification.EXTRA_MESSAGES) }.getOrNull() != null

        if (hasMessagingTemplate || hasMessagingExtras) {
            // MessagingStyle is Android's standard for conversations (used by WhatsApp, Telegram, Signal, Instagram DMs, Discord).
            // Exclude explicit email apps which sometimes wrap emails in styles
            if (!pkg.contains("mail") && !pkg.contains("outlook")) {
                return true
            }
        }

        // 2. Explicit non-conversation apps: email, video, forums, social feeds, stores, browsers
        val isExplicitNonChatApp = pkg == "com.google.android.gm" ||
            pkg.startsWith("com.google.android.youtube") ||
            pkg == "com.reddit.frontpage" ||
            pkg == "com.twitter.android" ||
            pkg == "com.facebook.katana" ||
            pkg == "com.facebook.lite" ||
            pkg == "com.zhiliaoapp.musically" ||
            pkg == "com.ss.android.ugc.trill" ||
            pkg == "com.linkedin.android" ||
            pkg == "com.android.vending" ||
            pkg == "com.android.chrome" ||
            pkg.startsWith("com.amazon.") ||
            pkg == "com.spotify.music" ||
            pkg == "com.netflix.mediaclient"

        if (isExplicitNonChatApp) return false

        // 3. Android 11+ (API 30+) official conversation space
        if (channelIsConversation == true) return true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !shortcutId.isNullOrBlank()) {
            if (isDedicatedChatApp(pkg)) return true
        }

        // 4. Dedicated instant messaging and SMS apps
        if (isDedicatedChatApp(pkg)) {
            // Service / status notifications are not conversations
            if (notification.category == Notification.CATEGORY_STATUS ||
                notification.category == Notification.CATEGORY_SERVICE ||
                notification.category == Notification.CATEGORY_SYSTEM
            ) {
                return false
            }

            val title = notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            if (title.isBlank() && text.isBlank()) return false

            val combined = "$title $text".lowercase()
            if (isMessageSyncNotification(combined) ||
                combined.contains("web is currently active") ||
                combined.contains("backup in progress") ||
                combined.contains("checking for new messages")
            ) {
                return false
            }

            // Exclude OTPs, 2FA codes, transactional alerts in SMS apps
            if (isSmsApp(pkg) && isOtpOrTransactionalSms(combined)) {
                return false
            }

            return true
        }

        return false
    }

    fun isDedicatedChatApp(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        return pkg == "com.google.android.apps.messaging" ||
            pkg == "com.android.mms" ||
            pkg == "com.samsung.android.messaging" ||
            pkg == "com.whatsapp" ||
            pkg == "com.whatsapp.w4b" ||
            pkg == "org.telegram.messenger" ||
            pkg == "org.thunderdog.challegram" ||
            pkg.startsWith("org.telegram") ||
            pkg == "com.signal.android" ||
            pkg == "org.thoughtcrime.securesms" ||
            pkg == "com.facebook.orca" ||
            pkg == "com.facebook.mlite" ||
            pkg == "com.viber.voip" ||
            pkg == "jp.naver.line.android" ||
            pkg == "com.tencent.mm" ||
            pkg == "com.kakao.talk" ||
            pkg == "com.skype.raider" ||
            pkg == "kik.android" ||
            pkg == "com.snapchat.android" ||
            pkg == "im.vector.app" ||
            pkg == "com.threema.app" ||
            pkg == "org.wire" ||
            pkg == "org.session.aaa"
    }

    private fun isSmsApp(packageName: String): Boolean {
        val pkg = packageName.lowercase()
        return pkg == "com.google.android.apps.messaging" ||
            pkg == "com.android.mms" ||
            pkg == "com.samsung.android.messaging"
    }

    fun isOtpOrTransactionalSms(text: String): Boolean {
        val lower = text.lowercase()
        val otpKeywords = OTP_KEYWORDS
        return otpKeywords.any { lower.contains(it) }
    }

    fun isAuthenticChatMessage(notification: Notification, packageName: String): Boolean {
        return isAuthenticConversation(notification, packageName)
    }

    private fun isSystemLevelCategory(
        notification: Notification,
        packageName: String,
        packageManager: PackageManager
    ): Boolean {
        if (notification.category == Notification.CATEGORY_SYSTEM) return true
        if (notification.category == Notification.CATEGORY_STATUS ||
            notification.category == Notification.CATEGORY_SERVICE ||
            notification.category == Notification.CATEGORY_ERROR
        ) {
            return isSystemLevelPackage(packageName, packageManager)
        }
        return false
    }

    private fun isSystemLevelPackage(packageName: String, packageManager: PackageManager): Boolean {
        if (packageName in SYSTEM_LEVEL_PACKAGES) return true

        // User-facing apps (Chrome, DownloadManager, Play Store) are not internal OS components
        if (packageName == "com.android.chrome" ||
            packageName == "com.android.providers.downloads" ||
            packageName == "com.android.vending"
        ) {
            return false
        }

        val flags = runCatchingLogged("NotificationFilter", "Failed to get flags for package $packageName") {
            packageManager.getApplicationInfo(packageName, 0).flags
        } ?: 0
        val isSystemFlag = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
        if (!isSystemFlag) return false

        // System-level packages are internal OS frameworks & UI, not browser or user apps
        return packageName == "android" ||
            packageName.startsWith("com.android.systemui") ||
            packageName.startsWith("com.android.settings") ||
            packageName.startsWith("com.android.keyguard") ||
            packageName.startsWith("com.android.permissioncontroller") ||
            packageName.startsWith("com.android.shell")
    }
}

fun Notification.toIslandMode(
    sbn: StatusBarNotification? = null,
    liveActivitiesEnabled: Boolean = true,
    navigationEnabled: Boolean = true,
    deviceType: String? = null
): IslandMode {
    val packageName = sbn?.packageName.orEmpty()
    val effectiveDevice = OemDeviceRules.resolveEffectiveDevice(deviceType)

    val titleExtracted = NotificationContentExtractor.extractTitle(extras, "")
    val (textExtracted, _) = NotificationContentExtractor.extractText(extras, this)
    val titleText = "$titleExtracted $textExtracted".lowercase()


    // 0. Incoming & Ongoing Phone Calls (Evaluated first so VoIP calls from WhatsApp/Instagram are recognized)
    val isCallStyle = extras?.getString(Notification.EXTRA_TEMPLATE) == "android.app.Notification\$CallStyle"
    val actionLabels = actions.orEmpty().map { it.title?.toString()?.lowercase().orEmpty() }
    val hasIncomingCallActionPair =
        actionLabels.any { it.contains("answer") || it.contains("accept") || it.contains("take") || it.contains("receive") } &&
            actionLabels.any {
                it.contains("decline") ||
                    it.contains("reject") ||
                    it.contains("hang up") ||
                    it.contains("dismiss")
            }
    val isInCallApp = OemDeviceRules.isInCallPackage(packageName, effectiveDevice)
    val hasCallAction = actionLabels.any {
        it.contains("answer") || it.contains("accept") || it.contains("take") ||
            it.contains("decline") || it.contains("reject") || it.contains("hang up") ||
            it.contains("end call") || it.contains("cancel") || it.contains("mute") ||
            it.contains("speaker")
    }
    val isOngoingCall = (flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE)) != 0
    val hasCallKeyword = CALL_KEYWORDS
        .any { titleText.contains(it) }
    val isInCallEvent = isInCallApp && (hasCallAction || hasCallKeyword || isOngoingCall)
    val isCallEvent = (category == Notification.CATEGORY_CALL || isCallStyle || hasIncomingCallActionPair || isInCallEvent) && !isCallEnded()
    if (isCallEvent) {
        return IslandMode.IncomingCall
    }

    // 1. Turn-by-Turn Map Navigation
    if (navigationEnabled && sbn != null) {
        if (NavigationParser.parse(sbn) != null) {
            return IslandMode.Navigation
        }
    }

    // 2. Live Activities (Food delivery, ride-hailing tracking: Swiggy, Zomato, Uber, etc.)
    if (liveActivitiesEnabled && sbn != null) {
        if (LiveActivityParser.parse(sbn) != null) {
            return IslandMode.LiveActivity
        }
    }

    // 3. Messaging & Social Notifications Protection
    // Chat messages (Instagram reels/DMs, WhatsApp, Telegram, Discord, Signal, SMS) must NEVER
    // be hijacked by Timer, Stopwatch, ScreenRecording, Hotspot, DownloadUpload, or Music.
    // However, active media/file transfers inside messaging apps with a progress bar should flow to DownloadUpload.
    val hasDownloadProgress = (extras?.getInt(Notification.EXTRA_PROGRESS_MAX, 0) ?: 0) > 0 &&
        !isMessageSyncNotification(titleText) &&
        (DOWNLOAD_PROGRESS_KEYWORDS
            .any { titleText.contains(it) })
    val isMessaging = !hasDownloadProgress && NotificationFilter.isMessagingNotification(this, packageName)
    if (isMessaging) {
        return IslandMode.Notification
    }

    // 4. Timer & Stopwatch (Dedicated Clock/Timer support for clock & utility apps)
    if (TimerStopwatchParser.isStopwatch(this, packageName)) {
        return IslandMode.Stopwatch
    }
    if (TimerStopwatchParser.isTimer(this, packageName)) {
        return IslandMode.Timer
    }

    // 5. Screen & Voice Recording (High priority, OEM-aware)
    val isRecordingPackageOrKeyword = OemDeviceRules.isScreenRecording(packageName, titleText, effectiveDevice)
    val isScreenRecordingActive = isRecordingPackageOrKeyword && !isScreenRecordingComplete()
    if (isScreenRecordingActive) {
        return IslandMode.ScreenRecording
    }

    // 6. Hotspot & Tethering status (OEM-aware)
    val isHotspot = OemDeviceRules.isHotspot(packageName, titleText)
    if (isHotspot) {
        return IslandMode.Hotspot
    }

    // 6. Downloads, Uploads & Progress Transfers (from Google Play Store and all apps)
    val isMessageSync = isMessageSyncNotification(titleText)
    val isProgressCategory = category == Notification.CATEGORY_PROGRESS
    val progressMax = extras?.getInt(Notification.EXTRA_PROGRESS_MAX, 0) ?: 0
    val isIndeterminate = extras?.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false) == true

    val isKnownDownloadPackage = packageName == "com.android.vending" ||
        packageName == "com.android.providers.downloads" ||
        packageName == "com.google.android.packageinstaller" ||
        packageName == "com.android.packageinstaller"

    val hasTimeRemainingKeyword = TIME_REMAINING_KEYWORDS.any { titleText.contains(it) }

    val hasTransferSpeedKeyword = TRANSFER_SPEED_KEYWORDS.any { titleText.contains(it) }

    val hasFileSizeKeyword = FILE_SIZE_KEYWORDS.any { titleText.contains(it) }

    val activeTransferKeywords = ACTIVE_TRANSFER_KEYWORDS.any { titleText.contains(it) }

    val genericTransferKeywords = GENERIC_TRANSFER_KEYWORDS.any { titleText.contains(it) }

    val hasTransferIndicators = activeTransferKeywords ||
        genericTransferKeywords ||
        hasTimeRemainingKeyword ||
        hasTransferSpeedKeyword ||
        hasFileSizeKeyword

    val isDownloadOrUpload = !isMessageSync && (
        isProgressCategory ||
        (isKnownDownloadPackage && (progressMax > 0 || isIndeterminate || hasTransferIndicators)) ||
        (progressMax > 0 && (hasTransferIndicators || isKnownDownloadPackage || !isMessaging)) ||
        (isIndeterminate && (activeTransferKeywords || isKnownDownloadPackage))
    )
    if (isDownloadOrUpload) {
        return IslandMode.DownloadUpload
    }

    // 7. Media & Music Playback
    // IMPORTANT: Messaging apps (Instagram, WhatsApp, Telegram, Signal, etc.) attach a
    // MediaSession token to voice/audio message notifications so Android can control in-app
    // playback. We must NOT classify these as Music — doing so causes the notification (and
    // its group summary) to be suppressed from the system shade, making the entire DM thread
    // invisible. We only classify a notification as Music when it is genuinely a media player:
    //   • Its category is CATEGORY_TRANSPORT (the standard media transport category), OR
    //   • It has a media session AND is NOT a message/messaging notification.
    val hasMediaSession = extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true
    val isMessagingNotification = NotificationFilter.isMessagingNotification(this, packageName)
    if (!isMessagingNotification && (category == Notification.CATEGORY_TRANSPORT || hasMediaSession)) {
        return IslandMode.Music
    }

    return IslandMode.Notification
}

fun Notification.isDownloadComplete(): Boolean {
    val extras = extras ?: return false
    val progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
    val progressCurrent = extras.getInt(Notification.EXTRA_PROGRESS, 0)
    val isIndeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)

    if (progressMax > 0 && progressCurrent >= progressMax) return true

    val titleText = "${extras.getCharSequence(Notification.EXTRA_TITLE)} ${extras.getCharSequence(Notification.EXTRA_TEXT)} ${extras.getCharSequence(Notification.EXTRA_BIG_TEXT)}".lowercase()
    if (COMPLETION_KEYWORDS.any { titleText.contains(it) }) return true

    // NOTE: Do NOT add a fallback here that returns true when progressMax == 0 && !isIndeterminate.
    // That condition matches essentially EVERY regular notification (which has no progress bar at all),
    // causing isDownloadComplete() to misfire on non-download notifications and evict them prematurely.
    return false
}

fun Notification.isScreenRecordingComplete(): Boolean {
    val extras = extras ?: return false
    val titleText = "${extras.getCharSequence(Notification.EXTRA_TITLE)} ${extras.getCharSequence(Notification.EXTRA_TEXT)} ${extras.getCharSequence(Notification.EXTRA_BIG_TEXT)}".lowercase()
    return OemDeviceRules.isScreenRecordingComplete(this, titleText)
}

fun Notification.isCallEnded(): Boolean {
    val extras = extras ?: return false
    val titleText = "${extras.getCharSequence(Notification.EXTRA_TITLE)} ${extras.getCharSequence(Notification.EXTRA_TEXT)} ${extras.getCharSequence(Notification.EXTRA_BIG_TEXT)}".lowercase()
    val isOngoing = (flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE)) != 0

    // Ringing calls with answer action are never ended
    val actionLabels = actions.orEmpty().map { it.title?.toString()?.lowercase().orEmpty() }
    val hasAnswerAction = actionLabels.any {
        it.contains("answer") || it.contains("accept") || it.contains("take") || it.contains("receive")
    }
    if (hasAnswerAction) return false

    if (CALL_ENDED_KEYWORDS.any { titleText.contains(it) }) return true
    if (!isOngoing && category == Notification.CATEGORY_CALL) return true

    return false
}

fun isMessageSyncNotification(text: String): Boolean {
    return SYNC_PHRASES.any { text.contains(it) }
}
