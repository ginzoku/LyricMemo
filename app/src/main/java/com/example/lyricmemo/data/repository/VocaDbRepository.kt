package com.example.lyricmemo.data.repository

import android.util.Log
import com.example.lyricmemo.data.api.VocaDbApi
import com.example.lyricmemo.data.model.ArtistItem
import com.example.lyricmemo.data.model.SongItem
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

enum class SearchType {
    SONG_NAME,
    ARTIST_NAME
}

class VocaDbRepository @Inject constructor(
    private val vocaDbApi: VocaDbApi
) {
    // 曲名で曲を検索 (ページネーション対応)
    suspend fun searchSongsByName(query: String, start: Int = 0): List<SongItem> {
        return try {
            val response = vocaDbApi.searchSongs(query = query, nameMatchMode = "Auto", start = start)
            response.items.filter { !it.lyrics.isNullOrEmpty() }
        } catch (e: Exception) {
            Log.e("APIDebug", "searchSongsByName failed", e)
            emptyList()
        }
    }

    // アーティスト名（部分一致）でアーティストのリストを検索
    suspend fun searchArtists(query: String): List<ArtistItem> {
        return try {
            val response = vocaDbApi.searchArtists(query = query)
            response.items
        } catch (e: Exception) {
            Log.e("APIDebug", "searchArtists failed", e)
            emptyList()
        }
    }

    // アーティストIDで曲を検索 (ページネーション対応)
    suspend fun searchSongsByArtistId(artistId: Int, start: Int = 0): List<SongItem> {
        return try {
            Log.d("APIDebug", "Repository: Calling searchSongsByArtist with id: $artistId, start: $start")
            val response = vocaDbApi.searchSongsByArtist(artistId = artistId, start = start)
            Log.d("APIDebug", "Repository: API response received, ${response.items.size} items")
            response.items.filter { !it.lyrics.isNullOrEmpty() }
        } catch (e: Exception) {
            Log.e("APIDebug", "searchSongsByArtistId failed", e)
            emptyList()
        }
    }
}