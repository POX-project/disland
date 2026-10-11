/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.agupta07505.smartisland.model.IslandProfile
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.smartIslandDataStore by preferencesDataStore(
    name = "smart_island_settings",
    corruptionHandler = ReplaceFileCorruptionHandler {
        Log.e("SmartIslandSettings", "Settings were corrupted; restoring safe defaults")
        emptyPreferences()
    }
)

class SmartIslandSettingsRepository(private val context: Context) {
    private object Keys {
        val Enabled = booleanPreferencesKey("enabled")
        val Width = floatPreferencesKey("width")
        val Height = floatPreferencesKey("height")
        val XOffset = floatPreferencesKey("x_offset")
        val YOffset = floatPreferencesKey("y_offset")
        val CornerRadius = floatPreferencesKey("corner_radius")
        val Opacity = floatPreferencesKey("opacity")
        val BatteryColor = longPreferencesKey("battery_color")
        val NotificationDotColor = longPreferencesKey("notification_dot_color")
        val MusicVisualizerColor = longPreferencesKey("music_visualizer_color")
        val HotspotColor = longPreferencesKey("hotspot_color")
        val CallColor = longPreferencesKey("call_color")
        val LiveActivityColor = longPreferencesKey("live_activity_color")
        val TransferColor = longPreferencesKey("transfer_color")
        val NavigationColor = longPreferencesKey("navigation_color")
        val BluetoothColor = longPreferencesKey("bluetooth_color")
        val FlashlightColor = longPreferencesKey("flashlight_color")
        val ScreenRecordingColor = longPreferencesKey("screen_recording_color")
        val TimerColor = longPreferencesKey("timer_color")
        val StopwatchColor = longPreferencesKey("stopwatch_color")
        val ShortcutPackages = stringSetPreferencesKey("shortcut_packages")
        val ShowRecentApps = booleanPreferencesKey("show_recent_apps")
        val WelcomeDialogShown = booleanPreferencesKey("welcome_dialog_shown")
        val ShowOnLockScreen = booleanPreferencesKey("show_on_lock_screen")
        val LockScreenPrivacy = stringPreferencesKey("lock_screen_privacy")
        val ShowNotificationActions = booleanPreferencesKey("show_notification_actions")
        val HideFromNotificationShade = booleanPreferencesKey("hide_from_notification_shade")
        val LiveActivitiesEnabled = booleanPreferencesKey("live_activities_enabled")
        val NavigationEnabled = booleanPreferencesKey("navigation_enabled")
        val DisabledNotificationPackages = stringSetPreferencesKey("disabled_notification_packages")
        val DisabledSoundPackages = stringSetPreferencesKey("disabled_sound_packages")
        val HideWhenIdle = booleanPreferencesKey("hide_when_idle")
        val AutoHidePill = booleanPreferencesKey("auto_hide_pill")
        val AutoHideTimeoutSeconds = intPreferencesKey("auto_hide_timeout_seconds")
        val ShowInLandscape = booleanPreferencesKey("show_in_landscape")
        val AutoExpandOnNotification = booleanPreferencesKey("auto_expand_on_notification")
        val EnableShadow = booleanPreferencesKey("enable_shadow")
        val EnableMusicArtworkBackground = booleanPreferencesKey("enable_music_artwork_background")
        val ShowLyricsUnderControls = booleanPreferencesKey("show_lyrics_under_controls")
        val DeviceType = stringPreferencesKey("device_type")
        val AllowNetworkChecks = booleanPreferencesKey("allow_network_checks")
        val EnableNotificationHistory = booleanPreferencesKey("enable_notification_history")
        val NotificationHistoryRetentionHours = intPreferencesKey("notification_history_retention_hours")
        val PinchNotchEnabled = booleanPreferencesKey("pinch_notch_enabled")
        val AnimationDelayMs = intPreferencesKey("animation_delay_ms")
        val ContentHorizontalPadding = floatPreferencesKey("content_horizontal_padding")
        val ContentVerticalPadding = floatPreferencesKey("content_vertical_padding")
        val AnimationSpringStiffness = floatPreferencesKey("animation_spring_stiffness")
        val AnimationSpringDamping = floatPreferencesKey("animation_spring_damping")
        val ShadowElevation = floatPreferencesKey("shadow_elevation")
        val ShadowAlpha = floatPreferencesKey("shadow_alpha")
        val ShadowColor = longPreferencesKey("shadow_color")
        val BorderWidth = floatPreferencesKey("border_width")
        val BorderAlpha = floatPreferencesKey("border_alpha")
        val BorderColor = longPreferencesKey("border_color")
        val InnerShadowRadius = floatPreferencesKey("inner_shadow_radius")
        val InnerShadowAlpha = floatPreferencesKey("inner_shadow_alpha")
        val InnerShadowColor = longPreferencesKey("inner_shadow_color")
        val BackgroundBlur = floatPreferencesKey("background_blur")
        val IslandBackgroundColor = longPreferencesKey("island_background_color")
        val PinchCenterClearance = floatPreferencesKey("pinch_center_clearance")
        val SplitIslandEnabled = booleanPreferencesKey("split_island_enabled")
        val ExpandedScreenMargin = floatPreferencesKey("expanded_screen_margin")
        val ExpandedTopMargin = floatPreferencesKey("expanded_top_margin")
        val ExpandedContentHorizontalPadding = floatPreferencesKey("expanded_content_horizontal_padding")
        val ExpandedContentVerticalPadding = floatPreferencesKey("expanded_content_vertical_padding")
    }

    private object TemplateKeys {
        val Saved = booleanPreferencesKey("template_saved")
        val Width = floatPreferencesKey("template_width")
        val Height = floatPreferencesKey("template_height")
        val XOffset = floatPreferencesKey("template_x_offset")
        val YOffset = floatPreferencesKey("template_y_offset")
        val CornerRadius = floatPreferencesKey("template_corner_radius")
        val Opacity = floatPreferencesKey("template_opacity")
        val PinchNotchEnabled = booleanPreferencesKey("template_pinch_notch_enabled")
        val AnimationSpringStiffness = floatPreferencesKey("template_animation_spring_stiffness")
        val AnimationSpringDamping = floatPreferencesKey("template_animation_spring_damping")
        val AnimationDelayMs = intPreferencesKey("template_animation_delay_ms")
        val ContentHorizontalPadding = floatPreferencesKey("template_content_horizontal_padding")
        val ContentVerticalPadding = floatPreferencesKey("template_content_vertical_padding")
        val ShadowElevation = floatPreferencesKey("template_shadow_elevation")
        val ShadowAlpha = floatPreferencesKey("template_shadow_alpha")
        val BorderWidth = floatPreferencesKey("template_border_width")
        val BorderAlpha = floatPreferencesKey("template_border_alpha")
        val InnerShadowRadius = floatPreferencesKey("template_inner_shadow_radius")
        val BackgroundBlur = floatPreferencesKey("template_background_blur")
        val PinchCenterClearance = floatPreferencesKey("template_pinch_center_clearance")
        val SplitIslandEnabled = booleanPreferencesKey("template_split_island_enabled")
        val ExpandedScreenMargin = floatPreferencesKey("template_expanded_screen_margin")
        val ExpandedTopMargin = floatPreferencesKey("template_expanded_top_margin")
        val ExpandedContentHPad = floatPreferencesKey("template_expanded_content_h_pad")
        val ExpandedContentVPad = floatPreferencesKey("template_expanded_content_v_pad")
    }

    private object ProfileKeys {
        val ProfilesJson = stringPreferencesKey("island_profiles_json")
        val ActiveProfileId = stringPreferencesKey("active_profile_id")
    }

    val settings: Flow<SmartIslandSettings> = context.smartIslandDataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e(TAG, "Unable to read settings; using safe defaults", error)
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { prefs ->
            val defaults = SmartIslandSettings.Default
            SmartIslandSettings(
                enabled = prefs[Keys.Enabled] ?: defaults.enabled,
                width = validDimension(
                    prefs[Keys.Width],
                    defaults.width,
                    SmartIslandSettings.MIN_WIDTH,
                    SmartIslandSettings.MAX_WIDTH
                ),
                height = validDimension(
                    prefs[Keys.Height],
                    defaults.height,
                    SmartIslandSettings.MIN_HEIGHT,
                    SmartIslandSettings.MAX_HEIGHT
                ),
                xOffset = validDimension(
                    prefs[Keys.XOffset],
                    defaults.xOffset,
                    SmartIslandSettings.MIN_X_OFFSET,
                    SmartIslandSettings.MAX_X_OFFSET
                ),
                yOffset = validDimension(
                    prefs[Keys.YOffset],
                    defaults.yOffset,
                    SmartIslandSettings.MIN_Y_OFFSET,
                    SmartIslandSettings.MAX_Y_OFFSET
                ),
                cornerRadius = validDimension(
                    prefs[Keys.CornerRadius],
                    defaults.cornerRadius,
                    SmartIslandSettings.MIN_CORNER_RADIUS,
                    SmartIslandSettings.MAX_CORNER_RADIUS
                ),
                opacity = validDimension(
                    prefs[Keys.Opacity],
                    defaults.opacity,
                    SmartIslandSettings.MIN_OPACITY,
                    SmartIslandSettings.MAX_OPACITY
                ),
                batteryColor = validColor(prefs[Keys.BatteryColor], defaults.batteryColor),
                notificationDotColor = validColor(
                    prefs[Keys.NotificationDotColor],
                    defaults.notificationDotColor
                ),
                musicVisualizerColor = validColor(
                    prefs[Keys.MusicVisualizerColor],
                    defaults.musicVisualizerColor
                ),
                hotspotColor = validColor(prefs[Keys.HotspotColor], defaults.hotspotColor),
                callColor = validColor(prefs[Keys.CallColor], defaults.callColor),
                liveActivityColor = validColor(prefs[Keys.LiveActivityColor], defaults.liveActivityColor),
                transferColor = validColor(prefs[Keys.TransferColor], defaults.transferColor),
                navigationColor = validColor(prefs[Keys.NavigationColor], defaults.navigationColor),
                bluetoothColor = validColor(prefs[Keys.BluetoothColor], defaults.bluetoothColor),
                flashlightColor = validColor(prefs[Keys.FlashlightColor], defaults.flashlightColor),
                screenRecordingColor = validColor(prefs[Keys.ScreenRecordingColor], defaults.screenRecordingColor),
                timerColor = validColor(prefs[Keys.TimerColor], defaults.timerColor),
                stopwatchColor = validColor(prefs[Keys.StopwatchColor], defaults.stopwatchColor),
                shortcutPackages = prefs[Keys.ShortcutPackages]
                    ?.asSequence()
                    ?.filter { it.isNotBlank() && it.length <= MAX_PACKAGE_NAME_LENGTH }
                    ?.take(MAX_SHORTCUTS)
                    ?.toSet()
                    ?: defaults.shortcutPackages,
                showRecentApps = prefs[Keys.ShowRecentApps] ?: defaults.showRecentApps,
                welcomeDialogShown = prefs[Keys.WelcomeDialogShown] ?: defaults.welcomeDialogShown,
                showOnLockScreen = prefs[Keys.ShowOnLockScreen] ?: defaults.showOnLockScreen,
                lockScreenPrivacy = prefs[Keys.LockScreenPrivacy]
                    ?.takeIf { it in VALID_LOCK_SCREEN_PRIVACY_VALUES }
                    ?: defaults.lockScreenPrivacy,
                showNotificationActions = prefs[Keys.ShowNotificationActions]
                    ?: defaults.showNotificationActions,
                hideFromNotificationShade = prefs[Keys.HideFromNotificationShade]
                    ?: defaults.hideFromNotificationShade,
                liveActivitiesEnabled = prefs[Keys.LiveActivitiesEnabled]
                    ?: defaults.liveActivitiesEnabled,
                navigationEnabled = prefs[Keys.NavigationEnabled]
                    ?: defaults.navigationEnabled,
                disabledNotificationPackages = prefs[Keys.DisabledNotificationPackages]
                    ?.asSequence()
                    ?.filter { it.isNotBlank() && it.length <= MAX_PACKAGE_NAME_LENGTH }
                    ?.toSet()
                    ?: defaults.disabledNotificationPackages,
                disabledSoundPackages = prefs[Keys.DisabledSoundPackages]
                    ?.asSequence()
                    ?.filter { pkg -> pkg.isNotBlank() && pkg.length <= MAX_PACKAGE_NAME_LENGTH }
                    ?.toSet()
                    ?: defaults.disabledSoundPackages,
                hideWhenIdle = prefs[Keys.HideWhenIdle] ?: defaults.hideWhenIdle,
                autoHidePill = prefs[Keys.AutoHidePill] ?: defaults.autoHidePill,
                autoHideTimeoutSeconds = prefs[Keys.AutoHideTimeoutSeconds] ?: defaults.autoHideTimeoutSeconds,
                showInLandscape = prefs[Keys.ShowInLandscape] ?: defaults.showInLandscape,
                autoExpandOnNotification = prefs[Keys.AutoExpandOnNotification] ?: defaults.autoExpandOnNotification,
                enableShadow = prefs[Keys.EnableShadow] ?: defaults.enableShadow,
                enableMusicArtworkBackground = prefs[Keys.EnableMusicArtworkBackground] ?: defaults.enableMusicArtworkBackground,
                showLyricsUnderControls = prefs[Keys.ShowLyricsUnderControls] ?: defaults.showLyricsUnderControls,
                deviceType = prefs[Keys.DeviceType] ?: defaults.deviceType,
                allowNetworkChecks = prefs[Keys.AllowNetworkChecks] ?: defaults.allowNetworkChecks,
                enableNotificationHistory = prefs[Keys.EnableNotificationHistory] ?: defaults.enableNotificationHistory,
                notificationHistoryRetentionHours = prefs[Keys.NotificationHistoryRetentionHours] ?: defaults.notificationHistoryRetentionHours,
                pinchNotchEnabled = prefs[Keys.PinchNotchEnabled] ?: defaults.pinchNotchEnabled,
                animationDelayMs = prefs[Keys.AnimationDelayMs]?.coerceIn(
                    SmartIslandSettings.MIN_ANIMATION_DELAY_MS,
                    SmartIslandSettings.MAX_ANIMATION_DELAY_MS
                ) ?: defaults.animationDelayMs,
                contentHorizontalPadding = validDimension(
                    prefs[Keys.ContentHorizontalPadding],
                    defaults.contentHorizontalPadding,
                    SmartIslandSettings.MIN_CONTENT_PADDING,
                    SmartIslandSettings.MAX_CONTENT_PADDING
                ),
                contentVerticalPadding = validDimension(
                    prefs[Keys.ContentVerticalPadding],
                    defaults.contentVerticalPadding,
                    SmartIslandSettings.MIN_VERTICAL_PADDING,
                    SmartIslandSettings.MAX_VERTICAL_PADDING
                ),
                animationSpringStiffness = validDimension(
                    prefs[Keys.AnimationSpringStiffness],
                    defaults.animationSpringStiffness,
                    SmartIslandSettings.MIN_SPRING_STIFFNESS,
                    SmartIslandSettings.MAX_SPRING_STIFFNESS
                ),
                animationSpringDamping = validDimension(
                    prefs[Keys.AnimationSpringDamping],
                    defaults.animationSpringDamping,
                    SmartIslandSettings.MIN_SPRING_DAMPING,
                    SmartIslandSettings.MAX_SPRING_DAMPING
                ),
                shadowElevation = validDimension(
                    prefs[Keys.ShadowElevation],
                    defaults.shadowElevation,
                    SmartIslandSettings.MIN_SHADOW_ELEVATION,
                    SmartIslandSettings.MAX_SHADOW_ELEVATION
                ),
                shadowAlpha = validDimension(
                    prefs[Keys.ShadowAlpha],
                    defaults.shadowAlpha,
                    SmartIslandSettings.MIN_SHADOW_ALPHA,
                    SmartIslandSettings.MAX_SHADOW_ALPHA
                ),
                shadowColor = validColor(prefs[Keys.ShadowColor], defaults.shadowColor),
                borderWidth = validDimension(
                    prefs[Keys.BorderWidth],
                    defaults.borderWidth,
                    SmartIslandSettings.MIN_BORDER_WIDTH,
                    SmartIslandSettings.MAX_BORDER_WIDTH
                ),
                borderAlpha = validDimension(
                    prefs[Keys.BorderAlpha],
                    defaults.borderAlpha,
                    SmartIslandSettings.MIN_BORDER_ALPHA,
                    SmartIslandSettings.MAX_BORDER_ALPHA
                ),
                borderColor = validColor(prefs[Keys.BorderColor], defaults.borderColor),
                innerShadowRadius = validDimension(
                    prefs[Keys.InnerShadowRadius],
                    defaults.innerShadowRadius,
                    SmartIslandSettings.MIN_INNER_SHADOW_RADIUS,
                    SmartIslandSettings.MAX_INNER_SHADOW_RADIUS
                ),
                innerShadowAlpha = validDimension(
                    prefs[Keys.InnerShadowAlpha],
                    defaults.innerShadowAlpha,
                    SmartIslandSettings.MIN_INNER_SHADOW_ALPHA,
                    SmartIslandSettings.MAX_INNER_SHADOW_ALPHA
                ),
                innerShadowColor = validColor(prefs[Keys.InnerShadowColor], defaults.innerShadowColor),
                backgroundBlur = validDimension(
                    prefs[Keys.BackgroundBlur],
                    defaults.backgroundBlur,
                    SmartIslandSettings.MIN_BACKGROUND_BLUR,
                    SmartIslandSettings.MAX_BACKGROUND_BLUR
                ),
                islandBackgroundColor = validColor(prefs[Keys.IslandBackgroundColor], defaults.islandBackgroundColor),
                pinchCenterClearance = validDimension(
                    prefs[Keys.PinchCenterClearance],
                    defaults.pinchCenterClearance,
                    SmartIslandSettings.MIN_PINCH_CENTER_CLEARANCE,
                    SmartIslandSettings.MAX_PINCH_CENTER_CLEARANCE
                ),
                splitIslandEnabled = prefs[Keys.SplitIslandEnabled] ?: defaults.splitIslandEnabled,
                expandedScreenMargin = validDimension(
                    prefs[Keys.ExpandedScreenMargin],
                    defaults.expandedScreenMargin,
                    SmartIslandSettings.MIN_EXPANDED_SCREEN_MARGIN,
                    SmartIslandSettings.MAX_EXPANDED_SCREEN_MARGIN
                ),
                expandedTopMargin = validDimension(
                    prefs[Keys.ExpandedTopMargin],
                    defaults.expandedTopMargin,
                    SmartIslandSettings.MIN_EXPANDED_TOP_MARGIN,
                    SmartIslandSettings.MAX_EXPANDED_TOP_MARGIN
                ),
                expandedContentHorizontalPadding = validDimension(
                    prefs[Keys.ExpandedContentHorizontalPadding],
                    defaults.expandedContentHorizontalPadding,
                    SmartIslandSettings.MIN_EXPANDED_CONTENT_PADDING,
                    SmartIslandSettings.MAX_EXPANDED_CONTENT_PADDING
                ),
                expandedContentVerticalPadding = validDimension(
                    prefs[Keys.ExpandedContentVerticalPadding],
                    defaults.expandedContentVerticalPadding,
                    SmartIslandSettings.MIN_EXPANDED_CONTENT_PADDING,
                    SmartIslandSettings.MAX_EXPANDED_CONTENT_PADDING
                )
            )
        }

    suspend fun setDeviceType(value: String) = editSafely { it[Keys.DeviceType] = value }
    suspend fun setEnabled(value: Boolean) = editSafely { it[Keys.Enabled] = value }
    suspend fun setEnableShadow(value: Boolean) = editSafely { it[Keys.EnableShadow] = value }
    suspend fun setEnableMusicArtworkBackground(value: Boolean) = editSafely { it[Keys.EnableMusicArtworkBackground] = value }
    suspend fun setShowLyricsUnderControls(value: Boolean) = editSafely { it[Keys.ShowLyricsUnderControls] = value }
    suspend fun setWidth(value: Float) = editSafely {
        it[Keys.Width] = validDimension(
            value,
            SmartIslandSettings.Default.width,
            SmartIslandSettings.MIN_WIDTH,
            SmartIslandSettings.MAX_WIDTH
        )
    }
    suspend fun setHeight(value: Float) = editSafely {
        it[Keys.Height] = validDimension(
            value,
            SmartIslandSettings.Default.height,
            SmartIslandSettings.MIN_HEIGHT,
            SmartIslandSettings.MAX_HEIGHT
        )
    }
    suspend fun setXOffset(value: Float) = editSafely {
        it[Keys.XOffset] = validDimension(
            value,
            SmartIslandSettings.Default.xOffset,
            SmartIslandSettings.MIN_X_OFFSET,
            SmartIslandSettings.MAX_X_OFFSET
        )
    }
    suspend fun setYOffset(value: Float) = editSafely {
        it[Keys.YOffset] = validDimension(
            value,
            SmartIslandSettings.Default.yOffset,
            SmartIslandSettings.MIN_Y_OFFSET,
            SmartIslandSettings.MAX_Y_OFFSET
        )
    }
    suspend fun setPosition(
        width: Float,
        height: Float,
        xOffset: Float,
        yOffset: Float
    ) = editSafely {
        it[Keys.Width] = validDimension(width, SmartIslandSettings.Default.width, SmartIslandSettings.MIN_WIDTH, SmartIslandSettings.MAX_WIDTH)
        it[Keys.Height] = validDimension(height, SmartIslandSettings.Default.height, SmartIslandSettings.MIN_HEIGHT, SmartIslandSettings.MAX_HEIGHT)
        it[Keys.XOffset] = validDimension(xOffset, SmartIslandSettings.Default.xOffset, SmartIslandSettings.MIN_X_OFFSET, SmartIslandSettings.MAX_X_OFFSET)
        it[Keys.YOffset] = validDimension(yOffset, SmartIslandSettings.Default.yOffset, SmartIslandSettings.MIN_Y_OFFSET, SmartIslandSettings.MAX_Y_OFFSET)
    }
    suspend fun setCornerRadius(value: Float) = editSafely {
        it[Keys.CornerRadius] = validDimension(
            value,
            SmartIslandSettings.Default.cornerRadius,
            SmartIslandSettings.MIN_CORNER_RADIUS,
            SmartIslandSettings.MAX_CORNER_RADIUS
        )
    }
    suspend fun setOpacity(value: Float) = editSafely {
        it[Keys.Opacity] = validDimension(
            value,
            SmartIslandSettings.Default.opacity,
            SmartIslandSettings.MIN_OPACITY,
            SmartIslandSettings.MAX_OPACITY
        )
    }
    suspend fun setBatteryColor(value: Long) = editSafely {
        it[Keys.BatteryColor] = validColor(value, SmartIslandSettings.Default.batteryColor)
    }
    suspend fun setNotificationDotColor(value: Long) = editSafely {
        it[Keys.NotificationDotColor] = validColor(
            value,
            SmartIslandSettings.Default.notificationDotColor
        )
    }
    suspend fun setMusicVisualizerColor(value: Long) = editSafely {
        it[Keys.MusicVisualizerColor] = validColor(
            value,
            SmartIslandSettings.Default.musicVisualizerColor
        )
    }
    suspend fun setHotspotColor(value: Long) = editSafely {
        it[Keys.HotspotColor] = validColor(value, SmartIslandSettings.Default.hotspotColor)
    }
    suspend fun setCallColor(value: Long) = editSafely {
        it[Keys.CallColor] = validColor(value, SmartIslandSettings.Default.callColor)
    }
    suspend fun setLiveActivityColor(value: Long) = editSafely {
        it[Keys.LiveActivityColor] = validColor(value, SmartIslandSettings.Default.liveActivityColor)
    }
    suspend fun setTransferColor(value: Long) = editSafely {
        it[Keys.TransferColor] = validColor(value, SmartIslandSettings.Default.transferColor)
    }
    suspend fun setNavigationColor(value: Long) = editSafely {
        it[Keys.NavigationColor] = validColor(value, SmartIslandSettings.Default.navigationColor)
    }
    suspend fun setBluetoothColor(value: Long) = editSafely {
        it[Keys.BluetoothColor] = validColor(value, SmartIslandSettings.Default.bluetoothColor)
    }
    suspend fun setFlashlightColor(value: Long) = editSafely {
        it[Keys.FlashlightColor] = validColor(value, SmartIslandSettings.Default.flashlightColor)
    }
    suspend fun setScreenRecordingColor(value: Long) = editSafely {
        it[Keys.ScreenRecordingColor] = validColor(value, SmartIslandSettings.Default.screenRecordingColor)
    }
    suspend fun setTimerColor(value: Long) = editSafely {
        it[Keys.TimerColor] = validColor(value, SmartIslandSettings.Default.timerColor)
    }
    suspend fun setStopwatchColor(value: Long) = editSafely {
        it[Keys.StopwatchColor] = validColor(value, SmartIslandSettings.Default.stopwatchColor)
    }
    suspend fun setShortcutPackages(value: Set<String>) = editSafely {
        it[Keys.ShortcutPackages] = value
            .asSequence()
            .filter { packageName ->
                packageName.isNotBlank() && packageName.length <= MAX_PACKAGE_NAME_LENGTH
            }
            .take(MAX_SHORTCUTS)
            .toSet()
    }
    suspend fun setShowRecentApps(value: Boolean) = editSafely {
        it[Keys.ShowRecentApps] = value
    }
    suspend fun setWelcomeDialogShown(value: Boolean) = editSafely {
        it[Keys.WelcomeDialogShown] = value
    }

    /**
     * Reads the REAL persisted value (awaits the first on-disk emission) instead of
     * relying on the SmartIslandSettings.Default placeholder that State flows emit on the
     * first frame. Used to decide whether the welcome dialog should show.
     */
    suspend fun isWelcomeDialogShown(): Boolean = settings.first().welcomeDialogShown

    suspend fun setShowOnLockScreen(value: Boolean) = editSafely {
        it[Keys.ShowOnLockScreen] = value
    }
    suspend fun setLockScreenPrivacy(value: String) = editSafely {
        it[Keys.LockScreenPrivacy] = value.takeIf { privacy ->
            privacy in VALID_LOCK_SCREEN_PRIVACY_VALUES
        } ?: SmartIslandSettings.Default.lockScreenPrivacy
    }
    suspend fun setShowNotificationActions(value: Boolean) = editSafely {
        it[Keys.ShowNotificationActions] = value
    }
    suspend fun setHideFromNotificationShade(value: Boolean) = editSafely {
        it[Keys.HideFromNotificationShade] = value
    }
    suspend fun setLiveActivitiesEnabled(value: Boolean) = editSafely {
        it[Keys.LiveActivitiesEnabled] = value
    }
    suspend fun setNavigationEnabled(value: Boolean) = editSafely {
        it[Keys.NavigationEnabled] = value
    }
    suspend fun setDisabledNotificationPackages(value: Set<String>) = editSafely {
        it[Keys.DisabledNotificationPackages] = value
            .asSequence()
            .filter { pkg -> pkg.isNotBlank() && pkg.length <= MAX_PACKAGE_NAME_LENGTH }
            .toSet()
    }
    suspend fun setDisabledSoundPackages(value: Set<String>) = editSafely {
        it[Keys.DisabledSoundPackages] = value
            .asSequence()
            .filter { pkg -> pkg.isNotBlank() && pkg.length <= MAX_PACKAGE_NAME_LENGTH }
            .toSet()
    }
    suspend fun setHideWhenIdle(value: Boolean) = editSafely {
        it[Keys.HideWhenIdle] = value
    }
    suspend fun setAutoHidePill(value: Boolean) = editSafely {
        it[Keys.AutoHidePill] = value
    }
    suspend fun setAutoHideTimeoutSeconds(value: Int) = editSafely {
        it[Keys.AutoHideTimeoutSeconds] = value.coerceIn(1, 120)
    }
    suspend fun setShowInLandscape(value: Boolean) = editSafely {
        it[Keys.ShowInLandscape] = value
    }
    suspend fun setAutoExpandOnNotification(value: Boolean) = editSafely {
        it[Keys.AutoExpandOnNotification] = value
    }
    suspend fun setAllowNetworkChecks(value: Boolean) = editSafely {
        it[Keys.AllowNetworkChecks] = value
    }
    suspend fun setEnableNotificationHistory(value: Boolean) = editSafely {
        it[Keys.EnableNotificationHistory] = value
    }
    suspend fun setNotificationHistoryRetentionHours(value: Int) = editSafely {
        it[Keys.NotificationHistoryRetentionHours] = value
    }

    suspend fun setPinchNotchEnabled(value: Boolean) = editSafely {
        it[Keys.PinchNotchEnabled] = value
    }

    suspend fun setAnimationDelay(value: Int) = editSafely {
        it[Keys.AnimationDelayMs] = value.coerceIn(
            SmartIslandSettings.MIN_ANIMATION_DELAY_MS,
            SmartIslandSettings.MAX_ANIMATION_DELAY_MS
        )
    }

    suspend fun setContentHorizontalPadding(value: Float) = editSafely {
        it[Keys.ContentHorizontalPadding] = validDimension(
            value,
            SmartIslandSettings.Default.contentHorizontalPadding,
            SmartIslandSettings.MIN_CONTENT_PADDING,
            SmartIslandSettings.MAX_CONTENT_PADDING
        )
    }

    suspend fun setContentVerticalPadding(value: Float) = editSafely {
        it[Keys.ContentVerticalPadding] = validDimension(
            value,
            SmartIslandSettings.Default.contentVerticalPadding,
            SmartIslandSettings.MIN_VERTICAL_PADDING,
            SmartIslandSettings.MAX_VERTICAL_PADDING
        )
    }

    suspend fun setAnimationSpringStiffness(value: Float) = editSafely {
        it[Keys.AnimationSpringStiffness] = validDimension(
            value,
            SmartIslandSettings.Default.animationSpringStiffness,
            SmartIslandSettings.MIN_SPRING_STIFFNESS,
            SmartIslandSettings.MAX_SPRING_STIFFNESS
        )
    }

    suspend fun setAnimationSpringDamping(value: Float) = editSafely {
        it[Keys.AnimationSpringDamping] = validDimension(
            value,
            SmartIslandSettings.Default.animationSpringDamping,
            SmartIslandSettings.MIN_SPRING_DAMPING,
            SmartIslandSettings.MAX_SPRING_DAMPING
        )
    }

    suspend fun setAnimationPreset(speed: String) = editSafely {
        when (speed.uppercase()) {
            "XIAOMI", "FAST" -> {
                it[Keys.AnimationSpringStiffness] = 580f
                it[Keys.AnimationSpringDamping] = 0.68f
                it[Keys.AnimationDelayMs] = 0
            }
            "GENTLE" -> {
                it[Keys.AnimationSpringStiffness] = 340f
                it[Keys.AnimationSpringDamping] = 0.82f
                it[Keys.AnimationDelayMs] = 50
            }
            else -> { // "APPLE", "BALANCED"
                it[Keys.AnimationSpringStiffness] = 460f
                it[Keys.AnimationSpringDamping] = 0.72f
                it[Keys.AnimationDelayMs] = 0
            }
        }
    }

    suspend fun resetPosition() = editSafely {
        it[Keys.Width] = SmartIslandSettings.Default.width
        it[Keys.Height] = SmartIslandSettings.Default.height
        it[Keys.XOffset] = SmartIslandSettings.Default.xOffset
        it[Keys.YOffset] = SmartIslandSettings.Default.yOffset
        it[Keys.CornerRadius] = SmartIslandSettings.Default.cornerRadius
        it[Keys.Opacity] = SmartIslandSettings.Default.opacity
        it[Keys.PinchNotchEnabled] = SmartIslandSettings.Default.pinchNotchEnabled
        it[Keys.ContentHorizontalPadding] = SmartIslandSettings.Default.contentHorizontalPadding
        it[Keys.ContentVerticalPadding] = SmartIslandSettings.Default.contentVerticalPadding
        it[Keys.PinchCenterClearance] = SmartIslandSettings.Default.pinchCenterClearance
        it[Keys.ExpandedScreenMargin] = SmartIslandSettings.Default.expandedScreenMargin
        it[Keys.ExpandedTopMargin] = SmartIslandSettings.Default.expandedTopMargin
        it[Keys.ExpandedContentHorizontalPadding] = SmartIslandSettings.Default.expandedContentHorizontalPadding
        it[Keys.ExpandedContentVerticalPadding] = SmartIslandSettings.Default.expandedContentVerticalPadding
        it[Keys.SplitIslandEnabled] = SmartIslandSettings.Default.splitIslandEnabled
    }

    suspend fun setShadowElevation(value: Float) = editSafely {
        it[Keys.ShadowElevation] = validDimension(
            value,
            SmartIslandSettings.Default.shadowElevation,
            SmartIslandSettings.MIN_SHADOW_ELEVATION,
            SmartIslandSettings.MAX_SHADOW_ELEVATION
        )
    }

    suspend fun setShadowAlpha(value: Float) = editSafely {
        it[Keys.ShadowAlpha] = validDimension(
            value,
            SmartIslandSettings.Default.shadowAlpha,
            SmartIslandSettings.MIN_SHADOW_ALPHA,
            SmartIslandSettings.MAX_SHADOW_ALPHA
        )
    }

    suspend fun setShadowColor(value: Long) = editSafely {
        it[Keys.ShadowColor] = value
    }

    suspend fun setBorderWidth(value: Float) = editSafely {
        it[Keys.BorderWidth] = validDimension(
            value,
            SmartIslandSettings.Default.borderWidth,
            SmartIslandSettings.MIN_BORDER_WIDTH,
            SmartIslandSettings.MAX_BORDER_WIDTH
        )
    }

    suspend fun setBorderAlpha(value: Float) = editSafely {
        it[Keys.BorderAlpha] = validDimension(
            value,
            SmartIslandSettings.Default.borderAlpha,
            SmartIslandSettings.MIN_BORDER_ALPHA,
            SmartIslandSettings.MAX_BORDER_ALPHA
        )
    }

    suspend fun setBorderColor(value: Long) = editSafely {
        it[Keys.BorderColor] = value
    }

    suspend fun setInnerShadowRadius(value: Float) = editSafely {
        it[Keys.InnerShadowRadius] = validDimension(
            value,
            SmartIslandSettings.Default.innerShadowRadius,
            SmartIslandSettings.MIN_INNER_SHADOW_RADIUS,
            SmartIslandSettings.MAX_INNER_SHADOW_RADIUS
        )
    }

    suspend fun setInnerShadowAlpha(value: Float) = editSafely {
        it[Keys.InnerShadowAlpha] = validDimension(
            value,
            SmartIslandSettings.Default.innerShadowAlpha,
            SmartIslandSettings.MIN_INNER_SHADOW_ALPHA,
            SmartIslandSettings.MAX_INNER_SHADOW_ALPHA
        )
    }

    suspend fun setInnerShadowColor(value: Long) = editSafely {
        it[Keys.InnerShadowColor] = value
    }

    suspend fun setBackgroundBlur(value: Float) = editSafely {
        it[Keys.BackgroundBlur] = validDimension(
            value,
            SmartIslandSettings.Default.backgroundBlur,
            SmartIslandSettings.MIN_BACKGROUND_BLUR,
            SmartIslandSettings.MAX_BACKGROUND_BLUR
        )
    }

    suspend fun setIslandBackgroundColor(value: Long) = editSafely {
        it[Keys.IslandBackgroundColor] = value
    }

    suspend fun setPinchCenterClearance(value: Float) = editSafely {
        it[Keys.PinchCenterClearance] = validDimension(
            value,
            SmartIslandSettings.Default.pinchCenterClearance,
            SmartIslandSettings.MIN_PINCH_CENTER_CLEARANCE,
            SmartIslandSettings.MAX_PINCH_CENTER_CLEARANCE
        )
    }

    suspend fun setVisualThemePreset(theme: String) = editSafely {
        when (theme.uppercase()) {
            "OLED_BLACK" -> {
                it[Keys.Opacity] = 1.0f
                it[Keys.EnableShadow] = true
                it[Keys.ShadowElevation] = 18f
                it[Keys.ShadowAlpha] = 0.9f
                it[Keys.ShadowColor] = 0xFF000000L
                it[Keys.BorderWidth] = 0f
                it[Keys.BorderAlpha] = 0.20f
                it[Keys.BorderColor] = 0xFFFFFFFFL
                it[Keys.InnerShadowRadius] = 0f
                it[Keys.InnerShadowAlpha] = 0f
                it[Keys.BackgroundBlur] = 0f
                it[Keys.IslandBackgroundColor] = 0xFF000000L
            }
            "APPLE_GLASS" -> {
                it[Keys.Opacity] = 0.88f
                it[Keys.EnableShadow] = true
                it[Keys.ShadowElevation] = 16f
                it[Keys.ShadowAlpha] = 0.75f
                it[Keys.ShadowColor] = 0xFF000000L
                it[Keys.BorderWidth] = 0.8f
                it[Keys.BorderAlpha] = 0.22f
                it[Keys.BorderColor] = 0xFFFFFFFFL
                it[Keys.InnerShadowRadius] = 4f
                it[Keys.InnerShadowAlpha] = 0.15f
                it[Keys.InnerShadowColor] = 0xFFFFFFFFL
                it[Keys.BackgroundBlur] = 14f
                it[Keys.IslandBackgroundColor] = 0xFF0B0E14L
            }
            "FROSTED_ACRYLIC" -> {
                it[Keys.Opacity] = 0.72f
                it[Keys.EnableShadow] = true
                it[Keys.ShadowElevation] = 10f
                it[Keys.ShadowAlpha] = 0.6f
                it[Keys.ShadowColor] = 0xFF000000L
                it[Keys.BorderWidth] = 1.2f
                it[Keys.BorderAlpha] = 0.35f
                it[Keys.BorderColor] = 0xFFFFFFFFL
                it[Keys.InnerShadowRadius] = 8f
                it[Keys.InnerShadowAlpha] = 0.25f
                it[Keys.InnerShadowColor] = 0xFFFFFFFFL
                it[Keys.BackgroundBlur] = 22f
                it[Keys.IslandBackgroundColor] = 0xFF18181BL
            }
            "CYBER_GLOW" -> {
                it[Keys.Opacity] = 0.92f
                it[Keys.EnableShadow] = true
                it[Keys.ShadowElevation] = 24f
                it[Keys.ShadowAlpha] = 0.85f
                it[Keys.ShadowColor] = 0xFF06B6D4L
                it[Keys.BorderWidth] = 1.4f
                it[Keys.BorderAlpha] = 0.65f
                it[Keys.BorderColor] = 0xFF38BDF8L
                it[Keys.InnerShadowRadius] = 6f
                it[Keys.InnerShadowAlpha] = 0.30f
                it[Keys.InnerShadowColor] = 0xFF38BDF8L
                it[Keys.BackgroundBlur] = 8f
                it[Keys.IslandBackgroundColor] = 0xFF050B14L
            }
            "NEUMORPHIC" -> {
                it[Keys.Opacity] = 0.95f
                it[Keys.EnableShadow] = true
                it[Keys.ShadowElevation] = 18f
                it[Keys.ShadowAlpha] = 0.7f
                it[Keys.ShadowColor] = 0xFF000000L
                it[Keys.BorderWidth] = 1.0f
                it[Keys.BorderAlpha] = 0.15f
                it[Keys.BorderColor] = 0xFFFFFFFFL
                it[Keys.InnerShadowRadius] = 10f
                it[Keys.InnerShadowAlpha] = 0.35f
                it[Keys.InnerShadowColor] = 0xFF000000L
                it[Keys.BackgroundBlur] = 0f
                it[Keys.IslandBackgroundColor] = 0xFF141417L
            }
        }
    }

    suspend fun resetVisualStyles() = editSafely {
        it[Keys.EnableShadow] = SmartIslandSettings.Default.enableShadow
        it[Keys.ShadowElevation] = SmartIslandSettings.Default.shadowElevation
        it[Keys.ShadowAlpha] = SmartIslandSettings.Default.shadowAlpha
        it[Keys.ShadowColor] = SmartIslandSettings.Default.shadowColor
        it[Keys.BorderWidth] = SmartIslandSettings.Default.borderWidth
        it[Keys.BorderAlpha] = SmartIslandSettings.Default.borderAlpha
        it[Keys.BorderColor] = SmartIslandSettings.Default.borderColor
        it[Keys.InnerShadowRadius] = SmartIslandSettings.Default.innerShadowRadius
        it[Keys.InnerShadowAlpha] = SmartIslandSettings.Default.innerShadowAlpha
        it[Keys.InnerShadowColor] = SmartIslandSettings.Default.innerShadowColor
        it[Keys.BackgroundBlur] = SmartIslandSettings.Default.backgroundBlur
        it[Keys.IslandBackgroundColor] = SmartIslandSettings.Default.islandBackgroundColor
    }

    suspend fun setSplitIslandEnabled(value: Boolean) = editSafely {
        it[Keys.SplitIslandEnabled] = value
    }

    suspend fun setExpandedScreenMargin(value: Float) = editSafely {
        it[Keys.ExpandedScreenMargin] = validDimension(
            value,
            SmartIslandSettings.Default.expandedScreenMargin,
            SmartIslandSettings.MIN_EXPANDED_SCREEN_MARGIN,
            SmartIslandSettings.MAX_EXPANDED_SCREEN_MARGIN
        )
    }

    suspend fun setExpandedTopMargin(value: Float) = editSafely {
        it[Keys.ExpandedTopMargin] = validDimension(
            value,
            SmartIslandSettings.Default.expandedTopMargin,
            SmartIslandSettings.MIN_EXPANDED_TOP_MARGIN,
            SmartIslandSettings.MAX_EXPANDED_TOP_MARGIN
        )
    }

    suspend fun setExpandedContentHorizontalPadding(value: Float) = editSafely {
        it[Keys.ExpandedContentHorizontalPadding] = validDimension(
            value,
            SmartIslandSettings.Default.expandedContentHorizontalPadding,
            SmartIslandSettings.MIN_EXPANDED_CONTENT_PADDING,
            SmartIslandSettings.MAX_EXPANDED_CONTENT_PADDING
        )
    }

    suspend fun setExpandedContentVerticalPadding(value: Float) = editSafely {
        it[Keys.ExpandedContentVerticalPadding] = validDimension(
            value,
            SmartIslandSettings.Default.expandedContentVerticalPadding,
            SmartIslandSettings.MIN_EXPANDED_CONTENT_PADDING,
            SmartIslandSettings.MAX_EXPANDED_CONTENT_PADDING
        )
    }

    val hasSavedTemplate: Flow<Boolean> = context.smartIslandDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[TemplateKeys.Saved] ?: false }

    suspend fun saveCurrentAsTemplate() = editSafely { prefs ->
        val current = settings.first()
        prefs[TemplateKeys.Saved] = true
        prefs[TemplateKeys.Width] = current.width
        prefs[TemplateKeys.Height] = current.height
        prefs[TemplateKeys.XOffset] = current.xOffset
        prefs[TemplateKeys.YOffset] = current.yOffset
        prefs[TemplateKeys.CornerRadius] = current.cornerRadius
        prefs[TemplateKeys.Opacity] = current.opacity
        prefs[TemplateKeys.PinchNotchEnabled] = current.pinchNotchEnabled
        prefs[TemplateKeys.AnimationSpringStiffness] = current.animationSpringStiffness
        prefs[TemplateKeys.AnimationSpringDamping] = current.animationSpringDamping
        prefs[TemplateKeys.AnimationDelayMs] = current.animationDelayMs
        prefs[TemplateKeys.ContentHorizontalPadding] = current.contentHorizontalPadding
        prefs[TemplateKeys.ContentVerticalPadding] = current.contentVerticalPadding
        prefs[TemplateKeys.ShadowElevation] = current.shadowElevation
        prefs[TemplateKeys.ShadowAlpha] = current.shadowAlpha
        prefs[TemplateKeys.BorderWidth] = current.borderWidth
        prefs[TemplateKeys.BorderAlpha] = current.borderAlpha
        prefs[TemplateKeys.InnerShadowRadius] = current.innerShadowRadius
        prefs[TemplateKeys.BackgroundBlur] = current.backgroundBlur
        prefs[TemplateKeys.PinchCenterClearance] = current.pinchCenterClearance
        prefs[TemplateKeys.SplitIslandEnabled] = current.splitIslandEnabled
        prefs[TemplateKeys.ExpandedScreenMargin] = current.expandedScreenMargin
        prefs[TemplateKeys.ExpandedTopMargin] = current.expandedTopMargin
        prefs[TemplateKeys.ExpandedContentHPad] = current.expandedContentHorizontalPadding
        prefs[TemplateKeys.ExpandedContentVPad] = current.expandedContentVerticalPadding
    }

    suspend fun applySavedTemplate() = editSafely { prefs ->
        if (prefs[TemplateKeys.Saved] != true) return@editSafely
        prefs[TemplateKeys.Width]?.let { prefs[Keys.Width] = it }
        prefs[TemplateKeys.Height]?.let { prefs[Keys.Height] = it }
        prefs[TemplateKeys.XOffset]?.let { prefs[Keys.XOffset] = it }
        prefs[TemplateKeys.YOffset]?.let { prefs[Keys.YOffset] = it }
        prefs[TemplateKeys.CornerRadius]?.let { prefs[Keys.CornerRadius] = it }
        prefs[TemplateKeys.Opacity]?.let { prefs[Keys.Opacity] = it }
        prefs[TemplateKeys.PinchNotchEnabled]?.let { prefs[Keys.PinchNotchEnabled] = it }
        prefs[TemplateKeys.AnimationSpringStiffness]?.let { prefs[Keys.AnimationSpringStiffness] = it }
        prefs[TemplateKeys.AnimationSpringDamping]?.let { prefs[Keys.AnimationSpringDamping] = it }
        prefs[TemplateKeys.AnimationDelayMs]?.let { prefs[Keys.AnimationDelayMs] = it }
        prefs[TemplateKeys.ContentHorizontalPadding]?.let { prefs[Keys.ContentHorizontalPadding] = it }
        prefs[TemplateKeys.ContentVerticalPadding]?.let { prefs[Keys.ContentVerticalPadding] = it }
        prefs[TemplateKeys.ShadowElevation]?.let { prefs[Keys.ShadowElevation] = it }
        prefs[TemplateKeys.ShadowAlpha]?.let { prefs[Keys.ShadowAlpha] = it }
        prefs[TemplateKeys.BorderWidth]?.let { prefs[Keys.BorderWidth] = it }
        prefs[TemplateKeys.BorderAlpha]?.let { prefs[Keys.BorderAlpha] = it }
        prefs[TemplateKeys.InnerShadowRadius]?.let { prefs[Keys.InnerShadowRadius] = it }
        prefs[TemplateKeys.BackgroundBlur]?.let { prefs[Keys.BackgroundBlur] = it }
        prefs[TemplateKeys.PinchCenterClearance]?.let { prefs[Keys.PinchCenterClearance] = it }
        prefs[TemplateKeys.SplitIslandEnabled]?.let { prefs[Keys.SplitIslandEnabled] = it }
        prefs[TemplateKeys.ExpandedScreenMargin]?.let { prefs[Keys.ExpandedScreenMargin] = it }
        prefs[TemplateKeys.ExpandedTopMargin]?.let { prefs[Keys.ExpandedTopMargin] = it }
        prefs[TemplateKeys.ExpandedContentHPad]?.let { prefs[Keys.ExpandedContentHorizontalPadding] = it }
        prefs[TemplateKeys.ExpandedContentVPad]?.let { prefs[Keys.ExpandedContentVerticalPadding] = it }
    }

    val profiles: Flow<List<IslandProfile>> = context.smartIslandDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val jsonStr = prefs[ProfileKeys.ProfilesJson]
            if (jsonStr.isNullOrBlank()) {
                IslandProfile.defaultProfiles()
            } else {
                try {
                    val array = JSONArray(jsonStr)
                    val list = mutableListOf<IslandProfile>()
                    for (i in 0 until array.length()) {
                        list.add(IslandProfile.fromJson(array.getJSONObject(i)))
                    }
                    if (list.isEmpty()) IslandProfile.defaultProfiles() else list
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing island profiles JSON", e)
                    IslandProfile.defaultProfiles()
                }
            }
        }

    val activeProfileId: Flow<String> = context.smartIslandDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[ProfileKeys.ActiveProfileId] ?: IslandProfile.DEFAULT_ID }

    val activeProfile: Flow<IslandProfile?> = combine(profiles, activeProfileId) { list, activeId ->
        list.find { it.id == activeId } ?: list.firstOrNull()
    }

    suspend fun createProfile(name: String, description: String = ""): IslandProfile {
        val currentSettings = settings.first()
        val trimmedName = name.trim().ifEmpty { "Custom Profile" }
        val newProfile = IslandProfile(
            name = trimmedName,
            description = description.trim(),
            isDefault = false,
            settings = currentSettings
        )
        val currentProfiles = profiles.first().toMutableList()
        currentProfiles.add(newProfile)

        val array = JSONArray()
        currentProfiles.forEach { array.put(it.toJson()) }

        editSafely { prefs ->
            prefs[ProfileKeys.ProfilesJson] = array.toString()
            prefs[ProfileKeys.ActiveProfileId] = newProfile.id
        }
        return newProfile
    }

    suspend fun applyProfile(profileId: String): Boolean {
        val targetProfile = profiles.first().find { it.id == profileId } ?: return false
        editSafely { prefs ->
            applySettingsToPreferences(prefs, targetProfile.settings)
            prefs[ProfileKeys.ActiveProfileId] = profileId
        }
        return true
    }

    suspend fun updateProfile(profileId: String): Boolean {
        val allProfiles = profiles.first().toMutableList()
        val index = allProfiles.indexOfFirst { it.id == profileId }
        if (index == -1) return false

        val existing = allProfiles[index]
        val currentSettings = settings.first()
        val updated = existing.copy(settings = currentSettings)
        allProfiles[index] = updated

        val array = JSONArray()
        allProfiles.forEach { array.put(it.toJson()) }

        editSafely { prefs ->
            prefs[ProfileKeys.ProfilesJson] = array.toString()
            prefs[ProfileKeys.ActiveProfileId] = profileId
        }
        return true
    }

    suspend fun deleteProfile(profileId: String): Boolean {
        val allProfiles = profiles.first().toMutableList()
        val index = allProfiles.indexOfFirst { it.id == profileId }
        if (index == -1) return false
        val target = allProfiles[index]
        if (target.isDefault) return false // Cannot delete default built-in profile

        allProfiles.removeAt(index)
        val array = JSONArray()
        allProfiles.forEach { array.put(it.toJson()) }

        val currentActiveId = activeProfileId.first()
        val newActiveId = if (currentActiveId == profileId) {
            IslandProfile.DEFAULT_ID
        } else {
            currentActiveId
        }

        editSafely { prefs ->
            prefs[ProfileKeys.ProfilesJson] = array.toString()
            prefs[ProfileKeys.ActiveProfileId] = newActiveId
            if (currentActiveId == profileId) {
                allProfiles.find { it.id == IslandProfile.DEFAULT_ID }?.let { defProfile ->
                    applySettingsToPreferences(prefs, defProfile.settings)
                }
            }
        }
        return true
    }

    suspend fun renameProfile(profileId: String, newName: String): Boolean {
        val allProfiles = profiles.first().toMutableList()
        val index = allProfiles.indexOfFirst { it.id == profileId }
        if (index == -1) return false
        val existing = allProfiles[index]
        if (existing.isDefault) return false

        val trimmed = newName.trim().ifEmpty { return false }
        allProfiles[index] = existing.copy(name = trimmed)

        val array = JSONArray()
        allProfiles.forEach { array.put(it.toJson()) }

        editSafely { prefs ->
            prefs[ProfileKeys.ProfilesJson] = array.toString()
        }
        return true
    }

    suspend fun importProfile(jsonString: String): Result<IslandProfile> {
        return runCatching {
            val json = JSONObject(jsonString)
            val imported = IslandProfile.fromJson(json).copy(
                id = java.util.UUID.randomUUID().toString(),
                isDefault = false
            )
            val currentProfiles = profiles.first().toMutableList()
            currentProfiles.add(imported)

            val array = JSONArray()
            currentProfiles.forEach { array.put(it.toJson()) }

            editSafely { prefs ->
                applySettingsToPreferences(prefs, imported.settings)
                prefs[ProfileKeys.ProfilesJson] = array.toString()
                prefs[ProfileKeys.ActiveProfileId] = imported.id
            }
            imported
        }
    }

    suspend fun exportProfiles(): String {
        val allProfiles = profiles.first()
        val array = JSONArray()
        allProfiles.forEach { array.put(it.toJson()) }
        return array.toString(2)
    }

    suspend fun resetProfilesToDefault() {
        val defaults = IslandProfile.defaultProfiles()
        val array = JSONArray()
        defaults.forEach { array.put(it.toJson()) }

        editSafely { prefs ->
            prefs[ProfileKeys.ProfilesJson] = array.toString()
            prefs[ProfileKeys.ActiveProfileId] = IslandProfile.DEFAULT_ID
            defaults.firstOrNull()?.let {
                applySettingsToPreferences(prefs, it.settings)
            }
        }
    }

    private fun applySettingsToPreferences(prefs: MutablePreferences, s: SmartIslandSettings) {
        prefs[Keys.Width] = s.width
        prefs[Keys.Height] = s.height
        prefs[Keys.XOffset] = s.xOffset
        prefs[Keys.YOffset] = s.yOffset
        prefs[Keys.CornerRadius] = s.cornerRadius
        prefs[Keys.Opacity] = s.opacity
        prefs[Keys.BatteryColor] = s.batteryColor
        prefs[Keys.NotificationDotColor] = s.notificationDotColor
        prefs[Keys.MusicVisualizerColor] = s.musicVisualizerColor
        prefs[Keys.HotspotColor] = s.hotspotColor
        prefs[Keys.CallColor] = s.callColor
        prefs[Keys.LiveActivityColor] = s.liveActivityColor
        prefs[Keys.TransferColor] = s.transferColor
        prefs[Keys.NavigationColor] = s.navigationColor
        prefs[Keys.BluetoothColor] = s.bluetoothColor
        prefs[Keys.FlashlightColor] = s.flashlightColor
        prefs[Keys.ScreenRecordingColor] = s.screenRecordingColor
        prefs[Keys.TimerColor] = s.timerColor
        prefs[Keys.StopwatchColor] = s.stopwatchColor
        prefs[Keys.PinchNotchEnabled] = s.pinchNotchEnabled
        prefs[Keys.AnimationDelayMs] = s.animationDelayMs
        prefs[Keys.ContentHorizontalPadding] = s.contentHorizontalPadding
        prefs[Keys.ContentVerticalPadding] = s.contentVerticalPadding
        prefs[Keys.AnimationSpringStiffness] = s.animationSpringStiffness
        prefs[Keys.AnimationSpringDamping] = s.animationSpringDamping
        prefs[Keys.ShadowElevation] = s.shadowElevation
        prefs[Keys.ShadowAlpha] = s.shadowAlpha
        prefs[Keys.ShadowColor] = s.shadowColor
        prefs[Keys.BorderWidth] = s.borderWidth
        prefs[Keys.BorderAlpha] = s.borderAlpha
        prefs[Keys.BorderColor] = s.borderColor
        prefs[Keys.InnerShadowRadius] = s.innerShadowRadius
        prefs[Keys.InnerShadowAlpha] = s.innerShadowAlpha
        prefs[Keys.InnerShadowColor] = s.innerShadowColor
        prefs[Keys.BackgroundBlur] = s.backgroundBlur
        prefs[Keys.IslandBackgroundColor] = s.islandBackgroundColor
        prefs[Keys.PinchCenterClearance] = s.pinchCenterClearance
        prefs[Keys.SplitIslandEnabled] = s.splitIslandEnabled
        prefs[Keys.ExpandedScreenMargin] = s.expandedScreenMargin
        prefs[Keys.ExpandedTopMargin] = s.expandedTopMargin
        prefs[Keys.ExpandedContentHorizontalPadding] = s.expandedContentHorizontalPadding
        prefs[Keys.ExpandedContentVerticalPadding] = s.expandedContentVerticalPadding
        prefs[Keys.ShowLyricsUnderControls] = s.showLyricsUnderControls
        prefs[Keys.EnableShadow] = s.enableShadow
        prefs[Keys.EnableMusicArtworkBackground] = s.enableMusicArtworkBackground
    }

    private suspend fun editSafely(transform: suspend (MutablePreferences) -> Unit) {
        try {
            context.smartIslandDataStore.edit(transform)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Unable to persist settings", error)
        }
    }

    private fun validDimension(
        value: Float?,
        fallback: Float,
        min: Float,
        max: Float
    ): Float = value?.takeIf { it.isFinite() }?.coerceIn(min, max) ?: fallback

    private fun validColor(value: Long?, fallback: Long): Long =
        value?.takeIf { it != 0L } ?: fallback

    private companion object {
        const val TAG = "SmartIslandSettings"
        const val MAX_SHORTCUTS = 8
        const val MAX_PACKAGE_NAME_LENGTH = 255
        const val MIN_ARGB_COLOR = 0x01000000L
        const val MAX_ARGB_COLOR = 0xFFFFFFFFL
        const val ALPHA_SHIFT = 24
        val VALID_LOCK_SCREEN_PRIVACY_VALUES = setOf("AppIconOnly", "FullContent")
    }
}
