package com.example.lyricmemo.ui.input

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
class InputLyricsViewModel @Inject constructor(
    private val savedSongRepository: SavedSongRepository
) : ViewModel() {

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun saveSong(title: String, artist: String, url: String, lyrics: String) {
        if (title.isBlank()) {
            _saveMessage.value = "曲名は必須です"
            return
        }
        if (lyrics.isBlank()) {
            _saveMessage.value = "歌詞は必須です"
            return
        }

        viewModelScope.launch {
            try {
                // URLはYouTubeのものか判定して保存（今回は単純にそのまま保存）
                // サムネイルURLは手動入力では取得困難なためnullとするか、
                // YouTube URLから生成するロジックを入れても良いが今回はnull
                savedSongRepository.saveSong(
                    title = title,
                    artist = artist,
                    lyrics = lyrics,
                    youtubeUrl = url.ifBlank { null },
                    thumbnailUrl = null
                )
                _saveMessage.value = "保存しました"
                _isSaved.value = true
            } catch (e: Exception) {
                _saveMessage.value = e.message ?: "保存に失敗しました"
            }
        }
    }

    fun messageShown() {
        _saveMessage.value = null
    }
}