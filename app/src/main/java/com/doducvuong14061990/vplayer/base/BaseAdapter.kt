package com.doducvuong14061990.vplayer.base

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.doducvuong14061990.vplayer.callback.OnItemClickListener

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.doducvuong14061990.vplayer.BR

abstract class BaseAdapter<T>(private val itemClickListener: OnItemClickListener<T>) : RecyclerView.Adapter<BaseAdapter<T>.ViewHolder>() {

    private var mMusicList : ArrayList<T> = ArrayList()
    private var mMusicListOld : ArrayList<T> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun addData(mMusicList: ArrayList<T>) {
        this.mMusicList = mMusicList
        this.mMusicListOld = mMusicList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =  mMusicList.size

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition(position)
    }
    abstract fun getLayoutIdForPosition(position: Int): Int
    private fun getItemForPosition(position: Int): T = mMusicList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =  DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            viewType,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemForPosition : T = getItemForPosition(position)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.bind(itemForPosition)
    }



    // Holds the views for adding it to image and text
    inner class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(obj: T){
            binding.setVariable(BR.music, obj)
            binding.executePendingBindings()
        }
    }


}
