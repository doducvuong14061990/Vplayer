package com.doducvuong14061990.vplayer.callback

import com.doducvuong14061990.vplayer.data.model.Song
import com.doducvuong14061990.vplayer.data.model.Songs

interface OnItemClickListener<T> {
    fun onItemClick(data: T)
}