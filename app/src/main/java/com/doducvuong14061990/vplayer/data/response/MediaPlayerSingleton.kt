package com.doducvuong14061990.vplayer.data.response

import android.media.MediaPlayer


class MediaPlayerSingleton private constructor() {
    // mp: Là duy nhất (do chỉ có 1 thể hiện của class), nhưng mp có thể thay đổi được giá trị.
    var mp: MediaPlayer? = MediaPlayer()
    companion object{
        @Volatile
        private var instance: MediaPlayerSingleton? = null

        fun getInstance(): MediaPlayerSingleton = synchronized(this) {
            return@synchronized instance ?: MediaPlayerSingleton().also {
                instance = it
            }
        }
    }
}