package com.example.lyricmemo.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.repository.SavedSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsDetailViewModel @Inject constructor(
    private val savedSongRepository: SavedSongRepository
) : ViewModel() {

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    fun saveSong(
        title: String, 
        artist: String, 
        lyrics: String,
        youtubeUrl: String?,
        thumbnailUrl: String?
    ) {
        viewModelScope.launch {
            try {
                savedSongRepository.saveSong(title, artist, lyrics, youtubeUrl, thumbnailUrl)
                _saveMessage.value = "保存しました"
            } catch (e: Exception) {
                _saveMessage.value = e.message ?: "保存に失敗しました"
            }
        }
    }

    fun messageShown() {
        _saveMessage.value = null
    }
}