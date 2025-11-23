package com.example.lyricmemo.data.api

import com.example.lyricmemo.data.model.VocaDbSongResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VocaDbApi {
    @GET("api/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("nameMatchMode") nameMatchMode: String, // Exact, Partial, StartsWith, Word, Words
        @Query("fields") fields: String = "Lyrics",
        @Query("lang") lang: String = "Japanese",
        @Query("maxResults") maxResults: Int = 50,
        @Query("sort") sort: String = "FavoritedTimes"
    ): VocaDbSongResponse
}