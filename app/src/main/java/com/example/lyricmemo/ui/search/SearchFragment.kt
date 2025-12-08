package com.example.lyricmemo.ui.search

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lyricmemo.R
import com.example.lyricmemo.data.repository.SearchType
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SongSearchViewModel by activityViewModels()
    private var etSearch: EditText? = null
    private lateinit var songAdapter: SearchResultAdapter
    private lateinit var artistAdapter: ArtistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        etSearch = view.findViewById(R.id.etSearch)
        val chipGroupSearchType = view.findViewById<ChipGroup>(R.id.chipGroupSearchType)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvErrorMessage = view.findViewById<TextView>(R.id.tvErrorMessage)

        setupToolbar(toolbar)
        setupAdapters(recyclerView)
        setupListeners(chipGroupSearchType)
        observeViewModel(progressBar, tvErrorMessage, recyclerView)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun setupAdapters(recyclerView: RecyclerView) {
        songAdapter = SearchResultAdapter { song ->
            viewModel.selectSong(song)
            findNavController().navigate(R.id.lyricsDetailFragment)
        }
        artistAdapter = ArtistAdapter { artist ->
            val bundle = bundleOf(
                "artistId" to artist.id,
                "artistName" to artist.name
            )
            findNavController().navigate(R.id.action_searchFragment_to_songListFragment, bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun setupListeners(chipGroup: ChipGroup) {
        etSearch?.addTextChangedListener {
            viewModel.onQueryChanged(it.toString())
        }
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            // 検索欄のテキストをクリアする
            etSearch?.text?.clear()
            
            val searchType = when (checkedId) {
                R.id.chipSongName -> SearchType.SONG_NAME
                R.id.chipArtistName -> SearchType.ARTIST_NAME
                else -> SearchType.SONG_NAME
            }
            etSearch?.hint = if (searchType == SearchType.SONG_NAME) "曲名を入力" else "アーティスト名を入力"
            viewModel.onSearchTypeChanged(searchType)
        }
    }

    private fun observeViewModel(progressBar: ProgressBar, tvError: TextView, recyclerView: RecyclerView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 曲リストの監視
                launch {
                    viewModel.songListState.collect { state ->
                        progressBar.isVisible = state.isLoading
                        tvError.isVisible = state.errorMessage != null
                        tvError.text = state.errorMessage

                        if (state.songs.isNotEmpty()) {
                            recyclerView.adapter = songAdapter
                            songAdapter.submitList(state.songs)
                        } else if (!state.isLoading && viewModel.artistListState.value.artists.isEmpty()) {
                            // 他方のリストも空の場合のみクリア
                            songAdapter.submitList(emptyList())
                        }
                    }
                }

                // アーティストリストの監視
                launch {
                    viewModel.artistListState.collect { state ->
                        progressBar.isVisible = state.isLoading
                        tvError.isVisible = state.errorMessage != null
                        tvError.text = state.errorMessage

                        if (state.artists.isNotEmpty()) {
                            recyclerView.adapter = artistAdapter
                            artistAdapter.submitList(state.artists)
                        } else if (!state.isLoading && viewModel.songListState.value.songs.isEmpty()){
                             // 他方のリストも空の場合のみクリア
                            artistAdapter.submitList(emptyList())
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        etSearch = null
    }
}