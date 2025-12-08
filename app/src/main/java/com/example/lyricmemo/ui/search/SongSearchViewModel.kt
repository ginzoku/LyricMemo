package com.example.lyricmemo.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.data.model.ArtistItem
import com.example.lyricmemo.data.model.SongItem
import com.example.lyricmemo.data.repository.SearchType
import com.example.lyricmemo.data.repository.VocaDbRepository
import com.google.gson.Gson
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

    private val gson = Gson()

    // 各種UI状態
    private val _lyricsUiState = MutableStateFlow(LyricsUiState())
    val lyricsUiState: StateFlow<LyricsUiState> = _lyricsUiState.asStateFlow()

    private val _songListState = MutableStateFlow(SongListUiState())
    val songListState: StateFlow<SongListUiState> = _songListState.asStateFlow()

    private val _artistListState = MutableStateFlow(ArtistListUiState())
    val artistListState: StateFlow<ArtistListUiState> = _artistListState.asStateFlow()

    // 検索クエリと検索タイプ
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchType = MutableStateFlow(SearchType.SONG_NAME)

    init {
        viewModelScope.launch {
            _searchQuery.debounce(500).collect { query ->
                if (query.isNotBlank()) {
                    when (_searchType.value) {
                        SearchType.SONG_NAME -> searchSongsByName(query)
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
        if (_searchQuery.value.isNotBlank()) {
            when (type) {
                SearchType.SONG_NAME -> searchSongsByName(_searchQuery.value)
                SearchType.ARTIST_NAME -> searchArtists(_searchQuery.value)
            }
        }
    }

    // アーティスト名でアーティストを検索
    private fun searchArtists(query: String) {
        viewModelScope.launch {
            _songListState.value = SongListUiState() // 曲リストはクリア
            _artistListState.value = ArtistListUiState(isLoading = true)
            val artists = repository.searchArtists(query)
            
            // --- ログ出力 --- 
            Log.d("APIDebug", "searchArtists found: ${gson.toJson(artists)}")
            
            if (artists.isNotEmpty()) {
                _artistListState.value = ArtistListUiState(artists = artists)
            } else {
                _artistListState.value = ArtistListUiState(errorMessage = "アーティストが見つかりませんでした。")
            }
        }
    }

    // アーティストIDで曲を検索
    fun searchSongsByArtistId(artistId: Int) {
        viewModelScope.launch {
            _artistListState.value = ArtistListUiState() // アーティストリストはクリア
            _songListState.value = SongListUiState(isLoading = true)
            
            Log.d("APIDebug", "Searching songs for artistId: $artistId")
            val songs = repository.searchSongsByArtistId(artistId)
            
            // --- ログ出力 --- 
            Log.d("APIDebug", "searchSongsByArtistId found: ${gson.toJson(songs)}")

            if (songs.isNotEmpty()) {
                _songListState.value = SongListUiState(songs = songs)
            } else {
                _songListState.value = SongListUiState(errorMessage = "このアーティストの曲は見つかりませんでした。")
            }
        }
    }

    // ... (他のメソッドは変更なし) ...
    private fun searchSongsByName(query: String) {
        viewModelScope.launch {
            _artistListState.value = ArtistListUiState()
            _songListState.value = SongListUiState(isLoading = true)
            val songs = repository.searchSongsByName(query)
            if (songs.isNotEmpty()) {
                _songListState.value = SongListUiState(songs = songs)
            } else {
                _songListState.value = SongListUiState(errorMessage = "曲が見つかりませんでした。")
            }
        }
    }
    fun clearAllResults() {
        _songListState.value = SongListUiState()
        _artistListState.value = ArtistListUiState()
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