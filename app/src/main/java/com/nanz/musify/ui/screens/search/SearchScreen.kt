package com.nanz.musify.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nanz.musify.data.innertube.models.*
import com.nanz.musify.ui.components.SongItemRow
import com.nanz.musify.ui.theme.*
import com.nanz.musify.ui.viewmodels.MusicViewModel

@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onSongClick: (SongItem) -> Unit,
    onArtistClick: (ArtistItem) -> Unit,
    onAlbumClick: (AlbumItem) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.searchFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    val filterOptions = listOf(
        Pair("Semua", SearchFilter.ALL),
        Pair("Lagu", SearchFilter.SONGS),
        Pair("Artis", SearchFilter.ARTISTS),
        Pair("Album", SearchFilter.ALBUMS),
        Pair("Playlist", SearchFilter.PLAYLISTS)
    )

    Scaffold(
        containerColor = BackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            TextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = {
                    Text(
                        text = "Cari lagu, artis, atau album...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { (label, filter) ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.onFilterSelected(filter) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryRed,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            } else if (searchResults != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Artists Section
                    if (searchResults!!.artists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artis",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchResults!!.artists) { artist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onArtistClick(artist) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = artist.thumbnailUrl ?: "",
                                    contentDescription = artist.name,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceVariantDark),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = artist.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp))
                                    Text(text = "Artis YouTube Music", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 12.sp))
                                }
                            }
                        }
                    }

                    // Albums Section
                    if (searchResults!!.albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Album & Playlist",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchResults!!.albums) { album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAlbumClick(album) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = album.thumbnailUrl ?: "",
                                    contentDescription = album.title,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantDark),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = album.title, style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp))
                                    Text(text = album.artistName, style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 12.sp))
                                }
                            }
                        }
                    }

                    // Songs Section
                    if (searchResults!!.songs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Lagu",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchResults!!.songs) { song ->
                            SongItemRow(
                                song = song,
                                isPlaying = playbackState.currentSong?.id == song.id,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { viewModel.toggleFavorite(song) }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cari lagu, artis, atau album dari YouTube Music",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
