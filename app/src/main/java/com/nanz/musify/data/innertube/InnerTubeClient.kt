package com.nanz.musify.data.innertube

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nanz.musify.data.innertube.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * InnerTube YouTube Music API Client
 * Mengakses seluruh katalog musik, metadata, queue, search, dan direct streaming URL.
 */
class InnerTubeClient(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val BASE_URL = "https://music.youtube.com/youtubei/v1"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
        private const val CLIENT_VERSION = "1.20240901.01.00"
    }

    /**
     * Membuat Context Payload default untuk InnerTube (Web Remix & Android Music)
     */
    private fun createInnerTubeContext(clientName: String = "WEB_REMIX", clientVersion: String = CLIENT_VERSION): JsonObject {
        val client = JsonObject().apply {
            addProperty("clientName", clientName)
            addProperty("clientVersion", clientVersion)
            addProperty("hl", "id")
            addProperty("gl", "ID")
            addProperty("utcOffsetMinutes", 420)
        }
        return JsonObject().apply {
            add("client", client)
        }
    }

    /**
     * Menjalankan POST request ke endpoint InnerTube
     */
    private suspend fun post(endpoint: String, payload: JsonObject): JsonObject = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/$endpoint?prettyPrint=false"
        val requestBody = payload.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("User-Agent", USER_AGENT)
            .header("X-YouTube-Client-Name", "67")
            .header("X-YouTube-Client-Version", CLIENT_VERSION)
            .header("Referer", "https://music.youtube.com/")
            .header("Origin", "https://music.youtube.com")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("InnerTube HTTP ${response.code}: ${response.message}")
            }
            val responseBody = response.body?.string() ?: "{}"
            JsonParser.parseString(responseBody).asJsonObject
        }
    }

    /**
     * Mencari Lagu, Album, Artis, atau Playlist
     */
    suspend fun search(query: String, filter: SearchFilter = SearchFilter.ALL): SearchResult = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            add("context", createInnerTubeContext())
            addProperty("query", query)
            if (filter.value.isNotEmpty()) {
                addProperty("params", filter.value)
            }
        }

        try {
            val json = post("search", payload)
            parseSearchResults(query, json)
        } catch (e: Exception) {
            e.printStackTrace()
            SearchResult(query = query)
        }
    }

    /**
     * Mengambil Beranda YouTube Music (Explore, Charts, Moods, Quick Picks)
     */
    suspend fun getHomeFeed(): List<HomeSection> = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            add("context", createInnerTubeContext())
            addProperty("browseId", "FEmusic_home")
        }

        val sections = mutableListOf<HomeSection>()
        try {
            val json = post("browse", payload)
            val tabs = json.getAsJsonObject("contents")
                ?.getAsJsonObject("singleColumnBrowseResultsRenderer")
                ?.getAsJsonArray("tabs")

            val sectionListRenderer = tabs?.get(0)?.asJsonObject
                ?.getAsJsonObject("tabRenderer")
                ?.getAsJsonObject("content")
                ?.getAsJsonObject("sectionListRenderer")
                ?.getAsJsonArray("contents")

            sectionListRenderer?.forEach { element ->
                val sectionObj = element.asJsonObject
                val shelfRenderer = sectionObj.getAsJsonObject("musicCarouselShelfRenderer")
                    ?: sectionObj.getAsJsonObject("musicShelfRenderer")

                if (shelfRenderer != null) {
                    val title = extractText(shelfRenderer.getAsJsonObject("header")
                        ?.getAsJsonObject("musicCarouselShelfBasicHeaderRenderer")
                        ?.getAsJsonObject("title")) ?: "Rekomendasi"

                    val items = mutableListOf<SongItem>()
                    val albums = mutableListOf<AlbumItem>()

                    val contents = shelfRenderer.getAsJsonArray("contents")
                    contents?.forEach { itemElem ->
                        val itemObj = itemElem.asJsonObject
                        val twoRowItem = itemObj.getAsJsonObject("musicTwoRowItemRenderer")
                        val responsiveItem = itemObj.getAsJsonObject("musicResponsiveListItemRenderer")

                        if (twoRowItem != null) {
                            val navEndpoint = twoRowItem.getAsJsonObject("navigationEndpoint")
                            val watchEndpoint = navEndpoint?.getAsJsonObject("watchEndpoint")
                            val browseEndpoint = navEndpoint?.getAsJsonObject("browseEndpoint")

                            val itemTitle = extractText(twoRowItem.getAsJsonObject("title")) ?: "Unknown"
                            val subtitle = extractText(twoRowItem.getAsJsonObject("subtitle")) ?: ""
                            val thumb = extractThumbnail(twoRowItem.getAsJsonObject("thumbnailRenderer"))

                            if (watchEndpoint != null) {
                                val videoId = watchEndpoint.get("videoId")?.asString ?: ""
                                items.add(
                                    SongItem(
                                        id = videoId,
                                        title = itemTitle,
                                        artistName = subtitle,
                                        thumbnailUrl = thumb
                                    )
                                )
                            } else if (browseEndpoint != null) {
                                val browseId = browseEndpoint.get("browseId")?.asString ?: ""
                                albums.add(
                                    AlbumItem(
                                        id = browseId,
                                        title = itemTitle,
                                        artistName = subtitle,
                                        thumbnailUrl = thumb
                                    )
                                )
                            }
                        } else if (responsiveItem != null) {
                            val song = parseResponsiveListItem(responsiveItem)
                            if (song != null) items.add(song)
                        }
                    }

                    if (items.isNotEmpty() || albums.isNotEmpty()) {
                        sections.add(HomeSection(title = title, items = items, albums = albums))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sections
    }

    /**
     * Mendapatkan Stream URL Audio Langsung (Android Music Client Endpoint)
     */
    suspend fun getStreamInfo(videoId: String): StreamInfo? = withContext(Dispatchers.IO) {
        val androidContext = JsonObject().apply {
            val client = JsonObject().apply {
                addProperty("clientName", "ANDROID_MUSIC")
                addProperty("clientVersion", "6.20.51")
                addProperty("androidSdkVersion", 34)
                addProperty("hl", "id")
                addProperty("gl", "ID")
            }
            add("client", client)
        }

        val payload = JsonObject().apply {
            add("context", androidContext)
            addProperty("videoId", videoId)
            addProperty("contentCheckOk", true)
            addProperty("racyCheckOk", true)
        }

        try {
            val json = post("player", payload)
            val streamingData = json.getAsJsonObject("streamingData") ?: return@withContext null
            val adaptiveFormats = streamingData.getAsJsonArray("adaptiveFormats") ?: streamingData.getAsJsonArray("formats")

            var bestUrl: String? = null
            var bestBitrate = 0
            var mime = "audio/mp4"

            adaptiveFormats?.forEach { formatElem ->
                val formatObj = formatElem.asJsonObject
                val mimeType = formatObj.get("mimeType")?.asString ?: ""
                if (mimeType.startsWith("audio/")) {
                    val bitrate = formatObj.get("bitrate")?.asInt ?: 0
                    val url = formatObj.get("url")?.asString

                    if (url != null && bitrate > bestBitrate) {
                        bestBitrate = bitrate
                        bestUrl = url
                        mime = mimeType
                    }
                }
            }

            if (bestUrl != null) {
                StreamInfo(
                    videoId = videoId,
                    audioUrl = bestUrl!!,
                    bitrate = bestBitrate,
                    mimeType = mime
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mengambil Antrean Lagu / Up Next (Radio / Related Tracks)
     */
    suspend fun getQueue(videoId: String, playlistId: String? = null): List<SongItem> = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            add("context", createInnerTubeContext())
            addProperty("videoId", videoId)
            if (playlistId != null) {
                addProperty("playlistId", playlistId)
            }
            addProperty("isAudioOnly", true)
        }

        val list = mutableListOf<SongItem>()
        try {
            val json = post("next", payload)
            val tabs = json.getAsJsonObject("contents")
                ?.getAsJsonObject("singleColumnMusicWatchNextResultsRenderer")
                ?.getAsJsonObject("tabbedRenderer")
                ?.getAsJsonObject("watchNextTabbedResultsRenderer")
                ?.getAsJsonArray("tabs")

            val queueTab = tabs?.get(0)?.asJsonObject
                ?.getAsJsonObject("tabRenderer")
                ?.getAsJsonObject("content")
                ?.getAsJsonObject("musicQueueRenderer")
                ?.getAsJsonObject("content")
                ?.getAsJsonObject("playlistPanelRenderer")
                ?.getAsJsonArray("contents")

            queueTab?.forEach { itemElem ->
                val renderer = itemElem.asJsonObject.getAsJsonObject("playlistPanelVideoRenderer")
                if (renderer != null) {
                    val songId = renderer.get("videoId")?.asString ?: ""
                    val title = extractText(renderer.getAsJsonObject("title")) ?: "Unknown"
                    val artist = extractText(renderer.getAsJsonObject("shortBylineText")) ?: "Artis"
                    val duration = extractText(renderer.getAsJsonObject("lengthText")) ?: "0:00"
                    val thumb = extractThumbnail(renderer.getAsJsonObject("thumbnail"))

                    list.add(
                        SongItem(
                            id = songId,
                            title = title,
                            artistName = artist,
                            durationText = duration,
                            thumbnailUrl = thumb
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    /**
     * Mengambil Lirik Lagu
     */
    suspend fun getLyrics(browseId: String): LyricsItem? = withContext(Dispatchers.IO) {
        val payload = JsonObject().apply {
            add("context", createInnerTubeContext())
            addProperty("browseId", browseId)
        }

        try {
            val json = post("browse", payload)
            val section = json.getAsJsonObject("contents")
                ?.getAsJsonObject("sectionListRenderer")
                ?.getAsJsonArray("contents")
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("musicDescriptionShelfRenderer")

            val plainText = extractText(section?.getAsJsonObject("description"))
            if (!plainText.isNullOrBlank()) {
                LyricsItem(plainLyrics = plainText, isSynced = false)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Helper Parsers ---

    private fun parseSearchResults(query: String, json: JsonObject): SearchResult {
        val songs = mutableListOf<SongItem>()
        val albums = mutableListOf<AlbumItem>()
        val artists = mutableListOf<ArtistItem>()

        val contents = json.getAsJsonObject("contents")
            ?.getAsJsonObject("tabbedSearchResultsRenderer")
            ?.getAsJsonArray("tabs")
            ?.get(0)?.asJsonObject
            ?.getAsJsonObject("tabRenderer")
            ?.getAsJsonObject("content")
            ?.getAsJsonObject("sectionListRenderer")
            ?.getAsJsonArray("contents")

        contents?.forEach { sectionElem ->
            val shelfRenderer = sectionElem.asJsonObject.getAsJsonObject("musicShelfRenderer")
            val items = shelfRenderer?.getAsJsonArray("contents")

            items?.forEach { itemElem ->
                val responsiveItem = itemElem.asJsonObject.getAsJsonObject("musicResponsiveListItemRenderer")
                if (responsiveItem != null) {
                    val song = parseResponsiveListItem(responsiveItem)
                    if (song != null) {
                        songs.add(song)
                    }
                }
            }
        }

        return SearchResult(query = query, songs = songs, albums = albums, artists = artists)
    }

    private fun parseResponsiveListItem(item: JsonObject): SongItem? {
        val flexColumns = item.getAsJsonArray("flexColumns") ?: return null
        if (flexColumns.size() < 2) return null

        val col1 = flexColumns.get(0).asJsonObject
            .getAsJsonObject("musicResponsiveListItemFlexColumnRenderer")
            ?.getAsJsonObject("text")
        val title = extractText(col1) ?: return null

        val col2 = flexColumns.get(1).asJsonObject
            .getAsJsonObject("musicResponsiveListItemFlexColumnRenderer")
            ?.getAsJsonObject("text")
        val artist = extractText(col2) ?: "Artis"

        val navEndpoint = item.getAsJsonObject("playlistItemData")
        val videoId = navEndpoint?.get("videoId")?.asString
            ?: item.getAsJsonObject("overlay")
                ?.getAsJsonObject("musicItemThumbnailOverlayRenderer")
                ?.getAsJsonObject("content")
                ?.getAsJsonObject("musicPlayButtonRenderer")
                ?.getAsJsonObject("playNavigationEndpoint")
                ?.getAsJsonObject("watchEndpoint")
                ?.get("videoId")?.asString
            ?: ""

        if (videoId.isEmpty()) return null

        val thumbnail = extractThumbnail(item.getAsJsonObject("thumbnail"))

        return SongItem(
            id = videoId,
            title = title,
            artistName = artist,
            thumbnailUrl = thumbnail
        )
    }

    private fun extractText(textObj: JsonObject?): String? {
        if (textObj == null) return null
        val simpleText = textObj.get("simpleText")?.asString
        if (simpleText != null) return simpleText

        val runs = textObj.getAsJsonArray("runs")
        if (runs != null && runs.size() > 0) {
            val sb = StringBuilder()
            runs.forEach { sb.append(it.asJsonObject.get("text")?.asString ?: "") }
            return sb.toString()
        }
        return null
    }

    private fun extractThumbnail(thumbnailContainer: JsonObject?): String? {
        if (thumbnailContainer == null) return null
        val thumbnails = thumbnailContainer.getAsJsonObject("musicThumbnailRenderer")
            ?.getAsJsonObject("thumbnail")
            ?.getAsJsonArray("thumbnails")
            ?: thumbnailContainer.getAsJsonObject("croppedSquareThumbnailRenderer")
                ?.getAsJsonObject("thumbnail")
                ?.getAsJsonArray("thumbnails")
            ?: thumbnailContainer.getAsJsonArray("thumbnails")

        if (thumbnails != null && thumbnails.size() > 0) {
            val last = thumbnails.get(thumbnails.size() - 1).asJsonObject
            return last.get("url")?.asString
        }
        return null
    }
}
