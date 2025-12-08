package com.example.lyricmemo.data.model

data class VocaDbSongResponse(
    val items: List<SongItem>
)

data class SongItem(
    val name: String,
    val artistString: String,
    val lyrics: List<Lyric>?,
    val pvs: List<PV>?
)

data class Lyric(
    val value: String,
    val cultureCode: String // "ja" など
)

data class PV(
    val url: String,
    val service: String, // NicoNicoDouga, Youtube, etc.
    val pvType: String, // Original or other
    val thumbUrl: String?
)

// アーティスト検索用レスポンス
data class VocaDbArtistResponse(
    val items: List<ArtistItem>
)

// アーティスト情報
data class ArtistItem(
    val id: Int,
    val name: String
)
