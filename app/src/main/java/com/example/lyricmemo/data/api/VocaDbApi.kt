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
        @Query("fields") fields: String = "Lyrics,PVs",
        @Query("lang") lang: String = "Japanese",
        @Query("maxResults") maxResults: Int = 50, // 200 -> 50 に戻す
        @Query("sort") sort: String = "FavoritedTimes",
        @Query("start") start: Int = 0
    ): VocaDbSongResponse

    // アーティストID指定での曲検索
    @GET("api/songs")
    suspend fun searchSongsByArtist(
        @Query("artistId[]") artistId: Int,
        @Query("fields") fields: String = "Lyrics,PVs",
        @Query("lang") lang: String = "Japanese",
        @Query("maxResults") maxResults: Int = 50,
        @Query("sort") sort: String = "PublishDate",
        @Query("start") start: Int = 0
    ): VocaDbSongResponse
    
    // アーティスト検索（部分一致）
    @GET("api/artists")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("nameMatchMode") nameMatchMode: String = "Partial",
        @Query("maxResults") maxResults: Int = 20,
        @Query("sort") sort: String = "Name"
    ): VocaDbArtistResponse
}