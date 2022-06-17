package com.doducvuong14061990.vplayer.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.doducvuong14061990.vplayer.app.ApplicationClass
import com.doducvuong14061990.vplayer.data.model.Song
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import kotlin.collections.ArrayList

class Repository private constructor() : DefaultLifecycleObserver{

    private var job: Job? = null

    private val _mListSong = MutableLiveData<ArrayList<Song>>()
    val mListSong: LiveData<ArrayList<Song>>
        get() = _mListSong

    /** Repository da co vong doi cua homeActivity */
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycleScope.launch(Dispatchers.IO) {
            getAllAudio()
        }
    }

    private suspend fun getAllAudio() {
        val tempList = ArrayList<Song>()

        job =
            CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, exception ->
                job?.cancel()
                exception.printStackTrace()
            }).launch {
                val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
                val contentResolver: ContentResolver = ApplicationClass.application.contentResolver

                val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.AudioColumns.DATA,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Albums.ARTIST_ID,
                    MediaStore.Audio.Albums.ALBUM_ID,
                    MediaStore.Audio.AudioColumns.DURATION,
                )
                val cursor: Cursor? = contentResolver.query(
                    uri,
                    projection,
                    selection,
                    null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " DESC",
                    null
                )
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            val song = Song(
                                cursor.getLong(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                cursor.getString(5),
                                cursor.getLong(6),
                                cursor.getString(7).toLong()
                            )
                            val file = File(song.path)
                            if (file.exists()) tempList.add(song)

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("e", e.message.toString())
                        }
                    }
                    cursor.close()
                }

            }
        job?.join()
        withContext(Dispatchers.Main) {
            _mListSong.postValue(tempList)
        }
        job?.cancel()
    }

    companion object {
        @Volatile
        private var instance: Repository? = null

        fun getInstance(): Repository = synchronized(this) {
            return@synchronized instance ?: Repository().also {
                instance = it
            }
        }
    }

}

/** Chương trình nén ảnh trước khi upload... khi nào test. */
//fun Camparse() {
//    val size = (bitmap!!.height * (812.0 / bitmap!!.width)).toInt()
//    val b = Bitmap.createScaledBitmap(bitmap!!, 812, size, true)
//    val by = ByteArrayOutputStream()
//    b.compress(Bitmap.CompressFormat.JPEG, 100, by)
//    val bytes = by.toByteArray()
//    Image_select = Base64.encodeToString(bytes, 0)
//}

