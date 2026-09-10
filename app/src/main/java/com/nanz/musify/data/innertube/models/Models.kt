package com.nanz.musify.data.innertube.models

import com.google.gson.annotations.SerializedName

/**
 * Representasi lagu dalam NanzMusify
 */
data class SongItem(
    val id: String,
    val title: String,
    val artistName: String,
    val artistId: String? = null,
    val albumName: String? = null,
    val albumId: String? = null,
    val durationText: String? = null,
    val durationSeconds: Long = 0L,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
    val isExplicit: Boolean = false
)

/**
 * Representasi Album
 */
data class AlbumItem(
    val id: String,
    val title: String,
    val artistName: String,
    val artistId: String? = null,
    val year: String? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int = 0,
    val songs: List<SongItem> = emptyList()
)

/**
 * Representasi Artis
 */
data class ArtistItem(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val subscriberCount: String? = null,
    val description: String? = null,
    val topSongs: List<SongItem> = emptyList(),
    val topAlbums: List<AlbumItem> = emptyList()
)

/**
 * Section untuk Home (Explore / Trending / Moods)
 */
data class HomeSection(
    val title: String,
    val subtitle: String? = null,
    val items: List<SongItem> = emptyList(),
    val albums: List<AlbumItem> = emptyList()
)

/**
 * Tipe filter pencarian InnerTube
 */
enum class SearchFilter(val value: String) {
    ALL(""),
    SONGS("EgWKAQIIAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"),
    VIDEOS("EgWKAQIQAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"),
    ALBUMS("EgWKAQIYAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"),
    ARTISTS("EgWKAQIgAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"),
    PLAYLISTS("EgWKAQIoAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D")
}

/**
 * Hasil pencarian InnerTube
 */
data class SearchResult(
    val query: String,
    val songs: List<SongItem> = emptyList(),
    val albums: List<AlbumItem> = emptyList(),
    val artists: List<ArtistItem> = emptyList()
)

/**
 * Data Lirik Lagu
 */
data class LyricsItem(
    val plainLyrics: String? = null,
    val lines: List<LyricLine> = emptyList(),
    val isSynced: Boolean = false
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

/**
 * Model Stream Audio dari InnerTube Player API
 */
data class StreamInfo(
    val videoId: String,
    val audioUrl: String,
    val bitrate: Int = 128000,
    val mimeType: String = "audio/mp4",
    val contentLength: Long = 0L,
    val expirySeconds: Long = 21600L
)
