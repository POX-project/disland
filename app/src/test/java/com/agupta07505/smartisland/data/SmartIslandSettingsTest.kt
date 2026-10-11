/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SmartIslandSettingsTest {
    @Test
    fun testDefaultSettings() {
        val settings = SmartIslandSettings.Default
        assertEquals(false, settings.enabled)
        assertEquals(112f, settings.width)
        assertEquals(34f, settings.height)
        assertEquals(0f, settings.xOffset)
        assertEquals(12f, settings.yOffset)
        assertEquals(22f, settings.cornerRadius)
        assertEquals(1f, settings.opacity)
        assertEquals(true, settings.showNotificationActions)
        assertEquals(true, settings.hideFromNotificationShade)
        assertEquals(true, settings.autoExpandOnNotification)
        assertEquals(true, settings.enableShadow)
        assertEquals(true, settings.enableMusicArtworkBackground)
        assertEquals(0xFF2563EBL, settings.bluetoothColor)
        assertEquals(0xFFF59E0BL, settings.flashlightColor)
        assertEquals(0xFFEF4444L, settings.screenRecordingColor)
        assertEquals(0xFFF59E0BL, settings.timerColor)
        assertEquals(0xFF06B6D4L, settings.stopwatchColor)
        assertEquals(false, settings.allowNetworkChecks)
        assertEquals(false, settings.enableNotificationHistory)
        assertEquals(72, settings.notificationHistoryRetentionHours)
        assertEquals(false, settings.pinchNotchEnabled)
        assertEquals(0, settings.animationDelayMs)
        assertEquals(12f, settings.contentHorizontalPadding)
        assertEquals(0f, settings.contentVerticalPadding)
        assertEquals(460f, settings.animationSpringStiffness)
        assertEquals(0.72f, settings.animationSpringDamping)
        assertEquals(14f, settings.shadowElevation)
        assertEquals(0.8f, settings.shadowAlpha)
        assertEquals(0xFF000000L, settings.shadowColor)
        assertEquals(0f, settings.borderWidth)
        assertEquals(0.20f, settings.borderAlpha)
        assertEquals(0xFFFFFFFFL, settings.borderColor)
        assertEquals(0f, settings.innerShadowRadius)
        assertEquals(0f, settings.innerShadowAlpha)
        assertEquals(0xFFFFFFFFL, settings.innerShadowColor)
        assertEquals(0f, settings.backgroundBlur)
        assertEquals(0xFF000000L, settings.islandBackgroundColor)
        assertEquals(36f, settings.pinchCenterClearance)
        assertEquals(false, settings.splitIslandEnabled)
        assertEquals(12f, settings.expandedScreenMargin)
        assertEquals(10f, settings.expandedTopMargin)
        assertEquals(18f, settings.expandedContentHorizontalPadding)
        assertEquals(18f, settings.expandedContentVerticalPadding)
    }

    @Test
    fun testPinchNotchAndAnimationDelaySettings() {
        val settings = SmartIslandSettings(
            pinchNotchEnabled = true,
            animationDelayMs = 250
        )
        assertEquals(true, settings.pinchNotchEnabled)
        assertEquals(250, settings.animationDelayMs)
    }

    @Test
    fun testEquality() {
        val settings1 = SmartIslandSettings(enabled = true, width = 120f, pinchNotchEnabled = true, animationDelayMs = 200)
        val settings2 = SmartIslandSettings(enabled = true, width = 120f, pinchNotchEnabled = true, animationDelayMs = 200)
        val settings3 = SmartIslandSettings(enabled = true, width = 120f, pinchNotchEnabled = false, animationDelayMs = 200)

        assertEquals(settings1, settings2)
        assertNotEquals(settings1, settings3)
    }

    @Test
    fun testSettingsJsonSerialization() {
        val original = SmartIslandSettings(
            enabled = true,
            width = 145f,
            height = 36f,
            xOffset = -10f,
            yOffset = 15f,
            cornerRadius = 24f,
            opacity = 0.95f,
            showLyricsUnderControls = true,
            splitIslandEnabled = true,
            islandBackgroundColor = 0xFF0B0E14L,
            borderWidth = 1.2f,
            borderAlpha = 0.3f,
            borderColor = 0xFF38BDF8L,
            animationSpringStiffness = 580f,
            animationSpringDamping = 0.68f
        )

        val json = original.toJson()
        val restored = SmartIslandSettings.fromJson(json)

        assertEquals(original.enabled, restored.enabled)
        assertEquals(original.width, restored.width, 0.001f)
        assertEquals(original.height, restored.height, 0.001f)
        assertEquals(original.xOffset, restored.xOffset, 0.001f)
        assertEquals(original.yOffset, restored.yOffset, 0.001f)
        assertEquals(original.cornerRadius, restored.cornerRadius, 0.001f)
        assertEquals(original.opacity, restored.opacity, 0.001f)
        assertEquals(original.showLyricsUnderControls, restored.showLyricsUnderControls)
        assertEquals(original.splitIslandEnabled, restored.splitIslandEnabled)
        assertEquals(original.islandBackgroundColor, restored.islandBackgroundColor)
        assertEquals(original.borderWidth, restored.borderWidth, 0.001f)
        assertEquals(original.borderAlpha, restored.borderAlpha, 0.001f)
        assertEquals(original.borderColor, restored.borderColor)
        assertEquals(original.animationSpringStiffness, restored.animationSpringStiffness, 0.001f)
        assertEquals(original.animationSpringDamping, restored.animationSpringDamping, 0.001f)
    }
}
