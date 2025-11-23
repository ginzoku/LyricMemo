package com.example.lyricmemo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_songs")
data class SavedSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val lyrics: String,
    val savedAt: Long = System.currentTimeMillis()
)