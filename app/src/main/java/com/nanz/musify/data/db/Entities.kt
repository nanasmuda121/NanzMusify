package com.nanz.musify.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nanz.musify.data.innertube.models.SongItem

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistName: String,
    val albumName: String? = null,
    val durationText: String? = null,
    val thumbnailUrl: String? = null,
    val isFavorite: Boolean = false,
    val lastPlayedTime: Long? = null
) {
    fun toSongItem(): SongItem = SongItem(
        id = id,
        title = title,
        artistName = artistName,
        albumName = albumName,
        durationText = durationText,
        thumbnailUrl = thumbnailUrl
    )

    companion object {
        fun fromSongItem(song: SongItem, isFavorite: Boolean = false, lastPlayedTime: Long? = null) = SongEntity(
            id = song.id,
            title = song.title,
            artistName = song.artistName,
            albumName = song.albumName,
            durationText = song.durationText,
            thumbnailUrl = song.thumbnailUrl,
            isFavorite = isFavorite,
            lastPlayedTime = lastPlayedTime
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
