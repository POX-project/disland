/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationContentExtractorTest {

    @Test
    fun testStandardTitleAndTextExtraction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)

        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Alice"
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Hello there!"
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        mockNotif.extras = extras
        mockNotif.tickerText = null
        mockNotif.actions = null
        every { mockSbn.notification } returns mockNotif

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "WhatsApp")

        assertEquals("Alice", extracted.title)
        assertEquals("Hello there!", extracted.text)
        assertEquals("Hello there!", extracted.bigText)
    }

    @Test
    fun testMessagingStyleExtraction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)

        // In MessagingStyle, EXTRA_TEXT is null/empty and messages are in EXTRA_MESSAGES
        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns "Android Devs"
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns null
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        val msg1 = mockk<Bundle>(relaxed = true)
        every { msg1.getCharSequence("text") } returns "Hey anyone online?"
        every { msg1.getCharSequence("sender") } returns "Bob"

        val msg2 = mockk<Bundle>(relaxed = true)
        every { msg2.getCharSequence("text") } returns "Yes, working on Smart Island!"
        every { msg2.getCharSequence("sender") } returns "Charlie"

        val messagesArray = arrayOf<Parcelable>(msg1, msg2)
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns messagesArray

        mockNotif.extras = extras
        mockNotif.tickerText = null
        mockNotif.actions = null
        every { mockSbn.notification } returns mockNotif

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Telegram")

        assertEquals("Android Devs", extracted.title)
        assertEquals("Yes, working on Smart Island!", extracted.text)
        assertTrue(extracted.bigText.contains("Bob: Hey anyone online?"))
        assertTrue(extracted.bigText.contains("Charlie: Yes, working on Smart Island!"))
    }

    @Test
    fun testInboxStyleExtraction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)

        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "3 new emails"
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        val lines = arrayOf<CharSequence>(
            "GitHub: New release v6.0.0 is available",
            "Security Alert: New sign-in detected",
            "Team: Weekly sync starts in 10 minutes"
        )
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns lines

        mockNotif.extras = extras
        mockNotif.tickerText = null
        mockNotif.actions = null
        every { mockSbn.notification } returns mockNotif

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Gmail")

        assertEquals("3 new emails", extracted.title)
        assertEquals("Team: Weekly sync starts in 10 minutes", extracted.text)
        assertTrue(extracted.bigText.contains("GitHub: New release"))
    }

    @Test
    fun testMissingTitleFallbacksToAppName() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)

        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns null
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Just completed 10,000 steps!"
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        mockNotif.extras = extras
        mockNotif.tickerText = null
        mockNotif.actions = null
        every { mockSbn.notification } returns mockNotif

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Fitness")

        assertEquals("Fitness", extracted.title)
        assertEquals("Just completed 10,000 steps!", extracted.text)
    }

    @Test
    fun testExtractActionsWithQuickReply() {
        val mockNotif = mockk<Notification>()
        val mockAction1 = mockk<Notification.Action>()
        val mockAction2 = mockk<Notification.Action>()
        val mockPendingIntent1 = mockk<PendingIntent>()
        val mockPendingIntent2 = mockk<PendingIntent>()

        mockAction1.title = "Reply"
        mockAction1.actionIntent = mockPendingIntent1
        every { mockAction1.remoteInputs } returns arrayOf(mockk(relaxed = true))

        mockAction2.title = "Mark as read"
        mockAction2.actionIntent = mockPendingIntent2
        every { mockAction2.remoteInputs } returns null

        mockNotif.actions = arrayOf(mockAction1, mockAction2)

        val actions = NotificationContentExtractor.extractActions(mockNotif)

        assertEquals(2, actions.size)
        assertEquals("Reply", actions[0].title)
        assertTrue(actions[0].isQuickReply)
        assertEquals("Mark as read", actions[1].title)
        assertFalse(actions[1].isQuickReply)
    }

    @Test
    fun testMessagingStyleVoiceNoteExtraction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)

        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Alice"
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        // Audio message where text is null and MIME type is audio/mp4 (e.g. Instagram voice message)
        val voiceMsg = mockk<Bundle>(relaxed = true)
        every { voiceMsg.getCharSequence("text") } returns null
        every { voiceMsg.getString("type") } returns "audio/mp4"
        every { voiceMsg.getCharSequence("sender") } returns "Alice"
        every { voiceMsg.containsKey("uri") } returns true

        val messagesArray = arrayOf<Parcelable>(voiceMsg)
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns messagesArray

        mockNotif.extras = extras
        mockNotif.tickerText = null
        mockNotif.actions = null
        every { mockSbn.notification } returns mockNotif

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Instagram")

        assertEquals("Alice", extracted.title)
        assertEquals("Voice message", extracted.text)
        assertTrue(extracted.bigText.contains("Alice: Voice message"))
        assertEquals(1, extracted.messages.size)
        assertEquals("Voice message", extracted.messages[0].text)
        assertEquals("Alice", extracted.messages[0].sender)
    }

    @Test
    fun testMessagingNotificationAutoSynthesizesReplyAction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockContentIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.google.android.apps.messaging"
        every { mockSbn.postTime } returns 12345L
        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_MESSAGE
        mockNotif.contentIntent = mockContentIntent
        mockNotif.actions = emptyArray()
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Alice"
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Hey! Are you there?"
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Messages")

        // Should automatically synthesize a quick reply action even if actions array was empty
        assertTrue(extracted.actions.any { it.isQuickReply })
        val replyAction = extracted.actions.first { it.isQuickReply }
        assertEquals("Reply", replyAction.title)
        assertEquals("key_text_reply", replyAction.remoteInputKey)

        // Messages list should contain the initial message
        assertEquals(1, extracted.messages.size)
        assertEquals("Hey! Are you there?", extracted.messages[0].text)
        assertEquals("Alice", extracted.messages[0].sender)
    }

    @Test
    fun testSocialNotificationDoesNotSynthesizeReplyAction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockContentIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.instagram.android"
        every { mockSbn.postTime } returns 12345L
        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_SOCIAL
        mockNotif.contentIntent = mockContentIntent
        mockNotif.actions = emptyArray()
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "instagram"
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "john_doe liked your story."
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Instagram")

        // Must NOT synthesize any reply action for social alert
        assertFalse(extracted.actions.any { it.isQuickReply })
        assertFalse(extracted.actions.any { it.title.equals("Reply", ignoreCase = true) })
    }

    @Test
    fun testDownloadNotificationDoesNotSynthesizeReplyAction() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockContentIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.android.vending"
        every { mockSbn.postTime } returns 12345L
        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_PROGRESS
        mockNotif.contentIntent = mockContentIntent
        mockNotif.actions = emptyArray()
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0) } returns 100
        every { extras.getInt(Notification.EXTRA_PROGRESS, 0) } returns 45
        every { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) } returns null
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Subway Surfers"
        every { extras.getCharSequence("android.title.big") } returns null
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "45 MB / 100 MB • 1 min remaining"
        every { extras.getCharSequence(Notification.EXTRA_BIG_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUB_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) } returns null
        every { extras.getCharSequence(Notification.EXTRA_INFO_TEXT) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null
        every { extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) } returns null
        every { extras.get(Notification.EXTRA_PICTURE) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Google Play Store")

        // Must NOT synthesize any reply action for download
        assertFalse(extracted.actions.any { it.isQuickReply })
        assertFalse(extracted.actions.any { it.title.equals("Reply", ignoreCase = true) })
    }

    @Test
    fun testYouTubeCommentNotificationDoesNotHaveQuickReply() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockAction = mockk<Notification.Action>()
        val mockPendingIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.google.android.youtube"
        every { mockSbn.postTime } returns 12345L

        mockAction.title = "Reply"
        mockAction.actionIntent = mockPendingIntent
        every { mockAction.remoteInputs } returns arrayOf(mockk(relaxed = true))

        mockNotif.extras = extras
        mockNotif.category = null
        mockNotif.actions = arrayOf(mockAction)
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Tech Channel"
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "User commented: Great video!"
        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "YouTube")

        assertFalse(extracted.isConversation)
        assertTrue(extracted.messages.isEmpty())
        assertEquals(1, extracted.actions.size)
        // Must NOT be marked as quick reply because YouTube comments are not chat conversations
        assertFalse(extracted.actions[0].isQuickReply)
    }

    @Test
    fun testRedditNotificationWithReplyActionDoesNotHaveQuickReply() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockAction = mockk<Notification.Action>()
        val mockPendingIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.reddit.frontpage"
        every { mockSbn.postTime } returns 12345L

        mockAction.title = "Reply"
        mockAction.actionIntent = mockPendingIntent
        every { mockAction.remoteInputs } returns arrayOf(mockk(relaxed = true))

        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_MESSAGE
        mockNotif.actions = arrayOf(mockAction)
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "r/androiddev"
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Someone replied to your post: Neat!"
        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Reddit")

        assertFalse(extracted.isConversation)
        assertTrue(extracted.messages.isEmpty())
        assertEquals(1, extracted.actions.size)
        assertFalse(extracted.actions[0].isQuickReply)
    }

    @Test
    fun testGmailEmailWithReplyActionDoesNotHaveQuickReply() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockAction1 = mockk<Notification.Action>()
        val mockAction2 = mockk<Notification.Action>()
        val mockPendingIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.google.android.gm"
        every { mockSbn.postTime } returns 12345L

        mockAction1.title = "Reply"
        mockAction1.actionIntent = mockPendingIntent
        every { mockAction1.remoteInputs } returns arrayOf(mockk(relaxed = true))

        mockAction2.title = "Archive"
        mockAction2.actionIntent = mockPendingIntent
        every { mockAction2.remoteInputs } returns null

        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_EMAIL
        mockNotif.actions = arrayOf(mockAction1, mockAction2)
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Alice Smith"
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Project update for Q3"
        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Gmail")

        assertFalse(extracted.isConversation)
        assertTrue(extracted.messages.isEmpty())
        assertFalse(extracted.actions.any { it.isQuickReply })
    }

    @Test
    fun testOtpSmsDoesNotHaveQuickReplyOrSynthesizeReply() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockContentIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.google.android.apps.messaging"
        every { mockSbn.postTime } returns 12345L
        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_MESSAGE
        mockNotif.contentIntent = mockContentIntent
        mockNotif.actions = emptyArray()
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Bank Alert"
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Your OTP verification code is 492019. Do not share with anyone."
        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns null
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns null

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "Messages")

        assertFalse(extracted.isConversation)
        assertTrue(extracted.messages.isEmpty())
        assertFalse(extracted.actions.any { it.isQuickReply })
    }

    @Test
    fun testWhatsAppRealConversationHasQuickReplyAndMessages() {
        val mockSbn = mockk<StatusBarNotification>()
        val mockNotif = mockk<Notification>()
        val extras = mockk<Bundle>(relaxed = true)
        val mockAction = mockk<Notification.Action>()
        val mockPendingIntent = mockk<PendingIntent>()

        every { mockSbn.packageName } returns "com.whatsapp"
        every { mockSbn.postTime } returns 12345L

        mockAction.title = "Reply"
        mockAction.actionIntent = mockPendingIntent
        every { mockAction.remoteInputs } returns arrayOf(mockk(relaxed = true))

        val msgBundle = mockk<Bundle>(relaxed = true)
        every { msgBundle.getCharSequence("text") } returns "Hey, are you ready?"
        every { msgBundle.getCharSequence("sender") } returns "Bob"
        val msgs = arrayOf<Parcelable>(msgBundle)

        every { extras.getString(Notification.EXTRA_TEMPLATE) } returns "android.app.Notification\$MessagingStyle"
        every { extras.getParcelableArray(Notification.EXTRA_MESSAGES) } returns msgs
        every { extras.getCharSequence(Notification.EXTRA_TITLE) } returns "Bob"
        every { extras.getCharSequence(Notification.EXTRA_TEXT) } returns "Hey, are you ready?"

        mockNotif.extras = extras
        mockNotif.category = Notification.CATEGORY_MESSAGE
        mockNotif.actions = arrayOf(mockAction)
        mockNotif.tickerText = null
        every { mockSbn.notification } returns mockNotif

        val mockContext = mockk<Context>(relaxed = true)
        val extracted = NotificationContentExtractor.extract(mockSbn, mockContext, "WhatsApp")

        assertTrue(extracted.isConversation)
        assertEquals(1, extracted.messages.size)
        assertEquals("Hey, are you ready?", extracted.messages[0].text)
        assertTrue(extracted.actions.any { it.isQuickReply })
    }
}
