package com.doducvuong14061990.vplayer.ui.fragment.player

import android.graphics.Bitmap
import android.text.SpannableString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doducvuong14061990.vplayer.app.ApplicationClass.Companion.application
import com.doducvuong14061990.vplayer.data.model.Song
import com.doducvuong14061990.vplayer.utils.*

class PlayerViewModel() : ViewModel() {

    private val _mStatusBarHeight = MutableLiveData<Int>()
    val mStatusBarHeight: LiveData<Int>
        get() = _mStatusBarHeight

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _mSong = MutableLiveData<Song>()
    val mSong: LiveData<Song>
        get() = _mSong

    private val _mTitleToolbar = MutableLiveData<SpannableString>()
    val mTitleToolbar: LiveData<SpannableString>
        get() = _mTitleToolbar

    private val _mPhotoSongBitmap = MutableLiveData<Bitmap?>()
    val mPhotoSongBitmap: LiveData<Bitmap?>
        get() = _mPhotoSongBitmap

    private val _mNameSong = MutableLiveData<String>()
    val mNameSong: LiveData<String>
        get() = _mNameSong

    private val _mNameArtist = MutableLiveData<String>()
    val mNameArtist: LiveData<String>
        get() = _mNameArtist

    private val _mShuffle = MutableLiveData<Boolean>()
    val mShuffle: LiveData<Boolean>
        get() = _mShuffle

    private val _mRepeatPattern = MutableLiveData<Int>()
    val mRepeatPattern: LiveData<Int>
        get() = _mRepeatPattern


    private val _mClickPlayOrPause = MutableLiveData<Boolean>()
    val mClickPlayOrPause: LiveData<Boolean>
        get() = _mClickPlayOrPause

    private val _mClickPrevious = MutableLiveData<Boolean>()
    val mClickPrevious: LiveData<Boolean>
        get() = _mClickPrevious

    private val _mClickNext = MutableLiveData<Boolean>()
    val mClickNext: LiveData<Boolean>
        get() = _mClickNext

    init {
        _mStatusBarHeight.value = application.getStatusBarHeight()
        _mNameSong.value = ""
        _mNameArtist.value = ""
        _mTitleToolbar.value = "".setFormatEmptyTitle(application)
        _mPhotoSongBitmap.value = null
        _isPlaying.value = false
        _mShuffle.value = false
        _mClickPlayOrPause.value = false
        _mClickPrevious.value = false
        _mClickNext.value = false
        _mRepeatPattern.value = 0
    }

    fun setShuffle(value: Boolean) {
        _mShuffle.value = value
    }

    fun setRepeat(value: Int) {
        _mRepeatPattern.value = value
    }

    fun setPlaying(value: Boolean) {
        _isPlaying.value = value
    }

    fun onClickPlayOrPause(value: Boolean) {
        _mClickPlayOrPause.value = value
    }

    fun onClickShufle(value: Boolean) {
        _mShuffle.value = value
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

    fun handleLayoutPlayerFragment(song: Song) {
        _mTitleToolbar.value = song.album.setFormatTitle(application)

        _mPhotoSongBitmap.value = song.getSongPhotoBitmap(application)

        _mNameSong.value = song.title
        _mNameArtist.value = song.artist
    }

}