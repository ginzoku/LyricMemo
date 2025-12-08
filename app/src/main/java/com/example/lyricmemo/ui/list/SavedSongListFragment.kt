package com.example.lyricmemo.ui.list

import android.os.Bundle
import android.view.View
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
import com.example.lyricmemo.data.db.SavedSong
import com.example.lyricmemo.ui.search.SongSearchViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedSongListFragment : Fragment(R.layout.fragment_saved_song_list) {

    private val viewModel: SavedSongListViewModel by viewModels()
    
    // 共有ViewModelを使って詳細画面にデータを渡す
    private val songSearchViewModel: SongSearchViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbarの設定
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_saved_song_list)
        
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

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val fabAddNewSong = view.findViewById<FloatingActionButton>(R.id.fabAddNewSong)
        
        val adapter = SavedSongAdapter(
            onItemClick = { clickedSong ->
                // クリックされたら共有ViewModelにデータをセットして遷移
                songSearchViewModel.setSavedSong(clickedSong)
                findNavController().navigate(R.id.action_savedSongListFragment_to_lyricsDetailFragment)
            },
            onItemLongClick = { longClickedSong ->
                // 長押しされたら削除確認ダイアログを表示
                showDeleteConfirmationDialog(longClickedSong)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        // FABのクリック処理
        fabAddNewSong.setOnClickListener {
            // 歌詞入力画面へ遷移
            findNavController().navigate(R.id.action_savedSongListFragment_to_inputLyricsFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedSongs.collect { songs ->
                    adapter.submitList(songs)
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(song: SavedSong) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("曲の削除")
            .setMessage("'${song.title}' を削除しますか？")
            .setNegativeButton("いいえ", null)
            .setPositiveButton("はい") { _, _ ->
                viewModel.deleteSong(song)
            }
            .show()
    }
}