package com.example.lyricmemo.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lyricmemo.R
import com.example.lyricmemo.ui.search.SongSearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: SongSearchViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGoToSearch = view.findViewById<Button>(R.id.btnGoToSearch)
        val btnGoToList = view.findViewById<Button>(R.id.btnGoToList)

        // 検索画面へ遷移
        btnGoToSearch.setOnClickListener {
            // 遷移する前に前回の検索結果をクリア
            viewModel.clearSearchResults()
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        // リスト画面へ遷移
        btnGoToList.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_savedSongListFragment)
        }
    }
}