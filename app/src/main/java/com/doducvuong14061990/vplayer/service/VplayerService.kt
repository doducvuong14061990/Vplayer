package com.doducvuong14061990.vplayer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import com.doducvuong14061990.vplayer.NotificationReceiver
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_CANCEL
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_NEXT
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PAUSE
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PHONE_CALL_IS_ENDED
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PHONE_CALL_IS_STARTED
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PHONE_IS_RINGING
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_PREVIOUS
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_RESUME
import com.doducvuong14061990.vplayer.NotificationReceiver.Companion.ACTION_SEEK
import com.doducvuong14061990.vplayer.R
import com.doducvuong14061990.vplayer.data.model.Song
import com.doducvuong14061990.vplayer.data.model.Songs
import com.doducvuong14061990.vplayer.data.response.MediaPlayerSingleton
import com.doducvuong14061990.vplayer.data.response.MediaPlayerSingleton.Companion.getInstance
import com.doducvuong14061990.vplayer.ui.activity.HomeActivity
import com.doducvuong14061990.vplayer.utils.Constants.Companion.CHANNEL_ID
import com.doducvuong14061990.vplayer.utils.getSongPhotoBitmap
import java.util.Collections.shuffle
import kotlin.system.exitProcess


class VplayerService : Service() {
    val mediaPlayerSingleton: MediaPlayerSingleton = getInstance()

    var mSong: Song? = null
    var mListSong: ArrayList<Song>? = null
    var mPosition: Int = -1

    var mShuffle: Boolean = false

    // Dùng cho trường hợp cần khôi phục thứ tự các bài hát, sau khi xáo trộn.
    private var mSongListOld = arrayListOf<Song>()


    var isPlaying: Boolean = false
    var isRepeatAll: Boolean = false
    var mRepeatPattern: Int = 0

    private lateinit var mediaSessionCompat: MediaSessionCompat

    private val mBinder: VplayerBinder = VplayerBinder()

    inner class VplayerBinder : Binder() {
        fun getVplayerService(): VplayerService = this@VplayerService
    }

    override fun onCreate() {
        Log.d("CCC", "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CCC", "onStartCommand")
        if (intent == null) return START_NOT_STICKY
        val bundle: Bundle? = intent.extras
        val startPoint: String? = bundle?.getString("start_point", "")

        if (startPoint != null && startPoint != "" && startPoint == "Player_Fragment") {
            val songs: Songs? = bundle.getParcelable("object_songs")
            val position: Int = bundle.getInt("song_position", -1)
            songs?.let {
                mListSong = it.mListSong
                mSongListOld.addAll(it.mListSong)
                if (position > -1) {
                    mPosition = position
                    mSong = it.mListSong[position]
                    mSong?.let { song ->
                        createMediaPlayer(song)
                        sendDataVsActionToSongFragment()
                    }
                }
            }
        }

        if (startPoint != null && startPoint != "" && startPoint == "Notification") {
            val action: Int = bundle.getInt("action_music_service")
            handleAction(action)
        }

        if (startPoint != null && startPoint != "" && startPoint == "Control_Bottom") {
            val actionMusic = bundle.getInt("action_music_service")
        }

        return START_NOT_STICKY
    }

    private fun handleAction(action: Int) {
        when (action) {
            ACTION_PHONE_CALL_IS_STARTED -> {
                onPauseMusic()
            }
            ACTION_PHONE_IS_RINGING -> {
                onPauseMusic()
            }
            ACTION_PHONE_CALL_IS_ENDED -> {
                onResumeMusic()
            }

            ACTION_CANCEL -> {
                exitProcess(0)
            }
            ACTION_NEXT -> {
                onPreviousNext(true)
            }
            ACTION_PAUSE -> {
                onPauseMusic()
            }

            ACTION_RESUME -> {
                onResumeMusic()
            }

            ACTION_PREVIOUS -> {
                onPreviousNext(false)
            }
            ACTION_SEEK -> {
                mSong?.let { showNotification(it, 1.0F) }
            }
        }
    }

    fun setActionRepeat(value: Int, startPoint: String) {
        isRepeat(value, startPoint)
    }


    override fun onBind(intent: Intent?): IBinder {
        Log.d("CCC", "onBind")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("CCC", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d("CCC", "onDestroy")
        if (mediaPlayerSingleton.mp != null) {
            isPlaying = false
            mSongListOld.clear()
            mListSong?.clear()
            mSong = null
            mediaPlayerSingleton.mp = null
            mediaSessionCompat.release()
        }
        super.onDestroy()
    }

    private fun createMediaPlayer(song: Song) {
        try {
            mediaPlayerSingleton.mp?.reset()
            mediaPlayerSingleton.mp?.setDataSource(song.path)
            mediaPlayerSingleton.mp?.prepare()
            mediaPlayerSingleton.mp?.start()
            isPlaying = true
            showNotification(song, 1.0F)

            mediaPlayerSingleton.mp.also { mediaPlayer ->
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(song.path)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                isPlaying = true
                showNotification(song, 1.0F)
            }?.setOnCompletionListener { mediaPlayer ->
                if (isRepeatAll) {
                    onPreviousNext(true)
                } else {
                    mListSong?.let {
                        if (mPosition < it.size - 1) {
                            onPreviousNext(true)
                        } else if (mPosition == it.size - 1) {
                            mediaPlayer.stop()
                            isPlaying = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onPauseMusic() {
        if (mediaPlayerSingleton.mp == null) return
        mediaPlayerSingleton.mp?.pause()
        isPlaying = false
        mSong?.let {
            showNotification(it, 0.0F)
            sendDataVsActionToSongFragment()
            sendDataVsActionToPlayerFragment()
        }
    }

    fun onResumeMusic() {
        if (mediaPlayerSingleton.mp == null) return
        mediaPlayerSingleton.mp?.start()
        isPlaying = true
        mSong?.let {
            showNotification(it, 1.0F)
            sendDataVsActionToSongFragment()
            sendDataVsActionToPlayerFragment()
        }
    }

    fun onPreviousNext(isNext: Boolean) {
        mListSong?.let {
            if (isNext) {
                if (mPosition == it.size - 1) {
                    mPosition = 0
                } else {
                    mPosition += 1
                }

            } else {
                if (mPosition == 0) {
                    mPosition = it.size - 1
                } else {
                    mPosition -= 1
                }
            }
            mSong = it[mPosition]
            mSong?.let { song ->
                if (isPlaying) {
                    sendDataVsActionToSongFragment()
                    sendDataVsActionToPlayerFragment()
                    createMediaPlayer(song)
                } else {
                    isPlaying = true
                    sendDataVsActionToSongFragment()
                    sendDataVsActionToPlayerFragment()
                    createMediaPlayer(song)
                }

            }
        }
    }


    fun isShuffle(value: Boolean) {
        mShuffle = if (mListSong == null) {
            false
        } else {
            if (value) {
                mListSong?.let {
                    shuffle(it)
                }
                true
            } else {
                mListSong?.clear()
                mListSong?.addAll(mSongListOld)
                false
            }
        }
    }

    private fun isRepeat(value: Int, startPoint: String) {
        when (value) {
            0 -> {
                mRepeatPattern = 0
                mediaPlayerSingleton.mp?.isLooping = false
                isRepeatAll = false
                if (startPoint == "PlayerFragment") {
                    sendDataVsActionToSongFragment()
                }
            }
            1 -> {
                mRepeatPattern = 1
                mediaPlayerSingleton.mp?.isLooping = true
                isRepeatAll = false
                if (startPoint == "PlayerFragment") {
                    sendDataVsActionToSongFragment()
                }
            }

            2 -> {
                mRepeatPattern = 2
                mediaPlayerSingleton.mp?.isLooping = false
                isRepeatAll = true
                if (startPoint == "PlayerFragment") {
                    sendDataVsActionToSongFragment()
                }
            }
        }
    }

    private fun sendDataVsActionToPlayerFragment() {
        val intent: Intent = Intent("Send_Data_Vs_Action_To_PlayerFragment")
        val bundle: Bundle = Bundle()
        bundle.putString("point_start", "VplayerService")
        bundle.putParcelable("object_song", mSong)
        bundle.putBoolean("status_player", isPlaying)
        bundle.putInt("repeat_pattern", mRepeatPattern)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendDataVsActionToSongFragment() {
        val intent: Intent = Intent("Send_Data_Vs_Action_To_SongsFragment")
        val bundle: Bundle = Bundle()
        bundle.putString("point_start", "VplayerService")
        bundle.putParcelable("object_song", mSong)
        bundle.putBoolean("status_player", isPlaying)
        bundle.putInt("repeat_pattern", mRepeatPattern)
        intent.putExtras(bundle)

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun showNotification(song: Song, playbackSpeed: Float) {
        val bundle: Bundle = Bundle()
        bundle.putString("start_point", "Notification")
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(HomeActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.playerFragment)
            .setArguments(bundle)
            .createPendingIntent()

        mediaSessionCompat = MediaSessionCompat(this, "tag")

        mediaPlayerSingleton.mp?.let {
            mediaSessionCompat.setMetadata(
                MediaMetadataCompat.Builder().putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    it.duration.toLong()
                ).build()
            )
            mediaSessionCompat.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        it.currentPosition.toLong(),
                        playbackSpeed
                    )
                    .setActions(ACTION_SEEK_TO).build()
            )

            mediaSessionCompat.setCallback(object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    it.seekTo(pos.toInt())
//                    sendBroadcastAction(ACTION_SEEK)
                    mSong?.let { it1 -> showNotification(it1, 1.0F) }
                }
            })
        }

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.ic_music)
        notificationBuilder.setSubText("")
        notificationBuilder.setContentTitle(song.title)
        notificationBuilder.setContentText(song.artist)
        notificationBuilder.setLargeIcon(song.getSongPhotoBitmap(this))
        notificationBuilder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSessionCompat.sessionToken)
        )
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.addAction(
            R.drawable.ic_skip_previous,
            "Previous",
            getPendingIntent(this, ACTION_PREVIOUS)
        )
        if (isPlaying) {
            notificationBuilder.addAction(
                R.drawable.ic_pause,
                "Pause",
                getPendingIntent(this, ACTION_PAUSE)
            )
        } else {
            notificationBuilder.addAction(
                R.drawable.ic_play,
                "Play",
                getPendingIntent(this, ACTION_RESUME)
            )
        }
        notificationBuilder.addAction(
            R.drawable.ic_skip_next,
            "Next",
            getPendingIntent(this, ACTION_NEXT)
        )
        notificationBuilder.addAction(
            R.drawable.ic_close,
            "Close",
            getPendingIntent(this, ACTION_CANCEL)
        )

        val notification: Notification = notificationBuilder.build()

        startForeground(1, notification)
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent: Intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("action_music", action)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
}


