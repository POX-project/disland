/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.os.Bundle
import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import com.agupta07505.smartisland.model.IslandNotificationAction
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallStateParserTest {

    private fun createBaseExtras(): Bundle {
        val extras = mockk<Bundle>()
        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns null
        every { extras.containsKey(Notification.EXTRA_MEDIA_SESSION) } returns false
        every { extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false) } returns false
        every { extras.getBoolean("android.showChronometer", false) } returns false
        every { extras.getInt("android.callType", -1) } returns -1
        every { extras.containsKey("android.answerIntent") } returns false
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        return extras
    }

    @Test
    fun testIncomingCallRingingWithAnswerAction() {
        val actions = listOf(
            IslandNotificationAction(title = "Decline", pendingIntent = null),
            IslandNotificationAction(title = "Answer", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "John Doe", text = "Incoming call")
        assertEquals(CallState.RINGING, state)
        assertTrue(CallStateParser.isCallRinging(actions, "John Doe", "Incoming call"))
        assertFalse(CallStateParser.isCallDialing(actions, "John Doe", "Incoming call"))
        assertFalse(CallStateParser.isCallActive(actions, "John Doe", "Incoming call"))
    }

    @Test
    fun testIncomingCallRingingWithNonEnglishAction() {
        val actions = listOf(
            IslandNotificationAction(title = "Rechazar", pendingIntent = null),
            IslandNotificationAction(title = "Contestar", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "Maria", text = "Llamada entrante")
        assertEquals(CallState.RINGING, state)
    }

    @Test
    fun testIncomingCallRingingWithCallStyleExtra() {
        val extras = createBaseExtras()
        every { extras.getInt("android.callType", -1) } returns 1 // CALL_TYPE_INCOMING
        val actions = emptyList<IslandNotificationAction>()

        val state = CallStateParser.parseCallState(actions, title = "Mom", text = "", extras = extras)
        assertEquals(CallState.RINGING, state)
    }

    @Test
    fun testOutgoingCallDialingWithCallingText() {
        val actions = listOf(
            IslandNotificationAction(title = "Hang up", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "Alice", text = "Calling...")
        assertEquals(CallState.DIALING, state)
        assertFalse(CallStateParser.isCallRinging(actions, "Alice", "Calling..."))
        assertTrue(CallStateParser.isCallDialing(actions, "Alice", "Calling..."))
        assertFalse(CallStateParser.isCallActive(actions, "Alice", "Calling..."))
    }

    @Test
    fun testOutgoingCallDialingWithCallingMobileText() {
        val actions = listOf(
            IslandNotificationAction(title = "End call", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "Bob", text = "Calling mobile...")
        assertEquals(CallState.DIALING, state)
    }

    @Test
    fun testOutgoingCallDialingNonEnglish() {
        val actions = listOf(IslandNotificationAction(title = "Colgar", pendingIntent = null))
        val stateEs = CallStateParser.parseCallState(actions, title = "Carlos", text = "Llamando...")
        assertEquals(CallState.DIALING, stateEs)

        val statePt = CallStateParser.parseCallState(actions, title = "Lucas", text = "Chamando...")
        assertEquals(CallState.DIALING, statePt)

        val stateDe = CallStateParser.parseCallState(actions, title = "Hans", text = "Wählt...")
        assertEquals(CallState.DIALING, stateDe)

        val stateZh = CallStateParser.parseCallState(actions, title = "张伟", text = "正在呼叫...")
        assertEquals(CallState.DIALING, stateZh)
    }

    @Test
    fun testActiveCallWithChronometer() {
        val extras = createBaseExtras()
        every { extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false) } returns true
        val actions = listOf(
            IslandNotificationAction(title = "Hang up", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "Alice", text = "Calling...", extras = extras)
        assertEquals(CallState.ACTIVE, state)
        assertTrue(CallStateParser.isCallActive(actions, "Alice", "Calling...", extras = extras))
    }

    @Test
    fun testActiveCallWithOngoingCallText() {
        val actions = listOf(
            IslandNotificationAction(title = "Hang up", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "Bob", text = "Ongoing call")
        assertEquals(CallState.ACTIVE, state)
    }

    @Test
    fun testActiveCallWithDurationText() {
        val actions = listOf(
            IslandNotificationAction(title = "Hang up", pendingIntent = null)
        )
        val state = CallStateParser.parseCallState(actions, title = "Charlie", text = "01:23")
        assertEquals(CallState.ACTIVE, state)
    }

    @Test
    fun testIslandNotificationProperties() {
        val ringingNotif = IslandNotification(
            packageName = "com.google.android.dialer",
            appName = "Phone",
            title = "Mom",
            text = "Incoming call",
            timeMillis = 1000L,
            mode = IslandMode.IncomingCall,
            actionIntents = listOf(
                IslandNotificationAction("Answer", null),
                IslandNotificationAction("Decline", null)
            )
        )
        assertTrue(ringingNotif.isCallRinging)
        assertFalse(ringingNotif.isCallDialing)
        assertFalse(ringingNotif.isCallActive)

        val dialingNotif = IslandNotification(
            packageName = "com.google.android.dialer",
            appName = "Phone",
            title = "Dad",
            text = "Calling...",
            timeMillis = 1000L,
            mode = IslandMode.IncomingCall,
            actionIntents = listOf(IslandNotificationAction("Hang up", null))
        )
        assertFalse(dialingNotif.isCallRinging)
        assertTrue(dialingNotif.isCallDialing)
        assertFalse(dialingNotif.isCallActive)

        val activeNotif = IslandNotification(
            packageName = "com.google.android.dialer",
            appName = "Phone",
            title = "Dad",
            text = "Ongoing call",
            timeMillis = 1000L,
            mode = IslandMode.IncomingCall,
            actionIntents = listOf(IslandNotificationAction("Hang up", null))
        )
        assertFalse(activeNotif.isCallRinging)
        assertFalse(activeNotif.isCallDialing)
        assertTrue(activeNotif.isCallActive)
    }
}
