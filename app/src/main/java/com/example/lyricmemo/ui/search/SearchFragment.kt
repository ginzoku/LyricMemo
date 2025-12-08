package com.example.lyricmemo.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.lyricmemo.R
import com.example.lyricmemo.data.repository.SearchType
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SongSearchViewModel by activityViewModels()
    private var etSearch: EditText? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearch)
        val chipGroupSearchType = view.findViewById<ChipGroup>(R.id.chipGroupSearchType)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvErrorMessage = view.findViewById<TextView>(R.id.tvErrorMessage)

        val adapter = SearchResultAdapter { clickedSong ->
            viewModel.selectSong(clickedSong)
            findNavController().navigate(R.id.action_searchFragment_to_lyricsDetailFragment)
        }

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        // ユーザーの入力に応じてViewModelを更新
        etSearch?.addTextChangedListener {
            viewModel.onQueryChanged(it.toString())
        }

        // ChipGroupの選択変更を監視
        chipGroupSearchType.setOnCheckedChangeListener { _, checkedId ->
            val searchType = when (checkedId) {
                R.id.chipSongName -> SearchType.SONG_NAME
                R.id.chipArtistName -> SearchType.ARTIST_NAME
                else -> SearchType.SONG_NAME
            }
            viewModel.onSearchTypeChanged(searchType)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // ViewModelの検索クエリをEditTextに反映
                launch {
                    viewModel.searchQuery.collect { query ->
                        if (etSearch?.text.toString() != query) {
                            etSearch?.setText(query)
                        }
                    }
                }

                // 検索結果を監視
                launch {
                    viewModel.searchResultState.collect { state ->
                        progressBar.isVisible = state.isLoading
                        
                        if (state.errorMessage != null) {
                            tvErrorMessage.text = state.errorMessage
                            tvErrorMessage.isVisible = true
                            recyclerView.isVisible = false
                        } else {
                            tvErrorMessage.isVisible = false
                            recyclerView.isVisible = state.searchResults.isNotEmpty()
                            adapter.submitList(state.searchResults)
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