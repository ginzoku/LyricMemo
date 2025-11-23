package com.example.lyricmemo.ui.list

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.lyricmemo.R
import com.example.lyricmemo.ui.search.SongSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedSongListFragment : Fragment(R.layout.fragment_saved_song_list) {

    private val viewModel: SavedSongListViewModel by viewModels()
    
    // 共有ViewModelを使って詳細画面にデータを渡す
    private val sharedViewModel: SongSearchViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbarの設定
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_saved_song_list)
        
        // メニューアイテム選択時の処理
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_by_title -> {
                    viewModel.updateSortOrder(SortOrder.TITLE)
                    true
                }
                R.id.sort_by_artist -> {
                    viewModel.updateSortOrder(SortOrder.ARTIST)
                    true
                }
                R.id.sort_by_date -> {
                    viewModel.updateSortOrder(SortOrder.DATE)
                    true
                }
                else -> false
            }
        }

        // Toolbarの戻るボタン（アイコン）の設定
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        
        val adapter = SavedSongAdapter { clickedSong ->
            // クリックされたら共有ViewModelにデータをセットして遷移
            sharedViewModel.setSavedSong(clickedSong)
            findNavController().navigate(R.id.action_savedSongListFragment_to_lyricsDetailFragment)
        }

        recyclerView.adapter = adapter
        // 区切り線を追加
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        // 画面下部の戻るボタンの処理
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedSongs.collect { songs ->
                    adapter.submitList(songs)
                }
            }
        }
    }
}