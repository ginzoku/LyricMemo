package com.example.lyricmemo.data.model

data class VocaDbSongResponse(
    val items: List<SongItem>
)

data class SongItem(
    val name: String,
    val artistString: String,
    val lyrics: List<Lyric>?
)

data class Lyric(
    val value: String,
    val cultureCode: String // "ja" など
)