/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.service.lyrics

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class SongLyrics(
    val id: Long = 0,
    val trackName: String,
    val artistName: String,
    val albumName: String? = null,
    val durationSeconds: Double = 0.0,
    val isInstrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLines: List<LyricLine> = emptyList()
)

object LrcLibLyricsService {
    private const val TAG = "LrcLibLyrics"
    private const val BASE_URL = "https://lrclib.net/api"
    private const val USER_AGENT = "SmartIsland/6.0 (https://github.com/agupta07505/SmartIsland)"

    // In-memory cache keyed by "track|artist"
    private val lyricsCache = ConcurrentHashMap<String, SongLyrics>()
    private val prefetchScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun prefetch(
        trackName: String,
        artistName: String,
        albumName: String? = null,
        durationSeconds: Double? = null
    ) {
        if (trackName.isBlank()) return
        val cacheKey = "${trackName.trim().lowercase()}|${artistName.trim().lowercase()}"
        if (lyricsCache.containsKey(cacheKey)) return
        prefetchScope.launch {
            getLyrics(trackName, artistName, albumName, durationSeconds)
        }
    }

    suspend fun getLyrics(
        trackName: String,
        artistName: String,
        albumName: String? = null,
        durationSeconds: Double? = null
    ): SongLyrics? = withContext(Dispatchers.IO) {
        if (trackName.isBlank()) return@withContext null
        val rawKey = "${trackName.trim().lowercase()}|${artistName.trim().lowercase()}"
        lyricsCache[rawKey]?.let { return@withContext it }

        val cleanTrack = cleanTitle(trackName)
        val cleanArtist = cleanArtist(artistName)
        val cleanKey = "${cleanTrack.lowercase()}|${cleanArtist.lowercase()}"
        lyricsCache[cleanKey]?.let { return@withContext it }

        try {
            // Concurrent fast fetch: run exact match and search query concurrently
            val exactDeferred = async {
                fetchExact(cleanTrack, cleanArtist, albumName, duration = null)
            }
            val searchDeferred = async {
                fetchSearch(cleanTrack, cleanArtist)
            }

            val exactResult = exactDeferred.await()
            if (exactResult != null && exactResult.syncedLines.isNotEmpty()) {
                lyricsCache[rawKey] = exactResult
                lyricsCache[cleanKey] = exactResult
                return@withContext exactResult
            }

            val searchResult = searchDeferred.await()
            val finalResult = when {
                searchResult != null && searchResult.syncedLines.isNotEmpty() -> searchResult
                exactResult != null -> exactResult
                searchResult != null -> searchResult
                else -> null
            }

            if (finalResult != null) {
                lyricsCache[rawKey] = finalResult
                lyricsCache[cleanKey] = finalResult
            }
            finalResult
        } catch (e: Exception) {
            // No track/artist in log — avoids PII leak. Feature unchanged (still returns null).
            Log.w(TAG, "Failed to fetch lyrics: ${e.message}")
            null
        }
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("\\s*[\\[(](feat\\.|ft\\.|remastered|version|live|official|audio|deluxe|lyrics|music video).*?[\\])]", RegexOption.IGNORE_CASE), "")
            .replace(Regex(" - .*$"), "")
            .trim()
    }

    private fun cleanArtist(artist: String): String {
        return artist.split(",", "&", "feat.", "ft.", ";")[0].trim()
    }

    private fun readCapped(conn: HttpURLConnection, maxBytes: Int = 512 * 1024): String {
        // Feature-preserving: normal lyrics JSON is a few KB. Cap prevents OOM only.
        conn.inputStream.bufferedReader().use { reader ->
            val sb = StringBuilder()
            val buf = CharArray(8192)
            var total = 0
            while (true) {
                val n = reader.read(buf)
                if (n < 0) break
                total += n
                if (total > maxBytes) throw IllegalStateException("Lyrics response too large")
                sb.append(buf, 0, n)
            }
            return sb.toString()
        }
    }

    private fun fetchExact(track: String, artist: String, album: String?, duration: Double?): SongLyrics? {
        val queryParams = StringBuilder("track_name=${URLEncoder.encode(track, "UTF-8")}&artist_name=${URLEncoder.encode(artist, "UTF-8")}")
        if (!album.isNullOrBlank()) {
            queryParams.append("&album_name=${URLEncoder.encode(album, "UTF-8")}")
        }
        if (duration != null && duration > 0) {
            queryParams.append("&duration=${duration.toInt()}")
        }

        var conn: HttpURLConnection? = null
        return try {
            val url = URL("$BASE_URL/get?$queryParams")
            require(url.protocol == "https" && url.host == "lrclib.net") { "Unexpected lyrics host" }
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2500
                readTimeout = 2500
                setRequestProperty("User-Agent", USER_AGENT)
                instanceFollowRedirects = false
            }

            if (conn.responseCode == 200) {
                val response = readCapped(conn)
                parseSongLyricsJson(JSONObject(response))
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchSearch(track: String, artist: String): SongLyrics? {
        val query = "$track $artist"
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("$BASE_URL/search?q=${URLEncoder.encode(query, "UTF-8")}")
            require(url.protocol == "https" && url.host == "lrclib.net") { "Unexpected lyrics host" }
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2500
                readTimeout = 2500
                setRequestProperty("User-Agent", USER_AGENT)
                instanceFollowRedirects = false
            }

            if (conn.responseCode == 200) {
                val response = readCapped(conn)
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val lyrics = parseSongLyricsJson(item)
                    if (lyrics.syncedLines.isNotEmpty()) {
                        return lyrics
                    }
                }
                if (array.length() > 0) {
                    return parseSongLyricsJson(array.getJSONObject(0))
                }
            }
            null
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun parseSongLyricsJson(json: JSONObject): SongLyrics {
        val id = json.optLong("id", 0)
        val track = json.optString("trackName", "")
        val artist = json.optString("artistName", "")
        val album = json.optString("albumName", null)
        val duration = json.optDouble("duration", 0.0)
        val instrumental = json.optBoolean("instrumental", false)
        val plainLyrics = json.optString("plainLyrics", null)
        val rawSynced = json.optString("syncedLyrics", "")

        val lines = parseLrc(rawSynced)
        return SongLyrics(
            id = id,
            trackName = track,
            artistName = artist,
            albumName = album,
            durationSeconds = duration,
            isInstrumental = instrumental,
            plainLyrics = plainLyrics,
            syncedLines = lines
        )
    }

    fun parseLrc(lrcText: String): List<LyricLine> {
        if (lrcText.isBlank()) return emptyList()
        val list = mutableListOf<LyricLine>()
        val lrcRegex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

        lrcText.lines().forEach { line ->
            val match = lrcRegex.matchEntire(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val fracStr = match.groupValues[3]
                val fracMs = if (fracStr.length == 2) {
                    (fracStr.toLongOrNull() ?: 0L) * 10
                } else {
                    fracStr.toLongOrNull() ?: 0L
                }
                val text = match.groupValues[4].trim()
                val timestampMs = min * 60_000L + sec * 1_000L + fracMs
                if (text.isNotBlank()) {
                    list.add(LyricLine(timeMs = timestampMs, text = text))
                }
            }
        }
        return list.sortedBy { it.timeMs }
    }

    fun getCurrentLineIndex(lines: List<LyricLine>, currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var index = -1
        for (i in lines.indices) {
            if (currentPositionMs >= lines[i].timeMs) {
                index = i
            } else {
                break
            }
        }
        return index
    }
}
