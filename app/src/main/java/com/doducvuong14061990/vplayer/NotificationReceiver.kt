package com.doducvuong14061990.vplayer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import com.doducvuong14061990.vplayer.app.ApplicationClass
import com.doducvuong14061990.vplayer.data.model.Songs
import com.doducvuong14061990.vplayer.service.VplayerService
import com.doducvuong14061990.vplayer.ui.fragment.player.PlayerFragment
import com.doducvuong14061990.vplayer.utils.Constants.Companion.EXIT
import com.doducvuong14061990.vplayer.utils.Constants.Companion.NEXT
import com.doducvuong14061990.vplayer.utils.Constants.Companion.PLAY
import com.doducvuong14061990.vplayer.utils.Constants.Companion.PREVIOUS
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PREVIOUS = 0
        const val ACTION_PAUSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_NEXT = 3
        const val ACTION_CANCEL = 4
        const val ACTION_SEEK: Int = 5

        const val ACTION_PHONE_IS_RINGING = 9
        const val ACTION_PHONE_CALL_IS_STARTED = 10
        const val ACTION_PHONE_CALL_IS_ENDED: Int = 11
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.getStringExtra(TelephonyManager.EXTRA_STATE)) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                /** Phone call is stared... */
                context?.let {
                    actionSendToService(it, ACTION_PHONE_CALL_IS_STARTED)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                /** Phone call is ended... */
                context?.let {
                    actionSendToService(it, ACTION_PHONE_CALL_IS_ENDED)
                }
            }

            TelephonyManager.EXTRA_STATE_RINGING -> {
                /** Incoming call... */
                context?.let {
                    actionSendToService(it, ACTION_PHONE_IS_RINGING)
                }
            }
            else -> {
                val actionMusic: Int? = intent?.getIntExtra("action_music", -1)
                val intentService: Intent = Intent(context, VplayerService::class.java)
                val bundle: Bundle = Bundle()
                bundle.putString("start_point", "Notification")
                if (actionMusic != null && actionMusic > -1) {
                    bundle.putInt("action_music_service", actionMusic)
                    intentService.putExtras(bundle)
                    /** Dòng lệnh khiến service gọi onStartCommand liên tục theo mỗi lân seekbar chạy? */
                    context?.startService(intentService)
                }
            }
        }
    }

    private fun actionSendToService(context: Context, action: Int) {
        val intentService: Intent = Intent(context, VplayerService::class.java)
        val bundle: Bundle = Bundle()
        bundle.putInt("action_music_service", action)
        intentService.putExtras(bundle)
        context.startService(intentService)
    }
}
