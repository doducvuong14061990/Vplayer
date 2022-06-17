package com.doducvuong14061990.vplayer.ui.adapter

import android.annotation.SuppressLint
import android.widget.Filter
import android.widget.Filterable
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.base.BaseAdapter
import com.doducvuong14061990.vplayer.callback.OnItemClickListener
import com.doducvuong14061990.vplayer.data.model.Song

class SongAdapter(itemClickListener: OnItemClickListener<Song>): BaseAdapter<Song>(itemClickListener), Filterable{
    override fun getLayoutIdForPosition(position: Int): Int = R.layout.item_song_view

    var mMusicListOld : ArrayList<Song> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mMusicList: ArrayList<Song>) {
        this.mDataList = mMusicList
        this.mMusicListOld = mMusicList
        notifyDataSetChanged()
    }

    companion object {
        const val CLASS_NAME = "SongAdapter"
        const val SONG_ADAPTER = "SongAdapter"
        const val POSITION = "Position"
    }


    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val strSearch = constraint.toString()
                mDataList = if (strSearch.isEmpty()){
                    mMusicListOld
                }else{
                    val list = ArrayList<Song>()
                    for (song in mMusicListOld){
                        if (song.title.lowercase().contains(strSearch.lowercase())){
                            list.add(song)
                        }
                    }
                    list
                }
                val filterResults: FilterResults = FilterResults()
                filterResults.values = mDataList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mDataList = results?.values as ArrayList<Song>
                notifyDataSetChanged()
            }
        }
    }
}