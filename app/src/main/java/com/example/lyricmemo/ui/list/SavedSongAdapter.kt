package com.example.lyricmemo.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lyricmemo.R
import com.example.lyricmemo.data.db.SavedSong

class SavedSongAdapter(
    private val onItemClick: (SavedSong) -> Unit,
    private val onItemLongClick: (SavedSong) -> Unit
) : ListAdapter<SavedSong, SavedSongAdapter.ViewHolder>(SavedSongDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val tvLyricsPreview: TextView = view.findViewById(R.id.tvLyricsPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist
        
        val previewText = song.lyrics.replace("\n", " ")
        holder.tvLyricsPreview.text = previewText

        holder.itemView.setOnClickListener {
            onItemClick(song)
        }
        
        holder.itemView.setOnLongClickListener {
            onItemLongClick(song)
            true // trueを返してイベントを消費
        }
    }

    class SavedSongDiffCallback : DiffUtil.ItemCallback<SavedSong>() {
        override fun areItemsTheSame(oldItem: SavedSong, newItem: SavedSong): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedSong, newItem: SavedSong): Boolean {
            return oldItem == newItem
        }
    }
}