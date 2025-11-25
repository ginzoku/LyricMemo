package com.example.lyricmemo.data.api

import com.example.lyricmemo.data.model.VocaDbArtistResponse
import com.example.lyricmemo.data.model.VocaDbSongResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VocaDbApi {
    // 曲検索
    @GET("api/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("nameMatchMode") nameMatchMode: String = "Auto",
        @Query("fields") fields: String = "Lyrics,PVs", // PVsを追加
        @Query("lang") lang: String = "Japanese",
        @Query("maxResults") maxResults: Int = 50,
        @Query("sort") sort: String = "FavoritedTimes"
    ): VocaDbSongResponse

    // アーティストID指定での曲検索
    @GET("api/songs")
    suspend fun searchSongsByArtist(
        @Query("artistId") artistId: Int,
        @Query("fields") fields: String = "Lyrics,PVs", // PVsを追加
        @Query("lang") lang: String = "Japanese",
        @Query("maxResults") maxResults: Int = 50,
        @Query("sort") sort: String = "FavoritedTimes"
    ): VocaDbSongResponse
    
    // アーティスト検索
    @GET("api/artists")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("maxResults") maxResults: Int = 1, // 最も関連性の高い1件を取得
        @Query("sort") sort: String = "Name"
    ): VocaDbArtistResponse
}