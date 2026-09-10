package com.nanz.musify.ui.screens.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nanz.musify.data.innertube.models.AlbumItem
import com.nanz.musify.data.innertube.models.SongItem
import com.nanz.musify.ui.components.SongItemRow
import com.nanz.musify.ui.theme.*

@Composable
fun AlbumScreen(
    album: AlbumItem,
    onBackClick: () -> Unit,
    onSongClick: (SongItem, List<SongItem>) -> Unit,
    onPlayAllClick: (List<SongItem>) -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Album Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = album.thumbnailUrl ?: "",
                        contentDescription = album.title,
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceVariantDark),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = album.artistName,
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Play All Button
                    if (album.songs.isNotEmpty()) {
                        Button(
                            onClick = { onPlayAllClick(album.songs) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(0.6f)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Putar Semua (${album.songs.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tracklist
            itemsIndexed(album.songs) { index, song ->
                SongItemRow(
                    song = song,
                    onClick = { onSongClick(song, album.songs) }
                )
            }
        }
    }
}
