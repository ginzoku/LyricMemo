package com.example.lyricmemo.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.data.model.ArtistItem
import com.example.lyricmemo.data.model.SongItem
import com.example.lyricmemo.data.repository.SearchType
import com.example.lyricmemo.data.repository.VocaDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI状態の定義
data class LyricsUiState(val title: String = "", val artist: String = "", val lyricsBody: String = "", val isSaveButtonVisible: Boolean = true, val errorMessage: String? = null, val youtubeUrl: String? = null, val thumbnailUrl: String? = null)
data class SongListUiState(val songs: List<SongItem> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null)
data class ArtistListUiState(val artists: List<ArtistItem> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null)

@OptIn(FlowPreview::class)
@HiltViewModel
class SongSearchViewModel @Inject constructor(
    private val repository: VocaDbRepository
) : ViewModel() {

    private val _lyricsUiState = MutableStateFlow(LyricsUiState())
    val lyricsUiState: StateFlow<LyricsUiState> = _lyricsUiState.asStateFlow()

    private val _songListState = MutableStateFlow(SongListUiState())
    val songListState: StateFlow<SongListUiState> = _songListState.asStateFlow()

    private val _artistListState = MutableStateFlow(ArtistListUiState())
    val artistListState: StateFlow<ArtistListUiState> = _artistListState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchType = MutableStateFlow(SearchType.SONG_NAME)

    private var songNameSearchStart = 0
    private var hasMoreSongs = true
    private var isLoadingMoreSongs = false

    init {
        viewModelScope.launch {
            _searchQuery.debounce(500).collect { query ->
                if (query.isNotBlank()) {
                    when (_searchType.value) {
                        SearchType.SONG_NAME -> searchSongsByName(query, true)
                        SearchType.ARTIST_NAME -> searchArtists(query)
                    }
                } else {
                    clearAllResults()
                }
            }
        }
    }

    fun onQueryChanged(query: String) { _searchQuery.value = query }
    fun onSearchTypeChanged(type: SearchType) {
        _searchType.value = type
        onQueryChanged("")
    }

    fun loadMoreSongs() {
        if (_searchType.value == SearchType.SONG_NAME && hasMoreSongs && !isLoadingMoreSongs && _searchQuery.value.isNotBlank()) {
            searchSongsByName(_searchQuery.value, false)
        }
    }

    private fun searchSongsByName(query: String, reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                songNameSearchStart = 0
                hasMoreSongs = true
                isLoadingMoreSongs = false
                _artistListState.value = ArtistListUiState()
                _songListState.value = SongListUiState(isLoading = true, songs = emptyList())
            } else {
                if (isLoadingMoreSongs) return@launch
                isLoadingMoreSongs = true
            }
            
            val newSongs = repository.searchSongsByName(query, start = songNameSearchStart)
            
            if (newSongs.isEmpty()) {
                hasMoreSongs = false
            } else {
                songNameSearchStart += newSongs.size
                val currentSongs = _songListState.value.songs
                val updatedSongs = (currentSongs + newSongs).distinctBy { it.id }
                _songListState.value = _songListState.value.copy(songs = updatedSongs)
            }
            
            if (reset) {
                _songListState.value = _songListState.value.copy(isLoading = false)
                if (_songListState.value.songs.isEmpty()) {
                    _songListState.value = _songListState.value.copy(errorMessage = "曲が見つかりませんでした。")
                }
            }
            isLoadingMoreSongs = false
        }
    }

    private fun searchArtists(query: String) {
        viewModelScope.launch {
            _songListState.value = SongListUiState()
            _artistListState.value = ArtistListUiState(isLoading = true)
            val artists = repository.searchArtists(query)
            if (artists.isNotEmpty()) {
                _artistListState.value = ArtistListUiState(artists = artists)
            } else {
                _artistListState.value = ArtistListUiState(errorMessage = "アーティストが見つかりませんでした。")
            }
        }
    }

    fun clearAllResults() {
        _songListState.value = SongListUiState()
        _artistListState.value = ArtistListUiState()
        songNameSearchStart = 0
        hasMoreSongs = true
        isLoadingMoreSongs = false
    }
    
    fun selectSong(song: SongItem) {
        val lyricText = song.lyrics?.firstOrNull()?.value
        val youtubePv = song.pvs?.find { it.service.equals("Youtube", ignoreCase = true) }
        if (lyricText != null) {
            _lyricsUiState.value = LyricsUiState(title = song.name, artist = song.artistString, lyricsBody = lyricText, isSaveButtonVisible = true, youtubeUrl = youtubePv?.url, thumbnailUrl = youtubePv?.thumbUrl)
        } else {
            _lyricsUiState.value = LyricsUiState(title = song.name, artist = song.artistString, lyricsBody = "歌詞データなし", errorMessage = "No lyrics found", isSaveButtonVisible = false, youtubeUrl = youtubePv?.url, thumbnailUrl = youtubePv?.thumbUrl)
        }
    }

    fun setSavedSong(song: SavedSong) {
        _lyricsUiState.value = LyricsUiState(title = song.title, artist = song.artist, lyricsBody = song.lyrics, isSaveButtonVisible = false, youtubeUrl = song.youtubeUrl, thumbnailUrl = song.thumbnailUrl)
    }
}