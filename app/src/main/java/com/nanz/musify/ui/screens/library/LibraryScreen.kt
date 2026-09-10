package com.nanz.musify.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nanz.musify.data.innertube.models.SongItem
import com.nanz.musify.ui.components.SongItemRow
import com.nanz.musify.ui.theme.BackgroundDark
import com.nanz.musify.ui.theme.PrimaryRed
import com.nanz.musify.ui.theme.TextMuted
import com.nanz.musify.ui.viewmodels.MusicViewModel

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onSongClick: (SongItem) -> Unit
) {
    val favorites by viewModel.favoriteSongs.collectAsState()
    val history by viewModel.historySongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Favorit, 1: Riwayat

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Koleksi Saya",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BackgroundDark,
                    contentColor = PrimaryRed
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Favorit (${favorites.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Riwayat Putar (${history.size})") }
                    )
                }
            }
        }
    ) { innerPadding ->
        val list = if (selectedTab == 0) favorites else history

        if (list.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Belum ada lagu favorit" else "Belum ada riwayat putar",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(list) { song ->
                    SongItemRow(
                        song = song,
                        isPlaying = playbackState.currentSong?.id == song.id,
                        onClick = { onSongClick(song) },
                        onFavoriteClick = { viewModel.toggleFavorite(song) }
                    )
                }
            }
        }
    }
}
