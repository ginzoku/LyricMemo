package com.example.lyricmemo.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lyricmemo.R
import com.example.lyricmemo.data.model.SongItem

class SearchResultAdapter(
    private val onItemClick: (SongItem) -> Unit
) : ListAdapter<SongItem, SearchResultAdapter.ViewHolder>(SongDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val tvLyricSnippet: TextView = view.findViewById(R.id.tvLyricSnippet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.tvTitle.text = song.name
        holder.tvArtist.text = song.artistString
        
        // 歌詞の最初の1行を取得して表示
        val firstLyricLine = song.lyrics?.firstOrNull()?.value?.split("\n")?.firstOrNull()
        holder.tvLyricSnippet.text = firstLyricLine ?: "(歌詞情報なし)"
        
        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<SongItem>() {
        override fun areItemsTheSame(oldItem: SongItem, newItem: SongItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SongItem, newItem: SongItem): Boolean {
            return oldItem == newItem
        }
    }
}