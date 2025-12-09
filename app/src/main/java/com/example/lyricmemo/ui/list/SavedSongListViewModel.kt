package com.example.lyricmemo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.data.repository.SavedSongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    TITLE,
    ARTIST,
    DATE
}

@HiltViewModel
class SavedSongListViewModel @Inject constructor(
    private val savedSongRepository: SavedSongRepository
) : ViewModel() {

    // 検索クエリを保持するフロー
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // ソート順を保持するフロー
    private val _sortOrder = MutableStateFlow(SortOrder.DATE)

    // 検索クエリとソート順を組み合わせて、最終的なリストを生成する
    val savedSongs: StateFlow<List<SavedSong>> = 
        combine(_searchQuery, _sortOrder) { query, sortOrder ->
            Pair(query, sortOrder)
        }.flatMapLatest { (query, sortOrder) ->
            // クエリがある場合は検索、ない場合は全件取得
            val songsFlow = if (query.isBlank()) {
                savedSongRepository.getAllSongs()
            } else {
                savedSongRepository.searchSongs("%${query}%")
            }
            
            // 取得したFlowに対してソートを適用
            songsFlow.combine(_sortOrder) { songs, order ->
                when (order) {
                    SortOrder.TITLE -> songs.sortedBy { it.title }
                    SortOrder.ARTIST -> songs.sortedBy { it.artist }
                    SortOrder.DATE -> songs.sortedByDescending { it.savedAt }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun deleteSong(song: SavedSong) {
        viewModelScope.launch {
            savedSongRepository.deleteSong(song)
        }
    }
}