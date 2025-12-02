package com.example.lyricmemo.data.repository

import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.data.db.SavedSongDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavedSongRepository @Inject constructor(
    private val dao: SavedSongDao
) {
    suspend fun saveSong(
        title: String, 
        artist: String, 
        lyrics: String,
        youtubeUrl: String?,
        thumbnailUrl: String?
    ) {
        // 重複チェック
        val duplicate = dao.findDuplicate(title, artist, lyrics)
        if (duplicate != null) {
            throw Exception("すでに保存されています")
        }

        val song = SavedSong(
            title = title,
            artist = artist,
            lyrics = lyrics,
            youtubeUrl = youtubeUrl,
            thumbnailUrl = thumbnailUrl
        )
        dao.insert(song)
    }

    suspend fun deleteSong(song: SavedSong) {
        dao.delete(song)
    }

    fun getAllSongs(): Flow<List<SavedSong>> {
        return dao.getAllSongs()
    }
}