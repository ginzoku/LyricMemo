package com.example.lyricmemo.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.model.SongItem
import com.example.lyricmemo.data.repository.VocaDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongListUiState(
    val songs: List<SongItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val toolbarTitle: String = ""
)

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val repository: VocaDbRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongListUiState())
    val uiState: StateFlow<SongListUiState> = _uiState.asStateFlow()

    private var currentStart = 0
    private var hasMore = true
    private var isLoadingMore = false
    private val artistId: Int = savedStateHandle.get<Int>("artistId")!!

    init {
        val artistName = savedStateHandle.get<String>("artistName")
        _uiState.value = _uiState.value.copy(toolbarTitle = artistName ?: "曲一覧")
        loadSongs(true)
    }

    fun loadMoreSongs() {
        if (hasMore && !isLoadingMore) {
            loadSongs(false)
        }
    }

    private fun loadSongs(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                currentStart = 0
                hasMore = true
                isLoadingMore = false
                _uiState.value = _uiState.value.copy(isLoading = true, songs = emptyList())
            } else {
                if (isLoadingMore) return@launch
                isLoadingMore = true
            }

            val newSongs = repository.searchSongsByArtistId(artistId, start = currentStart)
            
            if (newSongs.isEmpty()) {
                hasMore = false
            } else {
                currentStart += newSongs.size
                val currentSongs = _uiState.value.songs
                val updatedSongs = (currentSongs + newSongs).distinctBy { it.id }
                _uiState.value = _uiState.value.copy(songs = updatedSongs)
            }

            if (reset) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (_uiState.value.songs.isEmpty()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "曲が見つかりませんでした。")
                }
            }
            isLoadingMore = false
        }
    }
}