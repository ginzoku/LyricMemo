package com.example.lyricmemo.data.repository

import com.example.lyricmemo.data.api.VocaDbApi
import com.example.lyricmemo.data.model.SongItem
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VocaDbRepository @Inject constructor(
    private val vocaDbApi: VocaDbApi
) {
    // 1件だけ取得する
    suspend fun getSong(songName: String): SongItem? {
        return try {
            val response = vocaDbApi.searchSongs(
                query = songName, 
                nameMatchMode = "Auto", // Autoに戻す
                maxResults = 10
            )
            response.items.firstOrNull { !it.lyrics.isNullOrEmpty() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 複数件検索して返す (歌詞があるものだけ)
    suspend fun searchSongsList(query: String): List<SongItem> {
        return try {
            val response = vocaDbApi.searchSongs(
                query = query, 
                nameMatchMode = "Auto" // Autoに戻す
            )
            response.items.filter { !it.lyrics.isNullOrEmpty() }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: HttpException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}