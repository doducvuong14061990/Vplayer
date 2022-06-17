package com.doducvuong14061990.vplayer.ui.adapter

import android.annotation.SuppressLint
import android.widget.Filter
import android.widget.Filterable
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.base.BaseAdapter
import com.doducvuong14061990.vplayer.callback.OnItemClickListener
import com.doducvuong14061990.vplayer.data.model.Song

class SongAdapter(itemClickListener: OnItemClickListener<Song>): BaseAdapter<Song>(itemClickListener){
    override fun getLayoutIdForPosition(position: Int): Int = R.layout.item_song_view

    companion object {
        const val CLASS_NAME = "SongAdapter"
        const val SONG_ADAPTER = "SongAdapter"
        const val POSITION = "Position"
    }


//    override fun getFilter(): Filter {
//        return object : Filter(){
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val strSearch = constraint.toString()
//                mMusicList = if (strSearch.isEmpty()){
//                    mMusicListOld
//                }else{
//                    val list = ArrayList<Music>()
//                    for (song in mMusicListOld){
//                        if (song.name.lowercase().contains(strSearch.lowercase())){
//                            list.add(song)
//                        }
//                    }
//                    list
//                }
//                val filterResults: FilterResults = FilterResults()
//                filterResults.values = mMusicList
//                return filterResults
//            }
//
//            @SuppressLint("NotifyDataSetChanged")
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                mMusicList = results?.values as ArrayList<Music>
//                binding.tvTotalSong.text = "Total Song: " + "${mMusicList.size}"
//                notifyDataSetChanged()
//            }
//
//        }
//    }
}