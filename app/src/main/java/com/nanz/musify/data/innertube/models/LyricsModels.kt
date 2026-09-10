package com.nanz.musify.data.innertube.models

data class SyncedLyricLine(
    val timeMs: Long,
    val text: String
)

data class EnhancedLyrics(
    val id: Long = 0,
    val trackName: String = "",
    val artistName: String = "",
    val albumName: String? = null,
    val durationSeconds: Double = 0.0,
    val plainLyrics: String? = null,
    val syncedLines: List<SyncedLyricLine> = emptyList(),
    val isSynced: Boolean = false
)
