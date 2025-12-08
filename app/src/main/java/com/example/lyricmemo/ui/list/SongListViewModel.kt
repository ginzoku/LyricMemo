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

    init {
        val artistId = savedStateHandle.get<Int>("artistId")
        val artistName = savedStateHandle.get<String>("artistName")
        
        if (artistId != null) {
            _uiState.value = _uiState.value.copy(toolbarTitle = artistName ?: "曲一覧")
            searchSongs(artistId)
        }
    }

    private fun searchSongs(artistId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val songs = repository.searchSongsByArtistId(artistId)
            if (songs.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = false, songs = songs)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "曲が見つかりませんでした。")
            }
        }
    }
}