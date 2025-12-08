package com.example.lyricmemo.ui.input

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lyricmemo.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InputLyricsFragment : Fragment(R.layout.fragment_input_lyrics) {

    private val viewModel: InputLyricsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etArtist = view.findViewById<EditText>(R.id.etArtist)
        val etUrl = view.findViewById<EditText>(R.id.etUrl)
        val etLyrics = view.findViewById<EditText>(R.id.etLyrics)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            viewModel.saveSong(
                title = etTitle.text.toString(),
                artist = etArtist.text.toString(),
                url = etUrl.text.toString(),
                lyrics = etLyrics.text.toString()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 保存メッセージの監視
                launch {
                    viewModel.saveMessage.collect { message ->
                        if (message != null) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            viewModel.messageShown()
                        }
                    }
                }

                // 保存完了状態の監視
                launch {
                    viewModel.isSaved.collect { isSaved ->
                        if (isSaved) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }
}