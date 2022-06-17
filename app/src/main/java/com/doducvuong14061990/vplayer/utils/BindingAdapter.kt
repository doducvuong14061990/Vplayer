package com.doducvuong14061990.vplayer.utils

import android.graphics.Bitmap
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.app.ApplicationClass.Companion.application
import com.doducvuong14061990.vplayer.data.model.Song
import com.google.android.material.imageview.ShapeableImageView

@BindingAdapter("android:iconImageButton")
fun iconImageButton(view: View, index: Int) {
    val v = view as ImageButton
    when (index) {
        0 -> v.setImageResource(R.drawable.ic_repeat)
        1 -> v.setImageResource(R.drawable.ic_repeat_one)
        2 -> v.setImageResource(R.drawable.ic_repeat_all)
    }
}

@BindingAdapter("android:iconImageButtonWhite")
fun iconImageButtonWhite(view: View, index: Int) {
    val v = view as ImageButton
    when (index) {
        0 -> v.setImageResource(R.drawable.ic_repeat_white)
        1 -> v.setImageResource(R.drawable.ic_repeat_one_white)
        2 -> v.setImageResource(R.drawable.ic_repeat_all_white)
    }
}

@BindingAdapter("android:typeface")
fun typeface(v: View, style: String) {
    val txt: TextView = v as TextView
    when (style) {
        "bold" -> txt.setTypeface(null, Typeface.BOLD)
        "normal" -> txt.setTypeface(null, Typeface.NORMAL)
        else -> txt.setTypeface(null, Typeface.NORMAL)
    }
}

@BindingAdapter("android:layoutMarginTop")
fun layoutMarginTop(view: View, dimen: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.topMargin = dimen.toInt()
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layoutMarginBottom")
fun layoutMarginBottom(view: View, dimen: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.bottomMargin = dimen.toInt()
    view.layoutParams = layoutParams
}

@BindingAdapter("android:setBackgroundPalette")
fun setBackgroundPalette(view: View, palette: Palette?) {
//    palette?.lightMutedSwatch?.rgb?.let { view.setCardBackgroundColor(it) }
    palette?.darkMutedSwatch?.rgb?.let { view.setBackgroundColor(it) }
//    palette?.darkVibrantSwatch?.rgb?.let { view.setBackgroundColor(it) }
//    palette?.lightMutedSwatch?.rgb?.let { view.setBackgroundColor(it) }
//    palette?.vibrantSwatch?.rgb?.let { view.setBackgroundColor(it) }
//    palette?.lightVibrantSwatch?.rgb?.let { view.setBackgroundColor(it) }
}

@BindingAdapter("android:setSongBitmap")
fun setSongBitmap(view: View, song: Song) {
    val v = view as ShapeableImageView
    Glide.with(application).load(song.id.getSongPhotoUri(application)).centerCrop().placeholder(R.drawable.music_small_min).into(v)
}

@BindingAdapter("android:setPhotoBitmap")
fun setPhotoBitmap(view: View, bitmap: Bitmap?) {
    val v = view as ImageView
    if (bitmap != null){
        v.setImageBitmap(bitmap)
    }else{
        v.setImageResource(R.drawable.music_medium_min)
    }
}

