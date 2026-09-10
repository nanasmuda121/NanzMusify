package com.nanz.musify.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Cari", Icons.Default.Search)
    object Library : Screen("library", "Koleksi", Icons.Default.LibraryMusic)
    object Player : Screen("player", "Pemutar")
    object Lyrics : Screen("lyrics", "Lirik")
}
