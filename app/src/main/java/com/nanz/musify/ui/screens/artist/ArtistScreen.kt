package com.nanz.musify.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nanz.musify.data.innertube.models.AlbumItem
import com.nanz.musify.data.innertube.models.ArtistItem
import com.nanz.musify.data.innertube.models.SongItem
import com.nanz.musify.ui.components.SongItemRow
import com.nanz.musify.ui.theme.*

@Composable
fun ArtistScreen(
    artist: ArtistItem,
    onBackClick: () -> Unit,
    onSongClick: (SongItem, List<SongItem>) -> Unit,
    onAlbumClick: (AlbumItem) -> Unit
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
            // Header Artis
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = artist.thumbnailUrl ?: "",
                        contentDescription = artist.name,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )

                    if (!artist.subscriberCount.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = artist.subscriberCount,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                }
            }

            // Top Songs Section
            if (artist.topSongs.isNotEmpty()) {
                item {
                    Text(
                        text = "Lagu Populer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                items(artist.topSongs) { song ->
                    SongItemRow(
                        song = song,
                        onClick = { onSongClick(song, artist.topSongs) }
                    )
                }
            }

            // Albums & Singles Section
            if (artist.topAlbums.isNotEmpty()) {
                item {
                    Text(
                        text = "Album & Single",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(artist.topAlbums) { album ->
                            Column(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable { onAlbumClick(album) }
                            ) {
                                AsyncImage(
                                    model = album.thumbnailUrl ?: "",
                                    contentDescription = album.title,
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceVariantDark),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
