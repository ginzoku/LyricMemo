package com.example.lyricmemo.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSongDao {
    @Insert
    suspend fun insert(song: SavedSong)

    @Delete
    suspend fun delete(song: SavedSong)

    @Query("SELECT * FROM saved_songs ORDER BY savedAt DESC")
    fun getAllSongs(): Flow<List<SavedSong>>

    @Query("SELECT * FROM saved_songs WHERE title = :title AND artist = :artist AND lyrics = :lyrics LIMIT 1")
    suspend fun findDuplicate(title: String, artist: String, lyrics: String): SavedSong?
}