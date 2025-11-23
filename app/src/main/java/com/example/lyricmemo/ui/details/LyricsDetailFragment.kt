package com.example.lyricmemo.ui.details

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lyricmemo.R
import com.example.lyricmemo.ui.search.SongSearchViewModel
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
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

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
                    btnSave.isVisible = state.isSaveButtonVisible
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
        btnSave.setOnClickListener {
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

        // 戻るボタンの処理
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}