package com.example.lyricmemo.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.lyricmemo.R
import com.example.lyricmemo.ui.search.SongSearchViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LyricsDetailFragment : Fragment(R.layout.fragment_lyrics_detail) {

    // データの表示用 (Activity Scopeで共有 - SearchFragmentと同じViewModelを参照)
    private val searchViewModel: SongSearchViewModel by activityViewModels()

    // 保存操作用 (Fragment Scopeで固有)
    private val lyricsDetailViewModel: LyricsDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSongTitle = view.findViewById<TextView>(R.id.tvSongTitle)
        val tvArtist = view.findViewById<TextView>(R.id.tvArtist)
        val tvLyricsContent = view.findViewById<TextView>(R.id.tvLyricsContent)
        val fabSave = view.findViewById<FloatingActionButton>(R.id.fabSave)
        val btnYoutube = view.findViewById<Button>(R.id.btnYoutube)
        val ivThumbnail = view.findViewById<ImageView>(R.id.ivThumbnail)

        // UI状態を監視して反映 (searchViewModel)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.uiState.collect { state ->
                    tvSongTitle.text = state.title
                    tvArtist.text = state.artist
                    tvLyricsContent.text = state.lyricsBody
                    
                    if (state.errorMessage != null) {
                        tvLyricsContent.text = state.errorMessage
                    }

                    // 保存ボタンの表示制御
                    fabSave.isVisible = state.isSaveButtonVisible

                    // YouTubeボタンの表示制御とクリック処理
                    if (state.youtubeUrl != null) {
                        btnYoutube.isVisible = true
                        btnYoutube.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.youtubeUrl))
                            startActivity(intent)
                        }
                    } else {
                        btnYoutube.isVisible = false
                    }

                    // サムネイル画像の表示
                    if (state.thumbnailUrl != null) {
                        ivThumbnail.isVisible = true
                        Glide.with(this@LyricsDetailFragment)
                            .load(state.thumbnailUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery) // 読み込み中の画像
                            .error(android.R.drawable.stat_notify_error) // エラー時の画像
                            .into(ivThumbnail)
                    } else {
                        ivThumbnail.isVisible = false
                    }
                }
            }
        }

        // 保存メッセージを監視 (lyricsDetailViewModel)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                lyricsDetailViewModel.saveMessage.collect { message ->
                    if (message != null) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        lyricsDetailViewModel.messageShown()
                    }
                }
            }
        }

        // 保存ボタンの処理
        fabSave.setOnClickListener {
            val currentState = searchViewModel.uiState.value
            if (currentState.title.isNotBlank() && currentState.lyricsBody.isNotBlank()) {
                lyricsDetailViewModel.saveSong(
                    title = currentState.title,
                    artist = currentState.artist,
                    lyrics = currentState.lyricsBody
                )
            } else {
                Toast.makeText(requireContext(), "保存するデータがありません", Toast.LENGTH_SHORT).show()
            }
        }
    }
}