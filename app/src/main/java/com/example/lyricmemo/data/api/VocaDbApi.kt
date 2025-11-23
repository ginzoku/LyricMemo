package com.example.lyricmemo.data.api

import com.example.lyricmemo.data.model.VocaDbSongResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VocaDbApi {
    @GET("api/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("fields") fields: String = "Lyrics",
        @Query("lang") lang: String = "Japanese",
        @Query("maxResults") maxResults: Int = 20, // デフォルトを20件に変更
        @Query("sort") sort: String = "FavoritedTimes"
    ): VocaDbSongResponse
}