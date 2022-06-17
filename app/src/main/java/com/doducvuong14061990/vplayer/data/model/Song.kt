package com.doducvuong14061990.vplayer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val album: String,
    val path: String,
    val artist: String,
    val artistId: String,
    val albumId: Long,
    val duration: Long
) : Parcelable