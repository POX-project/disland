/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.data

import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmartIslandNotificationRepositoryTest {

    @Test
    fun testPostNotification() {
        val repository = SmartIslandNotificationRepository()
        val notif = IslandNotification(
            key = "test_key",
            packageName = "com.test",
            appName = "TestApp",
            title = "Test Title",
            text = "Test Text",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )

        repository.postNotification(notif, autoExpand = false)
        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        assertEquals("test_key", notifications[0].key)
    }

    @Test
    fun testRemoveNotification() {
        val repository = SmartIslandNotificationRepository()
        val notif1 = IslandNotification(
            key = "key1",
            packageName = "com.test",
            appName = "TestApp",
            title = "Title 1",
            text = "Text 1",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )
        val notif2 = IslandNotification(
            key = "key2",
            packageName = "com.test",
            appName = "TestApp",
            title = "Title 2",
            text = "Text 2",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )

        repository.postNotification(notif1)
        repository.postNotification(notif2)
        assertEquals(2, repository.notifications.value.size)

        repository.removeNotification("key1")
        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        assertEquals("key2", notifications[0].key)
    }

    @Test
    fun testRemoveAllNotifications() {
        val repository = SmartIslandNotificationRepository()
        val notif1 = IslandNotification(
            key = "key1",
            packageName = "com.test",
            appName = "TestApp",
            title = "Title 1",
            text = "Text 1",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )
        val notif2 = IslandNotification(
            key = "key2",
            packageName = "com.test",
            appName = "TestApp",
            title = "Title 2",
            text = "Text 2",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )

        repository.postNotification(notif1)
        repository.postNotification(notif2)
        assertEquals(2, repository.notifications.value.size)

        repository.removeAllNotifications()
        assertTrue(repository.notifications.value.isEmpty())
    }

    @Test
    fun testScreenRecordingDemoNotification() {
        val repository = SmartIslandNotificationRepository()
        repository.showDemo(IslandMode.ScreenRecording)

        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        val notif = notifications[0]
        assertEquals("demo_screen_recording", notif.key)
        assertEquals(IslandMode.ScreenRecording, notif.mode)
        assertEquals("Screen Recording", notif.title)
    }

    @Test
    fun testTimerDemoNotification() {
        val repository = SmartIslandNotificationRepository()
        repository.showDemo(IslandMode.Timer)

        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        val notif = notifications[0]
        assertEquals("demo_timer", notif.key)
        assertEquals(IslandMode.Timer, notif.mode)
        assertEquals("Tea Timer", notif.title)
    }

    @Test
    fun testStopwatchDemoNotification() {
        val repository = SmartIslandNotificationRepository()
        repository.showDemo(IslandMode.Stopwatch)

        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        val notif = notifications[0]
        assertEquals("demo_stopwatch", notif.key)
        assertEquals(IslandMode.Stopwatch, notif.mode)
        assertEquals("Stopwatch", notif.title)
    }

    @Test
    fun testRemoveNotificationsForPackage() {
        val repository = SmartIslandNotificationRepository()
        val notif1 = IslandNotification(
            key = "key1",
            packageName = "com.app.a",
            appName = "App A",
            title = "Title 1",
            text = "Text 1",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )
        val notif2 = IslandNotification(
            key = "key2",
            packageName = "com.app.a",
            appName = "App A",
            title = "Title 2",
            text = "Text 2",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )
        val notif3 = IslandNotification(
            key = "key3",
            packageName = "com.app.b",
            appName = "App B",
            title = "Title 3",
            text = "Text 3",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )

        repository.postNotification(notif1)
        repository.postNotification(notif2)
        repository.postNotification(notif3)
        assertEquals(3, repository.notifications.value.size)

        repository.removeNotificationsForPackage("com.app.a")
        val remaining = repository.notifications.value
        assertEquals(1, remaining.size)
        assertEquals("key3", remaining[0].key)
    }

    @Test
    fun testResetTimer() = runTest {
        val repository = SmartIslandNotificationRepository()
        var triggered = false

        val job = launch {
            repository.resetTimerEvent.collect {
                triggered = true
            }
        }

        // Wait using virtual time to allow coroutine collection setup
        delay(100)

        repository.resetTimer()
        advanceUntilIdle()
        assertTrue(triggered)
        job.cancel()
    }

    @Test
    fun testSendCommand() = runTest {
        val repository = SmartIslandNotificationRepository()
        val receivedCommands = mutableListOf<SmartIslandCommand>()

        val job = launch {
            repository.commands.collect {
                receivedCommands.add(it)
            }
        }

        // Wait using virtual time to allow coroutine collection setup
        delay(100)

        val command = SmartIslandCommand.CancelNotification("key_to_cancel")
        repository.sendCommand(command)
        advanceUntilIdle()

        assertEquals(1, receivedCommands.size)
        assertEquals(command, receivedCommands[0])
        job.cancel()
    }

    @Test
    fun testBluetoothDemoNotification() {
        val repository = SmartIslandNotificationRepository()
        repository.showDemo(IslandMode.Bluetooth)

        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        val notif = notifications[0]
        assertEquals("demo_bluetooth", notif.key)
        assertEquals(IslandMode.Bluetooth, notif.mode)
        assertEquals("AirPods Pro", notif.title)
    }

    @Test
    fun testFlashlightDemoNotification() {
        val repository = SmartIslandNotificationRepository()
        repository.showDemo(IslandMode.Flashlight)

        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        val notif = notifications[0]
        assertEquals("demo_flashlight", notif.key)
        assertEquals(IslandMode.Flashlight, notif.mode)
        assertEquals("Flashlight ON", notif.title)
    }

    @Test
    fun testShowDemoNotification() {
        val repository = SmartIslandNotificationRepository()
        repository.showDemo(IslandMode.Battery)

        val notifications = repository.notifications.value
        assertEquals(1, notifications.size)
        val demo = notifications[0]
        assertEquals("demo_battery", demo.key)
        assertEquals(IslandMode.Battery, demo.mode)
        assertEquals("85%", demo.text)
    }

    @Test
    fun testClearTestNotificationsOnlyRemovesDemoNotifications() {
        val repository = SmartIslandNotificationRepository()
        
        val realNotif = IslandNotification(
            key = "real_whatsapp_1",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "Alice",
            text = "Hello!",
            mode = IslandMode.Notification,
            timeMillis = System.currentTimeMillis()
        )
        repository.postNotification(realNotif)
        repository.showDemo(IslandMode.Battery)

        assertEquals(2, repository.notifications.value.size)

        repository.clearTestNotifications()

        val remaining = repository.notifications.value
        assertEquals(1, remaining.size)
        assertEquals("real_whatsapp_1", remaining[0].key)
    }

    @Test
    fun keepsOnlyTheMostRecentFiftyNotifications() {
        val repository = SmartIslandNotificationRepository()

        repeat(60) { index ->
            repository.postNotification(
                IslandNotification(
                    key = "key_$index",
                    packageName = "com.test",
                    appName = "TestApp",
                    title = "Title $index",
                    text = "Text $index",
                    mode = IslandMode.Notification,
                    timeMillis = index.toLong()
                )
            )
        }

        val notifications = repository.notifications.value
        assertEquals(50, notifications.size)
        assertEquals("key_10", notifications.first().key)
        assertEquals("key_59", notifications.last().key)
    }

    @Test
    fun appendUserReplyAppendsMessageAndKeepsNotification() {
        val repository = SmartIslandNotificationRepository()
        val initialNotif = IslandNotification(
            key = "msg_key_1",
            packageName = "com.google.android.apps.messaging",
            appName = "Messages",
            title = "Bob",
            text = "Hey! What's up?",
            timeMillis = 1000L,
            messages = listOf(
                com.agupta07505.smartisland.model.IslandChatMessage(
                    sender = "Bob",
                    text = "Hey! What's up?",
                    timeMillis = 1000L,
                    isFromUser = false
                )
            )
        )
        repository.postNotification(initialNotif)

        repository.appendUserReply("msg_key_1", "All good, working on code!")

        val current = repository.notifications.value.firstOrNull { it.key == "msg_key_1" }
        assertNotNull(current)
        assertEquals("All good, working on code!", current?.text)
        assertEquals(2, current?.messages?.size)
        assertEquals("Bob", current?.messages?.get(0)?.sender)
        assertFalse(current?.messages?.get(0)?.isFromUser ?: true)
        assertEquals("You", current?.messages?.get(1)?.sender)
        assertTrue(current?.messages?.get(1)?.isFromUser ?: false)
        assertEquals("All good, working on code!", current?.messages?.get(1)?.text)
    }

    @Test
    fun postNotificationMergesIncomingConversationMessagesPreservingUserReplies() {
        val repository = SmartIslandNotificationRepository()
        val baseTime = System.currentTimeMillis() - 60000L
        val notif1 = IslandNotification(
            key = "msg_chat_1",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "Alice",
            text = "Dinner at 7?",
            timeMillis = baseTime,
            messages = listOf(
                com.agupta07505.smartisland.model.IslandChatMessage(
                    sender = "Alice",
                    text = "Dinner at 7?",
                    timeMillis = baseTime,
                    isFromUser = false
                )
            )
        )
        repository.postNotification(notif1)

        // User replies in Island (System.currentTimeMillis())
        repository.appendUserReply("msg_chat_1", "Yes, see you there!")

        // Contact replies back with updated notification from WhatsApp later (System.currentTimeMillis() + 5000L)
        val replyTime = System.currentTimeMillis() + 5000L
        val notif2 = IslandNotification(
            key = "msg_chat_1",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "Alice",
            text = "Awesome!",
            timeMillis = replyTime,
            messages = listOf(
                com.agupta07505.smartisland.model.IslandChatMessage(
                    sender = "Alice",
                    text = "Dinner at 7?",
                    timeMillis = baseTime,
                    isFromUser = false
                ),
                com.agupta07505.smartisland.model.IslandChatMessage(
                    sender = "Alice",
                    text = "Awesome!",
                    timeMillis = replyTime,
                    isFromUser = false
                )
            )
        )
        repository.postNotification(notif2)

        val finalNotif = repository.notifications.value.first { it.key == "msg_chat_1" }
        assertEquals(3, finalNotif.messages.size)
        assertEquals("Dinner at 7?", finalNotif.messages[0].text)
        assertFalse(finalNotif.messages[0].isFromUser)
        assertEquals("Yes, see you there!", finalNotif.messages[1].text)
        assertTrue(finalNotif.messages[1].isFromUser)
        assertEquals("Awesome!", finalNotif.messages[2].text)
        assertFalse(finalNotif.messages[2].isFromUser)
    }

    @Test
    fun testDownloadUpdatesDoNotTriggerAutoExpand() = runTest {
        val repository = SmartIslandNotificationRepository()
        val autoExpandedKeys = mutableListOf<String>()
        val job = launch {
            repository.autoExpandEvent.collect { autoExpandedKeys.add(it) }
        }

        val download1 = IslandNotification(
            key = "dl_1",
            packageName = "com.android.vending",
            appName = "Google Play Store",
            title = "Subway Surfers",
            text = "40 MB / 100 MB • 2 mins remaining",
            mode = IslandMode.DownloadUpload,
            progress = 40,
            progressMax = 100,
            timeMillis = 1000L
        )
        repository.postNotification(download1, autoExpand = true)
        testScheduler.advanceUntilIdle()

        // DownloadUpload mode must never autoExpand
        assertTrue(autoExpandedKeys.isEmpty())

        // Time remaining update 2 seconds later
        val download2 = download1.copy(
            text = "45 MB / 100 MB • 1 min remaining",
            progress = 45,
            timeMillis = 3000L
        )
        repository.postNotification(download2, autoExpand = true)
        testScheduler.advanceUntilIdle()

        // Still must not trigger auto-expand or reveal spam
        assertTrue(autoExpandedKeys.isEmpty())

        job.cancel()
    }

    @Test
    fun testTimeRemainingUpdatesDoNotTriggerAutoExpandOnExistingNotification() = runTest {
        val repository = SmartIslandNotificationRepository()
        val autoExpandedKeys = mutableListOf<String>()
        val job = launch {
            repository.autoExpandEvent.collect { autoExpandedKeys.add(it) }
        }

        val notif1 = IslandNotification(
            key = "task_1",
            packageName = "com.example.app",
            appName = "TaskApp",
            title = "File Export",
            text = "Exporting • 5 mins remaining",
            mode = IslandMode.Notification,
            progress = 10,
            progressMax = 100,
            timeMillis = 1000L
        )
        // Initial post with progressMax > 0
        repository.postNotification(notif1, autoExpand = true)
        testScheduler.advanceUntilIdle()

        // Progress notifications must not autoExpand
        assertTrue(autoExpandedKeys.isEmpty())

        // Update with new time remaining
        val notif2 = notif1.copy(
            text = "Exporting • 4 mins remaining",
            progress = 20,
            timeMillis = 2000L
        )
        repository.postNotification(notif2, autoExpand = true)
        testScheduler.advanceUntilIdle()

        assertTrue(autoExpandedKeys.isEmpty())

        job.cancel()
    }

    @Test
    fun testIsDownloadDoneDetection() {
        val downloadInProgress = IslandNotification(
            key = "dl1",
            packageName = "com.android.chrome",
            appName = "Chrome",
            title = "movie.mp4",
            text = "Downloading... 45 MB of 100 MB",
            mode = IslandMode.DownloadUpload,
            progress = 45,
            progressMax = 100,
            timeMillis = 1000L
        )
        assertFalse(downloadInProgress.isDownloadDone)

        val downloadCompleteByProgress = downloadInProgress.copy(
            progress = 100,
            progressMax = 100
        )
        assertTrue(downloadCompleteByProgress.isDownloadDone)

        val downloadCompleteByKeywords = IslandNotification(
            key = "dl2",
            packageName = "com.android.chrome",
            appName = "Chrome",
            title = "update.zip",
            text = "Download complete",
            mode = IslandMode.DownloadUpload,
            progress = 0,
            progressMax = 0,
            timeMillis = 1000L
        )
        assertTrue(downloadCompleteByKeywords.isDownloadDone)

        val uploadCompleteByKeywords = IslandNotification(
            key = "ul1",
            packageName = "com.google.android.apps.docs",
            appName = "Google Drive",
            title = "backup.tar",
            text = "Uploaded successfully",
            mode = IslandMode.DownloadUpload,
            progress = 0,
            progressMax = 0,
            timeMillis = 1000L
        )
        assertTrue(uploadCompleteByKeywords.isDownloadDone)

        // Non-download modes should return false
        val regularNotification = downloadCompleteByProgress.copy(
            mode = IslandMode.Notification
        )
        assertFalse(regularNotification.isDownloadDone)
    }

    @Test
    fun testDemoDownloadUploadRotationIncludesCompletedDownload() {
        val repository = SmartIslandNotificationRepository()

        // 1st demo: download in progress
        repository.showDemo(IslandMode.DownloadUpload)
        val notif1 = repository.notifications.value.find { it.key == "demo_download" }
        assertNotNull(notif1)
        assertFalse(notif1!!.isDownloadDone)

        // 2nd demo: upload in progress
        repository.showDemo(IslandMode.DownloadUpload)
        val notif2 = repository.notifications.value.find { it.key == "demo_upload" }
        assertNotNull(notif2)
        assertFalse(notif2!!.isDownloadDone)

        // 3rd demo: download complete
        repository.showDemo(IslandMode.DownloadUpload)
        val notif3 = repository.notifications.value.find { it.key == "demo_download_complete" }
        assertNotNull(notif3)
        assertTrue(notif3!!.isDownloadDone)
        assertEquals(100, notif3.progress)
        assertEquals(100, notif3.progressMax)
    }

    @Test
    fun testDismissCompletedDownloadDispatchesCancelCommand() = runTest {
        val repository = SmartIslandNotificationRepository()
        val receivedCommands = mutableListOf<SmartIslandCommand>()
        val job = launch {
            repository.commands.collect { receivedCommands.add(it) }
        }

        val completedDownload = IslandNotification(
            key = "completed_dl_key",
            packageName = "com.android.chrome",
            appName = "Chrome",
            title = "setup.apk",
            text = "Download complete",
            mode = IslandMode.DownloadUpload,
            progress = 100,
            progressMax = 100,
            timeMillis = 1000L
        )
        repository.postNotification(completedDownload, autoExpand = false)
        testScheduler.advanceUntilIdle()
        assertEquals(1, repository.notifications.value.size)

        // Dismiss action: remove from repository and send cancel command
        repository.removeNotification(completedDownload.key)
        repository.sendCommand(SmartIslandCommand.CancelNotification(completedDownload.key))
        testScheduler.advanceUntilIdle()

        assertTrue(repository.notifications.value.isEmpty())
        assertEquals(1, receivedCommands.size)
        val command = receivedCommands[0] as SmartIslandCommand.CancelNotification
        assertEquals("completed_dl_key", command.key)

        job.cancel()
    }
}
