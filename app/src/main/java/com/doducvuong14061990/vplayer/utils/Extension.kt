package com.doducvuong14061990.vplayer.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.data.model.Song
import java.util.*
import java.util.concurrent.TimeUnit


fun <T> HashSet<T>.typeConversion(): ArrayList<T> {
    val list = ArrayList<T>()
    val iterator = this.iterator()
    while (iterator.hasNext()) {
        list.add(iterator.next())
    }
    return list
}

fun Context.getNavHeight(): Int {
    val resources: Resources = this.resources
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Context.getStatusBarHeight(): Int {
    val resources: Resources = this.resources
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}


fun AppCompatActivity.hideStatusBarAndNavigationBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.navigationBarColor = Color.TRANSPARENT
    }
}

fun Long.formatDuration(): String {
    var intMillis: Long = this
    val dd = TimeUnit.MILLISECONDS.toDays(intMillis)
    val daysMillis = TimeUnit.DAYS.toMillis(dd)
    intMillis -= daysMillis
    val hour: Long = TimeUnit.MILLISECONDS.toHours(intMillis)

    val minutes = TimeUnit.MINUTES.convert(this, TimeUnit.MILLISECONDS)
    val seconds = TimeUnit.SECONDS.convert(
        this,
        TimeUnit.MILLISECONDS
    ) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)

    return if (hour.toString() == "0") String.format(
        "%02d:%02d",
        minutes,
        seconds
    ) else String.format("%02d:%02d:%02d", hour, minutes, seconds)
}

/** Lấy ảnh của mỗi bài hát... */
fun Long.getSongPhotoUri(context: Context): Uri? {
    val uri: Uri = Uri.parse("content://media/external/audio/media/${this}/albumart")
    return try {
        val source: ImageDecoder.Source = ImageDecoder.createSource(
            context.contentResolver,
            uri
        )
        val bitmapResult: Result<Bitmap> = source.runCatching {
            ImageDecoder.decodeBitmap(this).copy(Bitmap.Config.RGBA_F16, true)
        }
        if (bitmapResult.getOrNull() != null) uri else null
    } catch (e: Exception) {
        null
    }
}

/** Lấy ảnh của mỗi bài hát... */
fun Song.getSongPhotoBitmap(context: Context): Bitmap? {
    val uri: Uri = Uri.parse("content://media/external/audio/media/${this.id}/albumart")
    return try {
        val source: ImageDecoder.Source = ImageDecoder.createSource(
            context.contentResolver,
            uri
        )
        val bitmapResult: Result<Bitmap> = source.runCatching {
            ImageDecoder.decodeBitmap(this).copy(Bitmap.Config.RGBA_F16, true)
        }
        bitmapResult.getOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun String.setFormatTitle(context: Context): SpannableString {
    val startTitle = "Playing from Album"
    val endTitle = this
    val title = "$startTitle\n$endTitle"

    val spannable = SpannableString(title)
    spannable.setSpan(
        ForegroundColorSpan(context.resources.getColor(R.color.textColorLight)),
        0,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        startTitle.length,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannable
}

fun String.setFormatEmptyTitle(context: Context): SpannableString {
    return SpannableString(this)
}


fun Int.getBitmapFromDrawable(context: Context): Bitmap {
    return BitmapFactory.decodeResource(
        context.resources,
        this
    )
}



