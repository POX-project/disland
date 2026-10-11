/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.service.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcLibLyricsServiceTest {

    @Test
    fun testParseLrcStandardTwoDigitFractions() {
        val lrc = """
            [00:12.34] First line of the song
            [00:15.80] Second line follows
            [01:02.10] Chorus starts here
        """.trimIndent()

        val lines = LrcLibLyricsService.parseLrc(lrc)
        assertEquals(3, lines.size)

        assertEquals(12340L, lines[0].timeMs)
        assertEquals("First line of the song", lines[0].text)

        assertEquals(15800L, lines[1].timeMs)
        assertEquals("Second line follows", lines[1].text)

        assertEquals(62100L, lines[2].timeMs)
        assertEquals("Chorus starts here", lines[2].text)
    }

    @Test
    fun testParseLrcThreeDigitFractions() {
        val lrc = """
            [00:05.123] Intro begins
            [00:10.500] Beat drops
        """.trimIndent()

        val lines = LrcLibLyricsService.parseLrc(lrc)
        assertEquals(2, lines.size)

        assertEquals(5123L, lines[0].timeMs)
        assertEquals("Intro begins", lines[0].text)

        assertEquals(10500L, lines[1].timeMs)
        assertEquals("Beat drops", lines[1].text)
    }

    @Test
    fun testParseLrcEmptyOrInvalid() {
        assertTrue(LrcLibLyricsService.parseLrc("").isEmpty())
        assertTrue(LrcLibLyricsService.parseLrc("   ").isEmpty())
        assertTrue(LrcLibLyricsService.parseLrc("Some random text without timestamps").isEmpty())
    }

    @Test
    fun testGetCurrentLineIndex() {
        val lines = listOf(
            LyricLine(10000L, "Line 1"),
            LyricLine(20000L, "Line 2"),
            LyricLine(35000L, "Line 3")
        )

        // Before first line
        assertEquals(-1, LrcLibLyricsService.getCurrentLineIndex(lines, 5000L))

        // Exactly on line 1
        assertEquals(0, LrcLibLyricsService.getCurrentLineIndex(lines, 10000L))

        // Between line 1 and line 2
        assertEquals(0, LrcLibLyricsService.getCurrentLineIndex(lines, 15000L))

        // Exactly on line 2
        assertEquals(1, LrcLibLyricsService.getCurrentLineIndex(lines, 20000L))

        // Between line 2 and line 3
        assertEquals(1, LrcLibLyricsService.getCurrentLineIndex(lines, 28000L))

        // Exactly on line 3
        assertEquals(2, LrcLibLyricsService.getCurrentLineIndex(lines, 35000L))

        // Well past line 3
        assertEquals(2, LrcLibLyricsService.getCurrentLineIndex(lines, 60000L))

        // Empty lines list
        assertEquals(-1, LrcLibLyricsService.getCurrentLineIndex(emptyList(), 15000L))
    }
}
