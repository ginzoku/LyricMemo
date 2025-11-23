package com.example.lyricmemo.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.lyricmemo.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SongSearchViewModel by activityViewModels()
    private var etSearch: EditText? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 画面表示時に前回の検索結果をクリア
        viewModel.clearSearchResults()

        etSearch = view.findViewById(R.id.etSearch)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvErrorMessage = view.findViewById<TextView>(R.id.tvErrorMessage)

        val adapter = SearchResultAdapter { clickedSong ->
            viewModel.selectSong(clickedSong)
            // 詳細画面へ遷移
            findNavController().navigate(R.id.action_searchFragment_to_lyricsDetailFragment)
            // 遷移時に検索欄をクリアしない（戻ってきた時に残すため）
        }

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        btnSearch.setOnClickListener {
            val query = etSearch?.text.toString()
            if (query.isNotBlank()) {
                // ViewModelの検索メソッドを呼ぶだけ
                viewModel.searchSongs(query)
            }
        }

        btnBack.setOnClickListener {
            // ホームに戻る時はクリアしておく（お好みで）
            etSearch?.text?.clear()
            findNavController().popBackStack()
        }

        // 検索結果を監視
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultState.collect { state ->
                    progressBar.isVisible = state.isLoading
                    
                    if (state.errorMessage != null) {
                        tvErrorMessage.text = state.errorMessage
                        tvErrorMessage.isVisible = true
                        recyclerView.isVisible = false
                    } else {
                        tvErrorMessage.isVisible = false
                        // 結果がない場合はRecyclerViewも隠す（初期状態など）
                        recyclerView.isVisible = state.searchResults.isNotEmpty()
                        adapter.submitList(state.searchResults)
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