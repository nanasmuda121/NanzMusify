package com.nanz.musify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanz.musify.data.innertube.models.*
import com.nanz.musify.data.repository.MusicRepository
import com.nanz.musify.player.PlaybackManager
import com.nanz.musify.player.PlaybackState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: MusicRepository,
    val playbackManager: PlaybackManager
) : ViewModel() {

    private val _homeSections = MutableStateFlow<List<HomeSection>>(emptyList())
    val homeSections: StateFlow<List<HomeSection>> = _homeSections.asStateFlow()

    private val _isHomeLoading = MutableStateFlow(false)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<SearchResult?>(null)
    val searchResults: StateFlow<SearchResult?> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _currentLyrics = MutableStateFlow<EnhancedLyrics?>(null)
    val currentLyrics: StateFlow<EnhancedLyrics?> = _currentLyrics.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private val _selectedArtist = MutableStateFlow<ArtistItem?>(null)
    val selectedArtist: StateFlow<ArtistItem?> = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<AlbumItem?>(null)
    val selectedAlbum: StateFlow<AlbumItem?> = _selectedAlbum.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    val favoriteSongs: StateFlow<List<SongItem>> = repository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historySongs: StateFlow<List<SongItem>> = repository.getHistorySongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJob: Job? = null

    init {
        loadHomeFeed()
        observeCurrentSongForLyrics()
    }

    private fun observeCurrentSongForLyrics() {
        viewModelScope.launch {
            playbackState.collect { state ->
                val song = state.currentSong
                if (song != null) {
                    loadLyricsForSong(song)
                }
            }
        }
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _isHomeLoading.value = true
            try {
                val sections = repository.getHome()
                _homeSections.value = sections
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isHomeLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        triggerSearch(newQuery, _searchFilter.value)
    }

    fun onFilterSelected(filter: SearchFilter) {
        _searchFilter.value = filter
        if (_searchQuery.value.isNotBlank()) {
            triggerSearch(_searchQuery.value, filter)
        }
    }

    private fun triggerSearch(query: String, filter: SearchFilter) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        searchJob = viewModelScope.launch {
            delay(400) // Debounce
            _isSearching.value = true
            try {
                val results = repository.search(query, filter)
                _searchResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun playSong(song: SongItem, queue: List<SongItem> = emptyList()) {
        playbackManager.playSong(song, queue)
        viewModelScope.launch {
            repository.recordPlayed(song)
        }
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun playNext() = playbackManager.playNext()
    fun playPrevious() = playbackManager.playPrevious()
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun toggleRepeat() = playbackManager.toggleRepeat()

    fun toggleFavorite(song: SongItem) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    private fun loadLyricsForSong(song: SongItem) {
        viewModelScope.launch {
            _isLyricsLoading.value = true
            try {
                val lyrics = repository.getLyrics(song.title, song.artistName, song.durationSeconds)
                _currentLyrics.value = lyrics
            } catch (e: Exception) {
                e.printStackTrace()
                _currentLyrics.value = null
            } finally {
                _isLyricsLoading.value = false
            }
        }
    }

    fun openArtist(channelId: String) {
        viewModelScope.launch {
            val artist = repository.getArtist(channelId)
            _selectedArtist.value = artist
        }
    }

    fun clearArtist() {
        _selectedArtist.value = null
    }

    fun openAlbum(browseId: String) {
        viewModelScope.launch {
            val album = repository.getAlbumOrPlaylist(browseId)
            _selectedAlbum.value = album
        }
    }

    fun clearAlbum() {
        _selectedAlbum.value = null
    }
}
