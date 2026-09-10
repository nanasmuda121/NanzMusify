package com.nanz.musify.data.repository

import com.nanz.musify.data.db.SongDao
import com.nanz.musify.data.db.SongEntity
import com.nanz.musify.data.innertube.InnerTubeClient
import com.nanz.musify.data.innertube.LrclibClient
import com.nanz.musify.data.innertube.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val innerTubeClient: InnerTubeClient,
    private val lrclibClient: LrclibClient,
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

    suspend fun getArtist(channelId: String): ArtistItem? {
        return innerTubeClient.getArtist(channelId)
    }

    suspend fun getAlbumOrPlaylist(browseId: String): AlbumItem? {
        return innerTubeClient.getAlbumOrPlaylist(browseId)
    }

    suspend fun getLyrics(trackName: String, artistName: String, durationSeconds: Long? = null): EnhancedLyrics? {
        return lrclibClient.getLyrics(trackName, artistName, durationSeconds)
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
