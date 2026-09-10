package com.nanz.musify.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nanz.musify.data.innertube.models.SongItem
import com.nanz.musify.player.RepeatMode
import com.nanz.musify.ui.components.SongItemRow
import com.nanz.musify.ui.theme.*
import com.nanz.musify.ui.viewmodels.MusicViewModel
import java.util.Locale

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onArtistClick: ((String) -> Unit)? = null
) {
    val state by viewModel.playbackState.collectAsState()
    val lyrics by viewModel.currentLyrics.collectAsState()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Now Playing, 1: Antrean, 2: Lirik

    val song = state.currentSong
    val lyricsListState = rememberLazyListState()

    // Auto-scroll lirik sinkron saat waktu lagu berjalan
    val activeLyricIndex = remember(lyrics, state.currentPosition) {
        if (lyrics != null && lyrics!!.isSynced && lyrics!!.syncedLines.isNotEmpty()) {
            val lines = lyrics!!.syncedLines
            var idx = 0
            for (i in lines.indices) {
                if (state.currentPosition >= lines[i].timeMs) {
                    idx = i
                } else {
                    break
                }
            }
            idx
        } else {
            -1
        }
    }

    LaunchedEffect(activeLyricIndex, selectedTab) {
        if (selectedTab == 2 && activeLyricIndex >= 0) {
            lyricsListState.animateScrollToItem((activeLyricIndex - 2).coerceAtLeast(0))
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Lagu") }
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Antrean (${state.queue.size})") }
                    )
                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = {
                            Text(if (lyrics?.isSynced == true) "Lirik ⚡ Sync" else "Lirik")
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Now Playing View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Big Album Art
                        AsyncImage(
                            model = song?.thumbnailUrl ?: "",
                            contentDescription = song?.title,
                            modifier = Modifier
                                .size(290.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shadow(24.dp, RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Title, Artist, and Favorite button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song?.title ?: "Tidak ada lagu",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = song?.artistName ?: "",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = TextSecondary,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (song != null) {
                                IconButton(onClick = { viewModel.toggleFavorite(song) }) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorit",
                                        tint = PrimaryRed
                                    )
                                }
                            }
                        }

                        // Progress Slider & Timers
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val duration = if (state.totalDuration > 0) state.totalDuration else 1L
                            val progress = (state.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

                            Slider(
                                value = progress,
                                onValueChange = { newProg ->
                                    val newPos = (newProg * duration).toLong()
                                    viewModel.seekTo(newPos)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryRed,
                                    activeTrackColor = PrimaryRed,
                                    inactiveTrackColor = ProgressBarBackground
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(state.currentPosition),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = formatTime(state.totalDuration),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Playback Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleShuffle() }) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (state.isShuffle) PrimaryRed else TextSecondary
                                )
                            }

                            IconButton(onClick = { viewModel.playPrevious() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Big Play / Pause Button
                            FloatingActionButton(
                                onClick = { viewModel.togglePlayPause() },
                                containerColor = PrimaryRed,
                                contentColor = TextPrimary,
                                shape = CircleShape,
                                modifier = Modifier.size(68.dp)
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        color = TextPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.playNext() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.toggleRepeat() }) {
                                Icon(
                                    imageVector = when (state.repeatMode) {
                                        RepeatMode.ONE -> Icons.Default.RepeatOne
                                        RepeatMode.ALL -> Icons.Default.Repeat
                                        RepeatMode.OFF -> Icons.Default.Repeat
                                    },
                                    contentDescription = "Repeat",
                                    tint = if (state.repeatMode != RepeatMode.OFF) PrimaryRed else TextSecondary
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Up Next Queue View
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Antrean Lagu Berikutnya (InnerTube Radio)",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        items(state.queue) { queueSong ->
                            SongItemRow(
                                song = queueSong,
                                isPlaying = queueSong.id == state.currentSong?.id,
                                onClick = { viewModel.playSong(queueSong, state.queue) }
                            )
                        }
                    }
                }
                2 -> {
                    // Synchronized Karaoke Lyrics View (LRCLIB)
                    if (isLyricsLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryRed)
                        }
                    } else if (lyrics != null && lyrics!!.isSynced && lyrics!!.syncedLines.isNotEmpty()) {
                        LazyColumn(
                            state = lyricsListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentPadding = PaddingValues(vertical = 120.dp)
                        ) {
                            itemsIndexed(lyrics!!.syncedLines) { index, line ->
                                val isActive = index == activeLyricIndex
                                val textColor by animateColorAsState(
                                    targetValue = if (isActive) PrimaryRed else TextMuted
                                )

                                Text(
                                    text = line.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = if (isActive) 22.sp else 16.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        lineHeight = if (isActive) 32.sp else 26.sp,
                                        color = textColor
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                        .clickable { viewModel.seekTo(line.timeMs) }
                                )
                            }
                        }
                    } else if (lyrics?.plainLyrics != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            item {
                                Text(
                                    text = lyrics!!.plainLyrics!!,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 17.sp,
                                        lineHeight = 28.sp,
                                        color = TextPrimary
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Lirik tidak ditemukan di database LRCLIB",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
