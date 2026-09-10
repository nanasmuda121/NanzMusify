package com.nanz.musify.data.innertube

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nanz.musify.data.innertube.models.EnhancedLyrics
import com.nanz.musify.data.innertube.models.SyncedLyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

/**
 * Client untuk LRCLIB (Open Source Synchronized Lyrics Provider)
 * https://lrclib.net/
 */
class LrclibClient(
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val BASE_URL = "https://lrclib.net/api"
        private const val USER_AGENT = "NanzMusify Android (https://github.com/nanasmuda121/NanzMusify)"
        private val LRC_REGEX = Pattern.compile("\\[(\\d+):(\\d+(?:\\.\\d+)?)\\](.*)")
    }

    /**
     * Mengambil lirik lagu sinkron & teks dari LRCLIB
     */
    suspend fun getLyrics(trackName: String, artistName: String, durationSeconds: Long? = null): EnhancedLyrics? = withContext(Dispatchers.IO) {
        val cleanTrack = cleanTitle(trackName)
        val cleanArtist = cleanArtistName(artistName)

        val urlBuilder = "$BASE_URL/get".toHttpUrlOrNull()?.newBuilder() ?: return@withContext null
        urlBuilder.addQueryParameter("track_name", cleanTrack)
        urlBuilder.addQueryParameter("artist_name", cleanArtist)
        if (durationSeconds != null && durationSeconds > 0) {
            urlBuilder.addQueryParameter("duration", durationSeconds.toString())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val json = JsonParser.parseString(body).asJsonObject
                    return@withContext parseLrcJson(json)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback ke pencarian search jika get by exact match 404
        return@withContext searchFallback(cleanTrack, cleanArtist)
    }

    private suspend fun searchFallback(track: String, artist: String): EnhancedLyrics? = withContext(Dispatchers.IO) {
        val urlBuilder = "$BASE_URL/search".toHttpUrlOrNull()?.newBuilder() ?: return@withContext null
        urlBuilder.addQueryParameter("track_name", track)
        urlBuilder.addQueryParameter("artist_name", artist)

        val request = Request.Builder()
            .url(urlBuilder.build())
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val jsonArr = JsonParser.parseString(body).asJsonArray
                    if (jsonArr.size() > 0) {
                        val first = jsonArr.get(0).asJsonObject
                        return@withContext parseLrcJson(first)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    private fun parseLrcJson(json: JsonObject): EnhancedLyrics {
        val plainLyrics = json.get("plainLyrics")?.let { if (!it.isJsonNull) it.asString else null }
        val syncedText = json.get("syncedLyrics")?.let { if (!it.isJsonNull) it.asString else null }
        val track = json.get("trackName")?.let { if (!it.isJsonNull) it.asString else "" } ?: ""
        val artist = json.get("artistName")?.let { if (!it.isJsonNull) it.asString else "" } ?: ""

        val syncedLines = mutableListOf<SyncedLyricLine>()
        if (!syncedText.isNullOrBlank()) {
            syncedText.lines().forEach { line ->
                val matcher = LRC_REGEX.matcher(line.trim())
                if (matcher.matches()) {
                    val min = matcher.group(1)?.toLongOrNull() ?: 0L
                    val sec = matcher.group(2)?.toDoubleOrNull() ?: 0.0
                    val text = matcher.group(3)?.trim() ?: ""
                    val timeMs = ((min * 60 + sec) * 1000).toLong()
                    syncedLines.add(SyncedLyricLine(timeMs, text))
                }
            }
        }

        return EnhancedLyrics(
            trackName = track,
            artistName = artist,
            plainLyrics = plainLyrics,
            syncedLines = syncedLines,
            isSynced = syncedLines.isNotEmpty()
        )
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("(?i)\\(official.*?\\)|\\[official.*?\\]|\\(video\\)|\\[video\\]|\\(audio\\)|\\[audio\\]|\\(lyrics?\\)|\\[lyrics?\\web\\]"), "")
            .replace(Regex("(?i)ft\\..*|feat\\..*"), "")
            .trim()
    }

    private fun cleanArtistName(artist: String): String {
        return artist.split(",", "&", "ft.", "feat.", "•")[0].trim()
    }
}
