package com.doducvuong14061990.vplayer.ui.fragment.home.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.doducvuong14061990.vplayer.data.model.Tab
import com.doducvuong14061990.vplayer.utils.getNavHeight
import com.doducvuong14061990.vplayer.utils.getStatusBarHeight


class HomeFragmentViewModel(val application: Application) : ViewModel() {

    private val _mTab = MutableLiveData<Tab>()
    val mTab: LiveData<Tab>
        get() = _mTab

    private val _mStatusBarHeight = MutableLiveData<Int>()
    val mStatusBarHeight: LiveData<Int>
        get() = _mStatusBarHeight




    init {
        _mStatusBarHeight.postValue(application.getStatusBarHeight())

        _mTab.postValue(
            Tab(
                "Songs",
                pagerSongs = true,
                pagerFavourites = false,
                pagerPlaylists = false,
                swipe = false
            )
        )
    }

    private fun toPagerSongs(swipe : Boolean) {
        _mTab.postValue(
            Tab(
                "Songs",
                pagerSongs = true,
                pagerFavourites = false,
                pagerPlaylists = false,
                swipe = swipe
            )
        )
    }

    private fun toPagerFavourites(swipe : Boolean) {
        _mTab.postValue(
            Tab(
                "Favourites",
                pagerSongs = false,
                pagerFavourites = true,
                pagerPlaylists = false,
                swipe = swipe
            )
        )
    }

    private fun toPagerPlayerlists(swipe : Boolean) {
        _mTab.postValue(
            Tab(
                "Playlists",
                pagerSongs = false,
                pagerFavourites = false,
                pagerPlaylists = true,
                swipe = swipe
            )
        )
    }

    fun onClickTab(position: Int) {
        when (position) {
            0 -> {
                toPagerSongs(false)
            }
            1 -> {
                toPagerFavourites(false)
            }
            2 -> {
                toPagerPlayerlists(false)
            }
            else -> {
                toPagerSongs(false)
            }
        }
    }

    val onPagerChange = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> {
                    toPagerSongs(true)
                }
                1 -> {
                    toPagerFavourites(true)
                }
                2 -> {
                    toPagerPlayerlists(true)
                }
                else -> {
                    toPagerSongs(true)
                }
            }
            super.onPageSelected(position)
        }
    }
}