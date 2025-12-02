package com.example.lyricmemo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedSong::class], version = 2) // バージョンを2に更新
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedSongDao(): SavedSongDao
}