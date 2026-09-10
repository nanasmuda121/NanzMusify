package com.nanz.musify.data.repository

import com.nanz.musify.data.db.SongDao
import com.nanz.musify.data.db.SongEntity
import com.nanz.musify.data.innertube.InnerTubeClient
import com.nanz.musify.data.innertube.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val innerTubeClient: InnerTubeClient,
    private val songDao: SongDao
) {
    suspend fun getHome(): List<HomeSection> {
        return innerTubeClient.getHomeFeed()
    }

    suspend fun search(query: String, filter: SearchFilter = SearchFilter.ALL): SearchResult {
        return innerTubeClient.search(query, filter)
    }

    suspend fun getStreamInfo(videoId: String): StreamInfo? {
        return innerTubeClient.getStreamInfo(videoId)
    }

    suspend fun getQueue(videoId: String): List<SongItem> {
        return innerTubeClient.getQueue(videoId)
    }

    suspend fun getLyrics(browseId: String): LyricsItem? {
        return innerTubeClient.getLyrics(browseId)
    }

    fun getFavoriteSongs(): Flow<List<SongItem>> {
        return songDao.getFavoriteSongs().map { list -> list.map { it.toSongItem() } }
    }

    fun getHistorySongs(): Flow<List<SongItem>> {
        return songDao.getHistorySongs().map { list -> list.map { it.toSongItem() } }
    }

    suspend fun toggleFavorite(song: SongItem) {
        val existing = songDao.getSongById(song.id)
        if (existing != null) {
            songDao.setFavorite(song.id, !existing.isFavorite)
        } else {
            songDao.insertSong(SongEntity.fromSongItem(song, isFavorite = true))
        }
    }

    suspend fun recordPlayed(song: SongItem) {
        val existing = songDao.getSongById(song.id)
        if (existing != null) {
            songDao.updateLastPlayed(song.id)
        } else {
            songDao.insertSong(SongEntity.fromSongItem(song, isFavorite = false, lastPlayedTime = System.currentTimeMillis()))
        }
    }
}
