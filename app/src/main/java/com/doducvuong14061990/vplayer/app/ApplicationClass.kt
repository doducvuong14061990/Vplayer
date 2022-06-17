package com.doducvuong14061990.vplayer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.doducvuong14061990.vplayer.utils.Constants.Companion.CHANNEL_ID
import com.doducvuong14061990.vplayer.utils.Constants.Companion.VPLAYER_SERVICE

class ApplicationClass : Application() {

    companion object{
        lateinit var application: Application
    }
    override fun onCreate() {
        super.onCreate()
        application = this

         createChannelIdNotification()
    }

    private fun createChannelIdNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel : NotificationChannel =
                NotificationChannel(CHANNEL_ID,
                    VPLAYER_SERVICE,
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setSound(null, null)
            val notificationManager : NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}