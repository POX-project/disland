/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.model

import com.agupta07505.smartisland.data.SmartIslandSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandProfileTest {

    @Test
    fun testDefaultProfilesList() {
        val defaults = IslandProfile.defaultProfiles()
        assertTrue("Default profiles list should not be empty", defaults.isNotEmpty())
        assertEquals(5, defaults.size)

        val defaultProfile = defaults.find { it.id == IslandProfile.DEFAULT_ID }
        assertNotNull("Default profile must exist", defaultProfile)
        assertTrue(defaultProfile!!.isDefault)
        assertEquals("Dynamic Island Pro (Default)", defaultProfile.name)

        val appleProfile = defaults.find { it.id == IslandProfile.APPLE_ID }
        assertNotNull(appleProfile)
        assertEquals(120f, appleProfile!!.settings.width)
        assertEquals(35f, appleProfile.settings.height)

        val notchProfile = defaults.find { it.id == IslandProfile.COMPACT_NOTCH_ID }
        assertNotNull(notchProfile)
        assertTrue(notchProfile!!.settings.pinchNotchEnabled)

        val oledProfile = defaults.find { it.id == IslandProfile.OLED_ID }
        assertNotNull(oledProfile)
        assertEquals(580f, oledProfile!!.settings.animationSpringStiffness)

        val cyberProfile = defaults.find { it.id == IslandProfile.CYBER_ID }
        assertNotNull(cyberProfile)
        assertEquals(0xFF38BDF8L, cyberProfile!!.settings.borderColor)
    }

    @Test
    fun testProfileJsonSerialization() {
        val customSettings = SmartIslandSettings.Default.copy(
            width = 160f,
            height = 42f,
            cornerRadius = 24f,
            showLyricsUnderControls = true,
            splitIslandEnabled = true,
            islandBackgroundColor = 0xFF121212L
        )

        val profile = IslandProfile(
            id = "test-profile-123",
            name = "My Custom Notch",
            description = "Custom profile for testing",
            isDefault = false,
            createdAt = 1700000000000L,
            settings = customSettings
        )

        val json = profile.toJson()
        assertEquals("test-profile-123", json.getString("id"))
        assertEquals("My Custom Notch", json.getString("name"))
        assertEquals("Custom profile for testing", json.getString("description"))
        assertFalse(json.getBoolean("isDefault"))
        assertEquals(1700000000000L, json.getLong("createdAt"))

        val deserialized = IslandProfile.fromJson(json)
        assertEquals(profile.id, deserialized.id)
        assertEquals(profile.name, deserialized.name)
        assertEquals(profile.description, deserialized.description)
        assertEquals(profile.isDefault, deserialized.isDefault)
        assertEquals(profile.createdAt, deserialized.createdAt)
        assertEquals(160f, deserialized.settings.width)
        assertEquals(42f, deserialized.settings.height)
        assertEquals(24f, deserialized.settings.cornerRadius)
        assertTrue(deserialized.settings.showLyricsUnderControls)
        assertTrue(deserialized.settings.splitIslandEnabled)
        assertEquals(0xFF121212L, deserialized.settings.islandBackgroundColor)
    }

    @Test
    fun testProfileFromJsonDefaultsWhenFieldsMissing() {
        val emptyJson = org.json.JSONObject()
        val profile = IslandProfile.fromJson(emptyJson)

        assertNotNull(profile.id)
        assertEquals("Custom Profile", profile.name)
        assertEquals("", profile.description)
        assertFalse(profile.isDefault)
        assertEquals(SmartIslandSettings.Default.width, profile.settings.width)
    }
}
