package com.doducvuong14061990.vplayer.ui.fragment.home.song

import android.app.Application
import android.graphics.Bitmap
import android.util.LruCache
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.app.ApplicationClass.Companion.application
import com.doducvuong14061990.vplayer.data.model.Song
import com.doducvuong14061990.vplayer.utils.getBitmapFromDrawable
import com.doducvuong14061990.vplayer.utils.getNavHeight
import com.doducvuong14061990.vplayer.utils.getSongPhotoBitmap

// AndroidViewModel cung cấp ngữ cảnh ứng dụng...
class SongViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var mMemoryCache: LruCache<String, Palette>
    private var getBitmapFromDrawable: Bitmap

    private val _mNavBarHeight = MutableLiveData<Int>()
    val mNavBarHeight: LiveData<Int>
        get() = _mNavBarHeight

    private val _mPhotoSongBitmap = MutableLiveData<Bitmap?>()
    val mPhotoSongBitmap: LiveData<Bitmap?>
        get() = _mPhotoSongBitmap

    private val _mSongPhotoPalette = MutableLiveData<Palette>()
    val mSongPhotoPalette: LiveData<Palette>
        get() = _mSongPhotoPalette

    private val _mPlaying = MutableLiveData<Boolean>()
    val mPlaying: LiveData<Boolean>
        get() = _mPlaying

    private val _nameSong = MutableLiveData<String>()
    val nameSong: LiveData<String>
        get() = _nameSong

    private val _mOnClickedPhoto = MutableLiveData<Boolean>()
    val mOnClickedPhoto: LiveData<Boolean>
        get() = _mOnClickedPhoto

    private val _mClickPlayOrPause = MutableLiveData<Boolean>()
    val mClickPlayOrPause: LiveData<Boolean>
        get() = _mClickPlayOrPause

    private val _mShuffle = MutableLiveData<Boolean>()
    val mShuffle: LiveData<Boolean>
        get() = _mShuffle

    private val _mClickPrevious = MutableLiveData<Boolean>()
    val mClickPrevious: LiveData<Boolean>
        get() = _mClickPrevious

    private val _mClickNext = MutableLiveData<Boolean>()
    val mClickNext: LiveData<Boolean>
        get() = _mClickNext


    private val _mRepeatPattern = MutableLiveData<Int>()
    val mRepeatPattern: LiveData<Int>
        get() = _mRepeatPattern

    init {
        _mNavBarHeight.value = application.getNavHeight()
        _mOnClickedPhoto.value = false
        _mPlaying.value = false

        _mPhotoSongBitmap.value = null
        _mShuffle.value = false
        _mClickPlayOrPause.value = false
        _mClickPrevious.value = false
        _mClickNext.value = false

        _mRepeatPattern.value = 0

        getBitmapFromDrawable = R.drawable.music_small_min.getBitmapFromDrawable(application)
        lruCache()
    }

    fun setPlaying(value: Boolean) {
        _mPlaying.value = value
    }

    fun setOnClickPhoto(value: Boolean) {
        _mOnClickedPhoto.value = value
    }

    fun onClickPlayOrPause(value: Boolean) {
        _mClickPlayOrPause.value = value
    }

    fun onClickPrevious(value: Boolean) {
        _mClickPrevious.value = value
    }

    fun onClickNext(value: Boolean) {
        _mClickNext.value = value
    }

    fun onClickRepeat() {
        mRepeatPattern.value?.let {
            if (it < 2) {
                _mRepeatPattern.value = it.plus(1)
            } else {
                _mRepeatPattern.value = 0
            }
        }
    }

    private fun lruCache() {
        /** Bộ nhớ cache mà thiết bị cung cấp cho app */
        val maxMemory: Int = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        // Use 1/8th of the available memory for this memory cache.
        val cacheSize: Int = maxMemory / 8
        mMemoryCache = object : LruCache<String, Palette>(cacheSize) {
            override fun sizeOf(key: String?, value: Palette?): Int {
                return super.sizeOf(key, value)
            }
        }
    }

    private fun addPaletteToMemoryCache(key: String, palette: Palette) {
        synchronized(mMemoryCache) {
            if (mMemoryCache.get(key) == null) {
                mMemoryCache.put(key, palette)
            }
        }
    }

    private fun getPaletteFromMemCache(key: String): Palette {
        return mMemoryCache.get(key)
    }


    private fun getPalette(bitmap: Bitmap): Palette {
        return Palette.from(bitmap).maximumColorCount(32).generate()
    }

    fun setDataToUIControlMini(song: Song, isPlaying: Boolean, repeat: Int) {
        _mPhotoSongBitmap.value = song.getSongPhotoBitmap(application)
        _mPlaying.value = isPlaying
        _nameSong.value = song.title
        _mRepeatPattern.value = repeat

        mPhotoSongBitmap.value.also {
            if (it != null) {
                addPaletteToMemoryCache(
                    song.id.toString() + song.title,
                    getPalette(it)
                )
            } else {
                addPaletteToMemoryCache(
                    song.id.toString() + song.title,
                    getPalette(getBitmapFromDrawable)
                )
            }
        }
        _mSongPhotoPalette.value = getPaletteFromMemCache(song.id.toString() + song.title)
    }
}