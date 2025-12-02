package com.example.lyricmemo.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.data.model.SongItem
import com.example.lyricmemo.data.repository.VocaDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// 詳細画面用のUI状態
data class LyricsUiState(
    val title: String = "",
    val artist: String = "",
    val lyricsBody: String = "ここに歌詞が表示されます",
    val isSaveButtonVisible: Boolean = true,
    val errorMessage: String? = null,
    val youtubeUrl: String? = null // YouTubeのURL
)

// 検索結果リスト画面用のUI状態
data class SearchResultUiState(
    val searchResults: List<SongItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SongSearchViewModel @Inject constructor(
    private val repository: VocaDbRepository
) : ViewModel() {

    // 詳細画面の状態
    private val _uiState = MutableStateFlow(LyricsUiState())
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    // 検索結果リストの状態
    private val _searchResultState = MutableStateFlow(SearchResultUiState())
    val searchResultState: StateFlow<SearchResultUiState> = _searchResultState.asStateFlow()

    // 検索クエリ用のフロー
    private val _searchQuery = MutableStateFlow("")

    init {
        _searchQuery
            .debounce(500) // 500ミリ秒入力がなければ実行
            .onEach { query ->
                if (query.isNotBlank()) {
                    searchSongs(query)
                } else {
                    clearSearchResults()
                }
            }
            .launchIn(viewModelScope)
    }

    // 検索クエリを更新する (UIから呼ばれる)
    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // 検索実行 (内部ロジック)
    private fun searchSongs(query: String) {
        viewModelScope.launch {
            _searchResultState.value = SearchResultUiState(isLoading = true)
            
            val formattedQuery = query.trim().replace(Regex("\\s+"), " ")
            val results = repository.searchSongsList(formattedQuery)

            if (results.isNotEmpty()) {
                _searchResultState.value = SearchResultUiState(searchResults = results)
            } else {
                _searchResultState.value = SearchResultUiState(
                    errorMessage = "該当する曲が見つかりませんでした。"
                )
            }
        }
    }

    // 検索結果をクリアする
    fun clearSearchResults() {
        _searchResultState.value = SearchResultUiState()
    }

    // リストから曲が選択されたとき (詳細画面へデータをセット)
    fun selectSong(song: SongItem) {
        val lyricText = song.lyrics?.firstOrNull()?.value
        val youtubeUrl = song.pvs?.find { it.service.equals("Youtube", ignoreCase = true) }?.url
        
        if (lyricText != null) {
            _uiState.value = LyricsUiState(
                title = song.name,
                artist = song.artistString,
                lyricsBody = lyricText,
                isSaveButtonVisible = true,
                youtubeUrl = youtubeUrl
            )
        } else {
            _uiState.value = LyricsUiState(
                title = song.name,
                artist = song.artistString,
                lyricsBody = "歌詞データが登録されていませんでした。",
                errorMessage = "No lyrics found",
                isSaveButtonVisible = false,
                youtubeUrl = youtubeUrl
            )
        }
    }

    // 保存された曲を表示用にセットする (詳細画面用)
    fun setSavedSong(song: SavedSong) {
        _uiState.value = LyricsUiState(
            title = song.title,
            artist = song.artist,
            lyricsBody = song.lyrics,
            isSaveButtonVisible = false
        )
    }
}
