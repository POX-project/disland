/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.data

data class SmartIslandSettings(
    val enabled: Boolean = false,
    val width: Float = 112f,
    val height: Float = 34f,
    val xOffset: Float = 0f,
    val yOffset: Float = 12f,
    val cornerRadius: Float = 22f,
    val opacity: Float = 1f,
    val batteryColor: Long = 0xFF10B981L,
    val notificationDotColor: Long = 0xFF2563EBL,
    val musicVisualizerColor: Long = 0xFFFF6B9AL,
    val hotspotColor: Long = 0xFFF59E0BL,
    val callColor: Long = 0xFF22C55EL,
    val liveActivityColor: Long = 0xFF8B5CF6L,
    val transferColor: Long = 0xFF06B6D4L,
    val navigationColor: Long = 0xFF10B981L,
    val bluetoothColor: Long = 0xFF2563EBL,
    val flashlightColor: Long = 0xFFF59E0BL,
    val screenRecordingColor: Long = 0xFFEF4444L,
    val timerColor: Long = 0xFFF59E0BL,
    val stopwatchColor: Long = 0xFF06B6D4L,
    val shortcutPackages: Set<String> = emptySet(),
    val showRecentApps: Boolean = false,
    val welcomeDialogShown: Boolean = false,
    val showOnLockScreen: Boolean = false,
    val lockScreenPrivacy: String = "AppIconOnly",
    val showNotificationActions: Boolean = true,
    val hideFromNotificationShade: Boolean = true,
    val liveActivitiesEnabled: Boolean = true,
    val navigationEnabled: Boolean = true,
    val disabledNotificationPackages: Set<String> = emptySet(),
    val disabledSoundPackages: Set<String> = emptySet(),
    val hideWhenIdle: Boolean = false,
    val autoHidePill: Boolean = false,
    val autoHideTimeoutSeconds: Int = 5,
    val showInLandscape: Boolean = false,
    val autoExpandOnNotification: Boolean = true,
    val enableShadow: Boolean = true,
    val enableMusicArtworkBackground: Boolean = true,
    val deviceType: String = "AUTO",
    val allowNetworkChecks: Boolean = false,
    val enableNotificationHistory: Boolean = false,
    val notificationHistoryRetentionHours: Int = 72,
    val pinchNotchEnabled: Boolean = false,
    val animationDelayMs: Int = 0,
    val contentHorizontalPadding: Float = 12f,
    val contentVerticalPadding: Float = 0f,
    val animationSpringStiffness: Float = 460f,
    val animationSpringDamping: Float = 0.72f,
    val shadowElevation: Float = 14f,
    val shadowAlpha: Float = 0.8f,
    val shadowColor: Long = 0xFF000000L,
    val borderWidth: Float = 0f,
    val borderAlpha: Float = 0.20f,
    val borderColor: Long = 0xFFFFFFFFL,
    val innerShadowRadius: Float = 0f,
    val innerShadowAlpha: Float = 0f,
    val innerShadowColor: Long = 0xFFFFFFFFL,
    val backgroundBlur: Float = 0f,
    val islandBackgroundColor: Long = 0xFF000000L,
    val pinchCenterClearance: Float = 36f,
    val splitIslandEnabled: Boolean = false,
    val expandedScreenMargin: Float = 12f,
    val expandedTopMargin: Float = 10f,
    val expandedContentHorizontalPadding: Float = 18f,
    val expandedContentVerticalPadding: Float = 18f,
    val showLyricsUnderControls: Boolean = true
) {
    companion object {
        val Default = SmartIslandSettings()

        const val MIN_WIDTH = 60f
        const val MAX_WIDTH = 280f
        const val MIN_HEIGHT = 24f
        const val MAX_HEIGHT = 60f
        const val MIN_X_OFFSET = -140f
        const val MAX_X_OFFSET = 140f
        const val MIN_Y_OFFSET = -20f
        const val MAX_Y_OFFSET = 80f
        const val MIN_CORNER_RADIUS = 0f
        const val MAX_CORNER_RADIUS = 40f
        const val MIN_OPACITY = 0.2f
        const val MAX_OPACITY = 1f
        const val MIN_ANIMATION_DELAY_MS = 0
        const val MAX_ANIMATION_DELAY_MS = 600
        const val DEFAULT_ANIMATION_DELAY_MS = 150
        const val MIN_CONTENT_PADDING = 0f
        const val MAX_CONTENT_PADDING = 40f
        const val DEFAULT_CONTENT_PADDING = 12f
        const val MIN_VERTICAL_PADDING = -16f
        const val MAX_VERTICAL_PADDING = 20f
        const val MIN_SPRING_STIFFNESS = 200f
        const val MAX_SPRING_STIFFNESS = 900f
        const val DEFAULT_SPRING_STIFFNESS = 520f
        const val MIN_SPRING_DAMPING = 0.5f
        const val MAX_SPRING_DAMPING = 1.0f
        const val DEFAULT_SPRING_DAMPING = 0.72f
        const val MIN_SHADOW_ELEVATION = 0f
        const val MAX_SHADOW_ELEVATION = 40f
        const val DEFAULT_SHADOW_ELEVATION = 14f
        const val MIN_SHADOW_ALPHA = 0.05f
        const val MAX_SHADOW_ALPHA = 1f
        const val DEFAULT_SHADOW_ALPHA = 0.8f
        const val MIN_BORDER_WIDTH = 0f
        const val MAX_BORDER_WIDTH = 4f
        const val DEFAULT_BORDER_WIDTH = 0f
        const val MIN_BORDER_ALPHA = 0.05f
        const val MAX_BORDER_ALPHA = 1f
        const val DEFAULT_BORDER_ALPHA = 0.20f
        const val MIN_INNER_SHADOW_RADIUS = 0f
        const val MAX_INNER_SHADOW_RADIUS = 20f
        const val DEFAULT_INNER_SHADOW_RADIUS = 0f
        const val MIN_INNER_SHADOW_ALPHA = 0f
        const val MAX_INNER_SHADOW_ALPHA = 1f
        const val DEFAULT_INNER_SHADOW_ALPHA = 0f
        const val MIN_BACKGROUND_BLUR = 0f
        const val MAX_BACKGROUND_BLUR = 30f
        const val DEFAULT_BACKGROUND_BLUR = 0f
        const val MIN_PINCH_CENTER_CLEARANCE = 20f
        const val MAX_PINCH_CENTER_CLEARANCE = 70f
        const val DEFAULT_PINCH_CENTER_CLEARANCE = 36f

        const val MIN_EXPANDED_SCREEN_MARGIN = 0f
        const val MAX_EXPANDED_SCREEN_MARGIN = 48f
        const val DEFAULT_EXPANDED_SCREEN_MARGIN = 12f

        const val MIN_EXPANDED_TOP_MARGIN = 0f
        const val MAX_EXPANDED_TOP_MARGIN = 60f
        const val DEFAULT_EXPANDED_TOP_MARGIN = 10f

        const val MIN_EXPANDED_CONTENT_PADDING = 6f
        const val MAX_EXPANDED_CONTENT_PADDING = 48f
        const val DEFAULT_EXPANDED_CONTENT_H_PADDING = 18f
        const val DEFAULT_EXPANDED_CONTENT_V_PADDING = 18f

        fun fromJson(json: org.json.JSONObject): SmartIslandSettings {
            val def = Default
            return SmartIslandSettings(
                enabled = json.optBoolean("enabled", def.enabled),
                width = json.optDouble("width", def.width.toDouble()).toFloat(),
                height = json.optDouble("height", def.height.toDouble()).toFloat(),
                xOffset = json.optDouble("xOffset", def.xOffset.toDouble()).toFloat(),
                yOffset = json.optDouble("yOffset", def.yOffset.toDouble()).toFloat(),
                cornerRadius = json.optDouble("cornerRadius", def.cornerRadius.toDouble()).toFloat(),
                opacity = json.optDouble("opacity", def.opacity.toDouble()).toFloat(),
                batteryColor = json.optLong("batteryColor", def.batteryColor),
                notificationDotColor = json.optLong("notificationDotColor", def.notificationDotColor),
                musicVisualizerColor = json.optLong("musicVisualizerColor", def.musicVisualizerColor),
                hotspotColor = json.optLong("hotspotColor", def.hotspotColor),
                callColor = json.optLong("callColor", def.callColor),
                liveActivityColor = json.optLong("liveActivityColor", def.liveActivityColor),
                transferColor = json.optLong("transferColor", def.transferColor),
                navigationColor = json.optLong("navigationColor", def.navigationColor),
                bluetoothColor = json.optLong("bluetoothColor", def.bluetoothColor),
                flashlightColor = json.optLong("flashlightColor", def.flashlightColor),
                screenRecordingColor = json.optLong("screenRecordingColor", def.screenRecordingColor),
                timerColor = json.optLong("timerColor", def.timerColor),
                stopwatchColor = json.optLong("stopwatchColor", def.stopwatchColor),
                showRecentApps = json.optBoolean("showRecentApps", def.showRecentApps),
                welcomeDialogShown = json.optBoolean("welcomeDialogShown", def.welcomeDialogShown),
                showOnLockScreen = json.optBoolean("showOnLockScreen", def.showOnLockScreen),
                lockScreenPrivacy = json.optString("lockScreenPrivacy", def.lockScreenPrivacy),
                showNotificationActions = json.optBoolean("showNotificationActions", def.showNotificationActions),
                hideFromNotificationShade = json.optBoolean("hideFromNotificationShade", def.hideFromNotificationShade),
                liveActivitiesEnabled = json.optBoolean("liveActivitiesEnabled", def.liveActivitiesEnabled),
                navigationEnabled = json.optBoolean("navigationEnabled", def.navigationEnabled),
                hideWhenIdle = json.optBoolean("hideWhenIdle", def.hideWhenIdle),
                autoHidePill = json.optBoolean("autoHidePill", def.autoHidePill),
                autoHideTimeoutSeconds = json.optInt("autoHideTimeoutSeconds", def.autoHideTimeoutSeconds),
                showInLandscape = json.optBoolean("showInLandscape", def.showInLandscape),
                autoExpandOnNotification = json.optBoolean("autoExpandOnNotification", def.autoExpandOnNotification),
                enableShadow = json.optBoolean("enableShadow", def.enableShadow),
                enableMusicArtworkBackground = json.optBoolean("enableMusicArtworkBackground", def.enableMusicArtworkBackground),
                deviceType = json.optString("deviceType", def.deviceType),
                allowNetworkChecks = json.optBoolean("allowNetworkChecks", def.allowNetworkChecks),
                enableNotificationHistory = json.optBoolean("enableNotificationHistory", def.enableNotificationHistory),
                notificationHistoryRetentionHours = json.optInt("notificationHistoryRetentionHours", def.notificationHistoryRetentionHours),
                pinchNotchEnabled = json.optBoolean("pinchNotchEnabled", def.pinchNotchEnabled),
                animationDelayMs = json.optInt("animationDelayMs", def.animationDelayMs),
                contentHorizontalPadding = json.optDouble("contentHorizontalPadding", def.contentHorizontalPadding.toDouble()).toFloat(),
                contentVerticalPadding = json.optDouble("contentVerticalPadding", def.contentVerticalPadding.toDouble()).toFloat(),
                animationSpringStiffness = json.optDouble("animationSpringStiffness", def.animationSpringStiffness.toDouble()).toFloat(),
                animationSpringDamping = json.optDouble("animationSpringDamping", def.animationSpringDamping.toDouble()).toFloat(),
                shadowElevation = json.optDouble("shadowElevation", def.shadowElevation.toDouble()).toFloat(),
                shadowAlpha = json.optDouble("shadowAlpha", def.shadowAlpha.toDouble()).toFloat(),
                shadowColor = json.optLong("shadowColor", def.shadowColor),
                borderWidth = json.optDouble("borderWidth", def.borderWidth.toDouble()).toFloat(),
                borderAlpha = json.optDouble("borderAlpha", def.borderAlpha.toDouble()).toFloat(),
                borderColor = json.optLong("borderColor", def.borderColor),
                innerShadowRadius = json.optDouble("innerShadowRadius", def.innerShadowRadius.toDouble()).toFloat(),
                innerShadowAlpha = json.optDouble("innerShadowAlpha", def.innerShadowAlpha.toDouble()).toFloat(),
                innerShadowColor = json.optLong("innerShadowColor", def.innerShadowColor),
                backgroundBlur = json.optDouble("backgroundBlur", def.backgroundBlur.toDouble()).toFloat(),
                islandBackgroundColor = json.optLong("islandBackgroundColor", def.islandBackgroundColor),
                pinchCenterClearance = json.optDouble("pinchCenterClearance", def.pinchCenterClearance.toDouble()).toFloat(),
                splitIslandEnabled = json.optBoolean("splitIslandEnabled", def.splitIslandEnabled),
                expandedScreenMargin = json.optDouble("expandedScreenMargin", def.expandedScreenMargin.toDouble()).toFloat(),
                expandedTopMargin = json.optDouble("expandedTopMargin", def.expandedTopMargin.toDouble()).toFloat(),
                expandedContentHorizontalPadding = json.optDouble("expandedContentHorizontalPadding", def.expandedContentHorizontalPadding.toDouble()).toFloat(),
                expandedContentVerticalPadding = json.optDouble("expandedContentVerticalPadding", def.expandedContentVerticalPadding.toDouble()).toFloat(),
                showLyricsUnderControls = json.optBoolean("showLyricsUnderControls", def.showLyricsUnderControls)
            )
        }
    }

    fun toJson(): org.json.JSONObject {
        return org.json.JSONObject().apply {
            put("enabled", enabled)
            put("width", width.toDouble())
            put("height", height.toDouble())
            put("xOffset", xOffset.toDouble())
            put("yOffset", yOffset.toDouble())
            put("cornerRadius", cornerRadius.toDouble())
            put("opacity", opacity.toDouble())
            put("batteryColor", batteryColor)
            put("notificationDotColor", notificationDotColor)
            put("musicVisualizerColor", musicVisualizerColor)
            put("hotspotColor", hotspotColor)
            put("callColor", callColor)
            put("liveActivityColor", liveActivityColor)
            put("transferColor", transferColor)
            put("navigationColor", navigationColor)
            put("bluetoothColor", bluetoothColor)
            put("flashlightColor", flashlightColor)
            put("screenRecordingColor", screenRecordingColor)
            put("timerColor", timerColor)
            put("stopwatchColor", stopwatchColor)
            put("showRecentApps", showRecentApps)
            put("welcomeDialogShown", welcomeDialogShown)
            put("showOnLockScreen", showOnLockScreen)
            put("lockScreenPrivacy", lockScreenPrivacy)
            put("showNotificationActions", showNotificationActions)
            put("hideFromNotificationShade", hideFromNotificationShade)
            put("liveActivitiesEnabled", liveActivitiesEnabled)
            put("navigationEnabled", navigationEnabled)
            put("hideWhenIdle", hideWhenIdle)
            put("autoHidePill", autoHidePill)
            put("autoHideTimeoutSeconds", autoHideTimeoutSeconds)
            put("showInLandscape", showInLandscape)
            put("autoExpandOnNotification", autoExpandOnNotification)
            put("enableShadow", enableShadow)
            put("enableMusicArtworkBackground", enableMusicArtworkBackground)
            put("deviceType", deviceType)
            put("allowNetworkChecks", allowNetworkChecks)
            put("enableNotificationHistory", enableNotificationHistory)
            put("notificationHistoryRetentionHours", notificationHistoryRetentionHours)
            put("pinchNotchEnabled", pinchNotchEnabled)
            put("animationDelayMs", animationDelayMs)
            put("contentHorizontalPadding", contentHorizontalPadding.toDouble())
            put("contentVerticalPadding", contentVerticalPadding.toDouble())
            put("animationSpringStiffness", animationSpringStiffness.toDouble())
            put("animationSpringDamping", animationSpringDamping.toDouble())
            put("shadowElevation", shadowElevation.toDouble())
            put("shadowAlpha", shadowAlpha.toDouble())
            put("shadowColor", shadowColor)
            put("borderWidth", borderWidth.toDouble())
            put("borderAlpha", borderAlpha.toDouble())
            put("borderColor", borderColor)
            put("innerShadowRadius", innerShadowRadius.toDouble())
            put("innerShadowAlpha", innerShadowAlpha.toDouble())
            put("innerShadowColor", innerShadowColor)
            put("backgroundBlur", backgroundBlur.toDouble())
            put("islandBackgroundColor", islandBackgroundColor)
            put("pinchCenterClearance", pinchCenterClearance.toDouble())
            put("splitIslandEnabled", splitIslandEnabled)
            put("expandedScreenMargin", expandedScreenMargin.toDouble())
            put("expandedTopMargin", expandedTopMargin.toDouble())
            put("expandedContentHorizontalPadding", expandedContentHorizontalPadding.toDouble())
            put("expandedContentVerticalPadding", expandedContentVerticalPadding.toDouble())
            put("showLyricsUnderControls", showLyricsUnderControls)
        }
    }
}
