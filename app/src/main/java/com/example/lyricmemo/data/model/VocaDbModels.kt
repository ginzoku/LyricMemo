package com.example.lyricmemo.data.model

data class VocaDbSongResponse(
    val items: List<SongItem>
)

data class SongItem(
    val name: String,
    val artistString: String,
    val lyrics: List<Lyric>?,
    val pvs: List<PV>? // PV情報を追加
)

data class Lyric(
    val value: String,
    val cultureCode: String // "ja" など
)

data class PV(
    val url: String,
    val service: String, // NicoNicoDouga, Youtube, etc.
    val pvType: String, // Original or other
    val thumbUrl: String? // サムネイル画像のURL
)
