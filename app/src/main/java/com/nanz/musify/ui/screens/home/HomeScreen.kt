package com.nanz.musify.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nanz.musify.data.innertube.models.SongItem
import com.nanz.musify.ui.components.SongItemRow
import com.nanz.musify.ui.theme.*
import com.nanz.musify.ui.viewmodels.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onSongClick: (SongItem) -> Unit
) {
    val sections by viewModel.homeSections.collectAsState()
    val isLoading by viewModel.isHomeLoading.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Scaffold(
        containerColor = BackgroundDark
    ) { innerPadding ->
        if (isLoading && sections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // App Bar Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nanz",
                            style = MaterialTheme.typography.headlineLarge.copy(color = PrimaryRed)
                        )
                        Text(
                            text = "Musify",
                            style = MaterialTheme.typography.headlineLarge.copy(color = TextPrimary)
                        )
                    }
                }

                // Dynamic Sections from InnerTube
                items(sections) { section ->
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        if (section.items.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(section.items) { song ->
                                    HomeSongCard(
                                        song = song,
                                        isPlaying = playbackState.currentSong?.id == song.id,
                                        onClick = { onSongClick(song) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSongCard(
    song: SongItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = song.thumbnailUrl ?: "",
            contentDescription = song.title,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) PrimaryRed else TextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = song.artistName,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
