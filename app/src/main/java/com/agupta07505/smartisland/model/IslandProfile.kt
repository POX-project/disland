/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.model

import com.agupta07505.smartisland.data.SmartIslandSettings
import org.json.JSONObject
import java.util.UUID

data class IslandProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val settings: SmartIslandSettings
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("description", description)
            put("isDefault", isDefault)
            put("createdAt", createdAt)
            put("settings", settings.toJson())
        }
    }

    companion object {
        const val DEFAULT_ID = "profile_default"
        const val APPLE_ID = "profile_apple_clean"
        const val COMPACT_NOTCH_ID = "profile_compact_notch"
        const val OLED_ID = "profile_ultra_oled"
        const val CYBER_ID = "profile_cyber_glow"

        fun fromJson(json: JSONObject): IslandProfile {
            return IslandProfile(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Custom Profile"),
                description = json.optString("description", ""),
                isDefault = json.optBoolean("isDefault", false),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                settings = if (json.has("settings")) {
                    SmartIslandSettings.fromJson(json.getJSONObject("settings"))
                } else {
                    SmartIslandSettings.Default
                }
            )
        }

        fun defaultProfiles(): List<IslandProfile> {
            return listOf(
                IslandProfile(
                    id = DEFAULT_ID,
                    name = "Dynamic Island Pro (Default)",
                    description = "Balanced default settings with fluid physics, live synced lyrics, and centered geometry",
                    isDefault = true,
                    settings = SmartIslandSettings.Default
                ),
                IslandProfile(
                    id = APPLE_ID,
                    name = "Apple Dynamic Island (Clean)",
                    description = "Authentic iPhone 16 Pro styling with subtle glass border and crisp margins",
                    isDefault = true,
                    settings = SmartIslandSettings.Default.copy(
                        width = 120f,
                        height = 35f,
                        xOffset = 0f,
                        yOffset = 11f,
                        cornerRadius = 22f,
                        borderWidth = 0.8f,
                        borderAlpha = 0.22f,
                        shadowElevation = 16f,
                        shadowAlpha = 0.75f,
                        showLyricsUnderControls = true
                    )
                ),
                IslandProfile(
                    id = COMPACT_NOTCH_ID,
                    name = "Compact Waterdrop Notch",
                    description = "Tuned for waterdrop and center-camera screens with pinch notch clearance",
                    isDefault = true,
                    settings = SmartIslandSettings.Default.copy(
                        width = 175f,
                        height = 34f,
                        xOffset = 0f,
                        yOffset = 0f,
                        pinchNotchEnabled = true,
                        pinchCenterClearance = 36f,
                        contentHorizontalPadding = 14f,
                        cornerRadius = 18f
                    )
                ),
                IslandProfile(
                    id = OLED_ID,
                    name = "Ultra-Fluid OLED",
                    description = "True OLED black with high-stiffness spring physics and zero animation delay",
                    isDefault = true,
                    settings = SmartIslandSettings.Default.copy(
                        opacity = 1.0f,
                        islandBackgroundColor = 0xFF000000L,
                        animationSpringStiffness = 580f,
                        animationSpringDamping = 0.68f,
                        animationDelayMs = 0,
                        shadowElevation = 18f
                    )
                ),
                IslandProfile(
                    id = CYBER_ID,
                    name = "Cyber Glow Studio",
                    description = "Neon cyan glow with illuminated border and futuristic visualizer theme",
                    isDefault = true,
                    settings = SmartIslandSettings.Default.copy(
                        width = 135f,
                        height = 36f,
                        borderColor = 0xFF38BDF8L,
                        borderWidth = 1.4f,
                        borderAlpha = 0.65f,
                        shadowColor = 0xFF06B6D4L,
                        shadowElevation = 24f,
                        shadowAlpha = 0.85f,
                        innerShadowColor = 0xFF38BDF8L,
                        innerShadowRadius = 6f,
                        innerShadowAlpha = 0.30f,
                        backgroundBlur = 8f,
                        musicVisualizerColor = 0xFF38BDF8L
                    )
                )
            )
        }
    }
}
