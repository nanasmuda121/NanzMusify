package com.nanz.musify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nanz.musify.ui.components.MiniPlayer
import com.nanz.musify.ui.navigation.Screen
import com.nanz.musify.ui.screens.home.HomeScreen
import com.nanz.musify.ui.screens.library.LibraryScreen
import com.nanz.musify.ui.screens.player.PlayerScreen
import com.nanz.musify.ui.screens.search.SearchScreen
import com.nanz.musify.ui.theme.BackgroundDark
import com.nanz.musify.ui.theme.NanzMusifyTheme
import com.nanz.musify.ui.theme.PrimaryRed
import com.nanz.musify.ui.theme.SurfaceDark
import com.nanz.musify.ui.theme.TextMuted
import com.nanz.musify.ui.viewmodels.MusicViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as NanzMusifyApp
        val repository = app.repository
        val playbackManager = app.playbackManager

        setContent {
            NanzMusifyTheme {
                val viewModel: MusicViewModel = viewModel {
                    MusicViewModel(repository, playbackManager)
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val playbackState by viewModel.playbackState.collectAsState()
                var isPlayerExpanded by remember { mutableStateOf(false) }

                val bottomNavItems = listOf(
                    Screen.Home,
                    Screen.Search,
                    Screen.Library
                )

                Scaffold(
                    containerColor = BackgroundDark,
                    bottomBar = {
                        if (!isPlayerExpanded) {
                            Column {
                                // Floating Mini Player
                                if (playbackState.currentSong != null) {
                                    val duration = if (playbackState.totalDuration > 0) playbackState.totalDuration else 1L
                                    val progress = (playbackState.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

                                    MiniPlayer(
                                        song = playbackState.currentSong,
                                        isPlaying = playbackState.isPlaying,
                                        progress = progress,
                                        onPlayPauseClick = { viewModel.togglePlayPause() },
                                        onClick = { isPlayerExpanded = true },
                                        onNextClick = { viewModel.playNext() }
                                    )
                                }

                                NavigationBar(
                                    containerColor = SurfaceDark,
                                    tonalElevation = 0.dp
                                ) {
                                    bottomNavItems.forEach { screen ->
                                        val selected = currentRoute == screen.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                if (currentRoute != screen.route) {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(Screen.Home.route) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = {
                                                screen.icon?.let { icon ->
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = screen.title
                                                    )
                                                }
                                            },
                                            label = { Text(screen.title) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = PrimaryRed,
                                                selectedTextColor = PrimaryRed,
                                                unselectedIconColor = TextMuted,
                                                unselectedTextColor = TextMuted,
                                                indicatorColor = SurfaceDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onSongClick = { song -> viewModel.playSong(song) }
                                )
                            }
                            composable(Screen.Search.route) {
                                SearchScreen(
                                    viewModel = viewModel,
                                    onSongClick = { song -> viewModel.playSong(song) }
                                )
                            }
                            composable(Screen.Library.route) {
                                LibraryScreen(
                                    viewModel = viewModel,
                                    onSongClick = { song -> viewModel.playSong(song) }
                                )
                            }
                        }

                        // Fullscreen Player Animation
                        AnimatedVisibility(
                            visible = isPlayerExpanded,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            PlayerScreen(
                                viewModel = viewModel,
                                onBackClick = { isPlayerExpanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
