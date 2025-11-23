package com.example.lyricmemo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.data.repository.SavedSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SortOrder {
    TITLE,
    ARTIST,
    DATE
}

@HiltViewModel
class SavedSongListViewModel @Inject constructor(
    savedSongRepository: SavedSongRepository
) : ViewModel() {

    // ソート順を保持するフロー
    private val _sortOrder = MutableStateFlow(SortOrder.DATE)

    // DBからのデータとソート順を組み合わせて、最終的なリストを生成する
    val savedSongs: StateFlow<List<SavedSong>> = combine(
        savedSongRepository.getAllSongs(),
        _sortOrder
    ) { songs, sortOrder ->
        when (sortOrder) {
            SortOrder.TITLE -> songs.sortedBy { it.title }
            SortOrder.ARTIST -> songs.sortedBy { it.artist }
            SortOrder.DATE -> songs.sortedByDescending { it.savedAt } // 新しい順
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}