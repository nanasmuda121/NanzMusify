package com.nanz.musify.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.nanz.musify.data.innertube.InnerTubeClient
import com.nanz.musify.data.innertube.models.SongItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlaybackState(
    val currentSong: SongItem? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val queue: List<SongItem> = emptyList(),
    val currentIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

class PlaybackManager(
    private val context: Context,
    private val innerTubeClient: InnerTubeClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private var exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    private var progressJob: Job? = null

    init {
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.value = _playbackState.value.copy(isLoading = true)
                    }
                    Player.STATE_READY -> {
                        _playbackState.value = _playbackState.value.copy(
                            isLoading = false,
                            totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                        )
                    }
                    Player.STATE_ENDED -> {
                        playNext()
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.value = _playbackState.value.copy(isLoading = false)
                    }
                }
            }
        })
    }

    fun playSong(song: SongItem, newQueue: List<SongItem> = emptyList()) {
        val queueToUse = if (newQueue.isNotEmpty()) newQueue else listOf(song)
        val index = queueToUse.indexOfFirst { it.id == song.id }.let { if (it == -1) 0 else it }

        _playbackState.value = _playbackState.value.copy(
            currentSong = song,
            queue = queueToUse,
            currentIndex = index,
            isLoading = true
        )

        scope.launch {
            try {
                val streamInfo = innerTubeClient.getStreamInfo(song.id)
                if (streamInfo != null) {
                    val mediaItem = MediaItem.Builder()
                        .setUri(streamInfo.audioUrl)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artistName)
                                .build()
                        )
                        .build()

                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()

                    // Otomatis ambil antrean (Up Next) jika antrean saat ini cuma 1 lagu
                    if (queueToUse.size <= 1) {
                        loadUpNextQueue(song.id)
                    }
                } else {
                    _playbackState.value = _playbackState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _playbackState.value = _playbackState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadUpNextQueue(videoId: String) {
        val upNext = innerTubeClient.getQueue(videoId)
        if (upNext.isNotEmpty()) {
            val current = _playbackState.value.currentSong
            val combined = mutableListOf<SongItem>()
            if (current != null) combined.add(current)
            combined.addAll(upNext.filter { it.id != videoId })
            _playbackState.value = _playbackState.value.copy(queue = combined)
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPosition = positionMs)
    }

    fun playNext() {
        val state = _playbackState.value
        if (state.queue.isNotEmpty() && state.currentIndex + 1 < state.queue.size) {
            val nextSong = state.queue[state.currentIndex + 1]
            playSong(nextSong, state.queue)
        } else if (state.repeatMode == RepeatMode.ALL && state.queue.isNotEmpty()) {
            playSong(state.queue.first(), state.queue)
        }
    }

    fun playPrevious() {
        val state = _playbackState.value
        if (state.currentPosition > 3000) {
            seekTo(0)
        } else if (state.queue.isNotEmpty() && state.currentIndex > 0) {
            val prevSong = state.queue[state.currentIndex - 1]
            playSong(prevSong, state.queue)
        }
    }

    fun toggleShuffle() {
        val isShuffle = !_playbackState.value.isShuffle
        _playbackState.value = _playbackState.value.copy(isShuffle = isShuffle)
    }

    fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        currentPosition = exoPlayer.currentPosition
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        exoPlayer.release()
    }
}
